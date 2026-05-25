# Runtime Ingestion Roadmap

This note defines the first implementation roadmap for runtime ingestion
adapters that will consume `protocol-sdk` parser modules from a separate JDK 21
collector runtime.

The SDK remains reusable independently from the runtime. Runtime adapters may
use Netty, MQTT, Kafka, HTTP servers, databases, queues, and deployment
frameworks, but those dependencies must not flow back into SDK modules.

## Goals

- Define the first practical order for TCP/Netty, MQTT, Kafka, and HTTP
  ingestion.
- Keep every ingress adapter protocol-neutral until it hands payloads to a
  runtime protocol binding.
- Define minimal interfaces needed to route payloads into SDK decoders.
- Preserve a clean dependency line from runtime modules to SDK modules.
- Make replay, diagnostics, backpressure, and parse errors first-class runtime
  concerns.

## Non-goals

- Do not add runtime adapter code to `protocol-sdk` in this roadmap.
- Do not introduce Spring, Netty, MQTT, Kafka, or HTTP dependencies into SDK
  parser modules.
- Do not define storage schemas or alarm/business semantics here.
- Do not require a single transport to know all protocol details.

## Implementation Order

Recommended first sequence:

| Step | Slice | Why first |
| --- | --- | --- |
| 1 | `runtime-core` contracts | All adapters need the same source, envelope, parse-result, queue, and sink contracts. |
| 2 | `runtime-protocol-iec104` | IEC104 is the most mature SDK parser and validates stateful stream decoding. |
| 3 | TCP/Netty for IEC104 | Most production collectors start with stable TCP sessions and reconnect behavior. |
| 4 | `runtime-protocol-modbus` plus Modbus TCP | Reuses TCP lifecycle while validating request/response metadata and unit IDs. |
| 5 | HTTP ingress | Simple generic push path for integration tests and non-persistent device/cloud callbacks. |
| 6 | MQTT ingress | Device/cloud scenarios need topic mapping, QoS handling, and payload format selection. |
| 7 | Kafka ingress | Replay, backfill, and pipeline integration should build on already proven parser bindings. |
| 8 | IEC101 and IEC103 bindings | Add after serial bridge/session policy is defined and real traces are available. |

This order intentionally starts with one mature stream protocol and one mature
TCP transport before adding message-oriented and replay-oriented adapters.

## Shared Runtime Interfaces

The first runtime interfaces should stay small enough to support all adapters.

```java
public interface IngressAdapter {
    void start();
    void stop();
}
```

```java
public interface IngressPublisher {
    PublishDecision publish(IngressEnvelope envelope);
}
```

```java
public interface ProtocolBinding {
    List<RuntimeParseResult> parse(IngressEnvelope envelope);
}
```

```java
public interface RuntimeSink {
    SinkResult write(List<ParsedRecord> records);
}
```

Minimum shared value objects:

| Type | Purpose |
| --- | --- |
| `SourceId` | Stable source identity from device ID, channel ID, topic, connection, or replay key. |
| `IngressEnvelope` | Raw payload bytes plus source, transport metadata, receive time, and tracing IDs. |
| `TransportMetadata` | Adapter-specific metadata such as remote address, topic, partition, offset, headers, or request ID. |
| `RuntimeParseResult` | Success or error output from a protocol binding. |
| `ParsedRecord` | Runtime normalized record that keeps typed protocol frame/value references and raw diagnostic bytes. |
| `ParseError` | Structured parser error with source, raw bytes, consumed bytes, message, and recoverability. |
| `BackpressureState` | Queue depth, high-water/low-water status, and adapter action. |

The SDK types stay inside `protocol-sdk`. Runtime records may hold SDK frame
objects as typed references, but SDK modules must never reference runtime types.

## TCP/Netty Ingestion

First scope:

- One Netty server module for TCP collector sessions.
- One source mapping per accepted connection.
- One stateful SDK stream decoder per active session.
- Initial protocol bindings for IEC104 and Modbus TCP.
- Session metrics for connected sources, inbound bytes, parsed frames, parse
  errors, reconnects, and queue depth.

Minimal adapter flow:

```text
Netty channel read
  -> source lookup
  -> IngressEnvelope
  -> bounded session queue
  -> ProtocolBinding.parse(...)
  -> runtime pipeline
```

IEC104 binding:

- Use one `Iec104StreamDecoder` per TCP session.
- Preserve raw APDU bytes on each parsed frame.
- Treat `INCOMPLETE` as normal stream buffering.
- Emit parse errors without closing the channel unless runtime policy says so.

Modbus TCP binding:

- Use one `ModbusTcpStreamDecoder` per TCP session.
- Preserve transaction ID, unit ID, function code, and exception response
  metadata for correlation.
- Keep polling schedules and outstanding request maps outside the SDK parser.

Backpressure:

- Use bounded per-session queues.
- Disable Netty auto-read or pause reads at high-water marks.
- Resume reads after low-water marks.
- Close or quarantine sessions that exceed malformed-frame or queue-overflow
  policy.

Follow-up scope:

- TLS and authentication.
- TCP client mode for devices that require outbound connections.
- TCP-to-serial bridge profiles for IEC101 and IEC103.
- Heartbeat and reconnect policy per protocol profile.

## HTTP Ingestion

First scope:

- Generic HTTP endpoint for binary or JSON push payloads.
- Source mapping from path, token, header, query parameter, or body field.
- Request-size limits and content-type validation.
- Optional synchronous parse for tests; asynchronous enqueue by default.

Minimal adapter flow:

```text
HTTP request
  -> auth and source mapping
  -> payload extraction
  -> IngressEnvelope
  -> bounded ingress queue
  -> ProtocolBinding.parse(...)
```

Backpressure:

- Return `429` when source-level queues are saturated.
- Return `503` when runtime-wide queues are saturated.
- Preserve request ID, source ID, payload hash, and error body for diagnostics.

First protocol targets:

- Generic raw binary forwarding to an explicitly configured protocol binding.
- JSON push payloads converted by runtime-only mappers.
- IEC104 or Modbus payload replay for integration tests.

## MQTT Ingestion

First scope:

- Subscribe to configured topics.
- Map topic patterns to source IDs and protocol binding names.
- Support binary payloads and JSON payloads.
- Use configured QoS per topic group.

Minimal adapter flow:

```text
MQTT message
  -> topic rule match
  -> source and protocol selection
  -> IngressEnvelope
  -> bounded topic/source queue
  -> ProtocolBinding.parse(...)
```

Backpressure:

- Bound in-flight messages per subscription.
- Delay acknowledgement until payload handoff reaches the configured
  durability point.
- Disconnect or slow subscriptions only through explicit policy.
- Route malformed payloads to parse-error handling instead of dropping them.

First protocol targets:

- Device/cloud binary payloads that already contain SDK-supported protocol
  frames.
- Vendor JSON converted by runtime-only mappers.
- Future protocol-specific topic profiles.

## Kafka Ingestion

First scope:

- Consume raw payload events for replay, backfill, and pipeline integration.
- Map topic/partition/key/header metadata to source IDs and protocol bindings.
- Commit offsets only after parse and downstream handoff reach the chosen
  durability point.

Minimal adapter flow:

```text
Kafka record
  -> source and protocol mapping
  -> IngressEnvelope
  -> partition-aware parser queue
  -> ProtocolBinding.parse(...)
  -> sink handoff
  -> offset commit
```

Backpressure:

- Pause partitions when parser queues or sinks exceed high-water marks.
- Resume partitions below low-water marks.
- Keep per-partition ordering when the configured sink requires it.
- Track parse errors by topic, partition, offset, source, protocol, and payload
  hash.

First protocol targets:

- Replay of IEC104 TCP payload captures.
- Replay of Modbus TCP ADUs.
- Future replay of MQTT or HTTP envelopes.

## Protocol Binding Matrix

| Protocol | First ingress | Later ingress | Runtime note |
| --- | --- | --- | --- |
| IEC104 | TCP/Netty | Kafka replay, HTTP test push, MQTT binary payload | Mature stateful stream parser; best first runtime validation target. |
| Modbus TCP | TCP/Netty | Kafka replay, HTTP test push | Reuses TCP stream infrastructure and validates request/response metadata. |
| IEC101 | TCP-to-serial bridge after TCP baseline | Kafka replay | Needs serial session and balanced/unbalanced policy outside SDK. |
| IEC103 | TCP-to-serial bridge after TCP baseline | Kafka replay | Needs relay session policy and real traces outside SDK. |
| HTTP payloads | HTTP | Kafka replay | Runtime-only mappers should avoid polluting SDK parsers. |
| Vendor MQTT payloads | MQTT | Kafka replay | Topic and payload mapping belong in runtime configuration. |

## Backpressure Defaults

Initial defaults should be conservative:

| Boundary | Default behavior |
| --- | --- |
| Adapter to parser queue | Bounded per source; reject, pause, or slow reads at high-water mark. |
| Parser to sink queue | Bounded per sink group; dead-letter parse errors separately. |
| TCP | Disable auto-read or close session by policy. |
| HTTP | Return `429` or `503`. |
| MQTT | Bound in-flight messages and delay acknowledgement by policy. |
| Kafka | Pause partitions and resume later. |

Every dropped, rejected, paused, resumed, or dead-lettered payload needs metrics
and enough metadata to explain why it happened.

## Error And Diagnostic Outputs

Each adapter should emit structured diagnostics:

- `sourceId`.
- transport name.
- protocol binding name.
- receive timestamp.
- raw payload hash and optional raw payload reference.
- parse result status.
- parser message and consumed byte count for SDK parse errors.
- transport metadata such as remote address, topic, offset, headers, or request
  ID.

The runtime may redact or externalize raw payload storage, but the SDK should
continue preserving raw bytes on parser outputs.

## Acceptance Mapping

| Issue #18 criterion | Roadmap answer |
| --- | --- |
| TCP/Netty ingestion for IEC101/IEC104 and Modbus TCP | TCP/Netty starts with IEC104 and Modbus TCP, then extends to IEC101/IEC103 through TCP-to-serial bridge profiles. |
| MQTT ingestion for device/cloud scenarios | MQTT topic mapping, QoS, payload formats, acknowledgement timing, and backpressure policy are defined. |
| Kafka ingestion for replay and pipeline integration | Kafka replay, backfill, partition pause/resume, offset commit timing, and diagnostics are defined. |
| HTTP ingestion for generic push scenarios | HTTP source mapping, payload extraction, request limits, async enqueue, and saturation responses are defined. |
| First implementation order and minimal interfaces | The roadmap lists implementation sequence and shared interfaces for adapters, publisher, binding, and sink. |
| SDK remains reusable independently | Runtime dependencies remain outside SDK modules, and runtime bindings depend one-way on SDK parser modules. |
