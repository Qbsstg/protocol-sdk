# Runtime Platform Architecture

This note defines the intended architecture for a future JDK 21 collector
runtime that consumes the Java 8 compatible protocol SDK modules without
introducing runtime dependencies back into the SDK.

## Goals

- Keep `protocol-sdk` focused on protocol parsing and Java 8 compatible public
  parser APIs.
- Build the collector runtime on JDK 21 so runtime code can use modern JVM
  operations, observability, and deployment practices.
- Support TCP/Netty, MQTT, Kafka, and HTTP ingress adapters without coupling
  those transports to SDK modules.
- Define a runtime pipeline for backpressure, batching, parser errors,
  diagnostics, and downstream delivery.
- Keep closed-source deployment concerns out of the open protocol SDK.

## Non-goals

- Do not add Spring, Netty, database, Redis, MQTT, Kafka, or HTTP server
  dependencies to `protocol-core`, `protocol-iec104`, `protocol-iec101`,
  `protocol-iec103`, or `protocol-modbus`.
- Do not make SDK decoders aware of device registries, tenant models,
  persistence, scheduling, retry policies, or alarm semantics.
- Do not migrate the SDK source target from Java 8 just because the runtime uses
  JDK 21.
- Do not make the runtime repository the release vehicle for SDK parser
  artifacts.

## Repository Boundary

The recommended long-term split is two public repositories:

| Repository | JDK target | Responsibility |
| --- | --- | --- |
| `protocol-sdk` | Java 8 compatible, built on JDK 8 and JDK 21 CI | Protocol parser contracts, stream/datagram decoders, typed protocol models, fixtures, and parser docs. |
| `protocol-runtime` | JDK 21 | Collector runtime, ingress adapters, session management, backpressure, batching, persistence integrations, and deployment assembly. |

The runtime can be prototyped from design documents in this repository, but
runtime implementation modules should not be added to the SDK reactor unless
there is a deliberate short-lived spike. The SDK should remain publishable as a
small Maven Central parser library.

## Dependency Direction

Dependency direction is one-way:

```text
runtime app
  -> runtime ingress adapters
  -> runtime protocol bindings
  -> protocol-sdk modules
```

Forbidden directions:

```text
protocol-sdk -> runtime
protocol-sdk -> Spring or Netty
protocol-sdk -> MQTT or Kafka clients
protocol-sdk -> database or Redis clients
```

Runtime protocol bindings may depend on SDK modules:

- `runtime-protocol-iec104` depends on `protocol-iec104`.
- `runtime-protocol-iec101` depends on `protocol-iec101`.
- `runtime-protocol-iec103` depends on `protocol-iec103`.
- `runtime-protocol-modbus` depends on `protocol-modbus`.

SDK modules should expose parsing outputs only. Runtime bindings translate SDK
frames into runtime records, attach device/source context, and route outputs to
pipelines or sinks.

## Runtime Module Shape

Proposed runtime modules:

| Module | Responsibility |
| --- | --- |
| `runtime-core` | Runtime-neutral contracts: source identity, ingress envelope, parser binding, parsed record, parse error, batch, sink, metrics tags, and backpressure decisions. |
| `runtime-ingress-tcp-netty` | Netty-based TCP sessions for IEC104, Modbus TCP, and TCP-to-serial bridge scenarios for IEC101/IEC103. |
| `runtime-ingress-mqtt` | MQTT subscriptions for device/cloud payloads and topic-to-source mapping. |
| `runtime-ingress-kafka` | Kafka consumption for replay, integration pipelines, and bulk backfill. |
| `runtime-ingress-http` | HTTP endpoints for generic push scenarios and simple integration tests. |
| `runtime-protocol-*` | Per-protocol bindings that own decoder lifecycle and convert `ParseResult` outputs into runtime records. |
| `runtime-pipeline` | Bounded queues, batching, worker assignment, retry policy, dead-letter routing, and delivery orchestration. |
| `runtime-sink-*` | Optional storage, message queue, observability, or downstream integrations. |
| `runtime-app` | Deployable assembly and configuration. |

This module shape lets transport and protocol concerns evolve independently.
For example, MQTT can carry raw IEC104 APDUs or JSON payloads without changing
the IEC104 SDK parser.

## Minimal Runtime Interfaces

Runtime interfaces should be small and protocol-neutral.

```java
public interface IngressAdapter {
    void start();
    void stop();
}
```

```java
public final class IngressEnvelope {
    private SourceId sourceId;
    private byte[] payload;
    private TransportMetadata metadata;
    private long receivedAtEpochMillis;
}
```

```java
public interface ProtocolBinding {
    List<RuntimeParseResult> parse(IngressEnvelope envelope);
}
```

```java
public final class RuntimeParseResult {
    private SourceId sourceId;
    private Object protocolFrame;
    private ParsedRecord record;
    private ParseError error;
    private byte[] rawBytes;
}
```

The runtime should not expose `Map<String, Object>` as the primary parsed data
model. It can provide a generic export view later, but internal records should
keep typed protocol details and raw bytes.

## Ingress Adapter Contracts

Ingress adapters should only own transport concerns:

- Connection/session lifecycle.
- Authentication and transport-level metadata.
- Byte or message boundary extraction.
- Source identification.
- Backpressure handoff.
- Metrics and transport diagnostics.

Ingress adapters should not parse protocol fields directly. They pass payloads
to protocol bindings through `IngressEnvelope`.

### TCP/Netty

TCP sessions should map one connection to one source context and one stateful
SDK stream decoder. Netty handlers own connection lifecycle and byte delivery,
but protocol parsing belongs in runtime protocol bindings.

Backpressure behavior:

- Use bounded per-session queues.
- Disable or slow reads when parser workers exceed high-water marks.
- Close or quarantine sessions that repeatedly exceed malformed-frame limits.
- Keep reconnect, retry, and heartbeat policy in runtime session code, not in
  SDK decoders.

### MQTT

MQTT ingress should map topics to source IDs and payload formats. The payload
may be raw protocol bytes, JSON, or vendor-specific binary. Topic routing and
payload selection belong in runtime configuration.

Backpressure behavior:

- Bound inflight messages per subscription.
- Use QoS and acknowledgement timing deliberately.
- Route malformed payloads to parse-error handling instead of dropping them
  silently.

### Kafka

Kafka ingress should support replay, backfill, and integration pipelines.
Offsets are committed only after parse and downstream handoff reach the chosen
runtime durability point.

Backpressure behavior:

- Pause partitions when parser or sink queues exceed thresholds.
- Resume partitions after queue depth returns below low-water marks.
- Track parse errors by topic, partition, offset, source, and protocol.

### HTTP

HTTP ingress should support generic push scenarios and low-friction
integration tests. It should validate request size, authentication, content
type, and source mapping before enqueueing payloads.

Backpressure behavior:

- Return `429` or `503` when queues are saturated.
- Preserve request IDs and raw payloads for diagnostics.
- Avoid doing expensive parsing on request threads unless the deployment is
  explicitly configured for synchronous parsing.

## Parser Binding Lifecycle

Runtime bindings own decoder lifecycle:

- One stream decoder per TCP session where the SDK decoder is stateful.
- One datagram or message parse call per UDP, MQTT, Kafka, or HTTP payload when
  the payload already has message boundaries.
- Decoder reset on reconnect, protocol switch, or explicit session reset.
- Parser configuration resolved from source metadata.

`ParseResult` handling:

| SDK result | Runtime handling |
| --- | --- |
| `SUCCESS` | Convert frame/value into one or more runtime records and attach source metadata. |
| `INCOMPLETE` | Keep decoder buffer in the session binding and wait for more bytes. |
| `ERROR` | Emit parse error with raw bytes, consumed byte count, source metadata, and diagnostic message. |

Malformed payloads should not crash ingress adapters. Runtime policy decides
whether to keep reading, reset the decoder, quarantine the source, or close the
session.

## Backpressure And Batching

Backpressure is a runtime concern. The SDK should continue returning parser
results without queue ownership.

Runtime defaults:

- Bounded queues between ingress, parser workers, and sinks.
- High-water and low-water marks per source and per adapter.
- Transport-specific pause or slow-read hooks.
- Metrics for queue depth, parse latency, sink latency, malformed frames, and
  dropped or rejected messages.
- Dead-letter output for payloads that cannot be parsed after configured
  recovery attempts.

Batching belongs after parsing. Batches should group records by downstream
delivery needs, not by parser internals. A batch should preserve source ID,
protocol, time range, raw diagnostic references, and delivery attempt metadata.

## Error Handling

Runtime errors should be explicit and observable:

- Transport error: connection reset, TLS/auth failure, MQTT disconnect, Kafka
  rebalance, or HTTP rejection.
- Parse error: SDK `ParseResult.error(...)` with raw bytes and consumed byte
  count.
- Mapping error: frame parsed, but runtime record conversion failed.
- Delivery error: sink or downstream system failed.

Each category should have counters, logs, and retry/quarantine policy. Parser
fixtures remain in SDK modules; operational retry policy remains in runtime.

## JDK 21 Runtime Choices

The runtime may use JDK 21 features because it is not part of the Java 8 SDK
surface:

- Virtual threads for blocking sink or management operations where they reduce
  thread-pool complexity.
- Records for runtime-only DTOs if they stay out of SDK public APIs.
- Modern TLS, observability, and container runtime defaults.
- Pattern matching or sealed types only inside runtime modules where they do
  not leak into Java 8 artifacts.

SDK artifacts must continue to compile with Java 8 source compatibility and
CI on JDK 8 and JDK 21.

## Implementation Order

1. Create runtime repository or prototype branch with `runtime-core` contracts.
2. Add `runtime-protocol-iec104` binding using existing `protocol-iec104`
   stream decoder fixtures.
3. Add TCP/Netty ingress for IEC104 because it is the most mature parser and
   validates stateful stream decoding.
4. Add Modbus TCP binding and TCP ingress reuse.
5. Add MQTT and HTTP ingress adapters for message-oriented push scenarios.
6. Add Kafka ingress for replay and integration pipelines.
7. Add IEC101 and IEC103 runtime bindings once session policies and real traces
   are available.
8. Add sink modules and deployment assembly after parser and ingress contracts
   are stable.

## Acceptance Mapping

| Issue #19 criterion | Design answer |
| --- | --- |
| Runtime repository/module boundaries | The design separates `protocol-sdk` from a future JDK 21 `protocol-runtime` and lists runtime modules. |
| Runtime consumes SDK without reverse dependency | Dependency direction is one-way from runtime bindings to SDK modules; SDK-to-runtime dependencies are explicitly forbidden. |
| Ingress adapters for TCP/Netty, MQTT, Kafka, and HTTP | Each adapter has a boundary and backpressure contract. |
| Backpressure, batching, and parse-result handling | Runtime queueing, pause behavior, batching, dead-letter routing, and `ParseResult` handling are defined. |
| SDK remains independent | The design preserves Java 8 SDK modules with no Spring, Netty, database, Redis, MQTT, Kafka, or HTTP dependencies. |
