# SDK 0.6.0 Release Plan

`0.6.0` is the Modbus stable-completion target after the published `0.5.0`
IEC101 and IEC103 completion release. The goal is to promote
`protocol-modbus` from an experimental parser module to a practical stable SDK
module for Modbus TCP and common Modbus-over-UDP deployments.

The release remains part of the Java 8 compatible SDK line. Runtime collectors,
network clients, polling schedulers, retry policies, MQTT/Kafka/HTTP adapters,
databases, Redis, Netty, Spring, and device registry concerns stay outside the
SDK modules.

## Release Intent

| Area | `0.6.0` target |
| --- | --- |
| `protocol-modbus` | Stable Modbus TCP/UDP ADU and PDU parser surface. |
| `protocol-core` | Maintain shared Java 8 parser contracts without runtime dependencies. |
| `protocol-iec101` | Compatibility maintenance after the `0.5.0` completion release. |
| `protocol-iec103` | Compatibility maintenance after the `0.5.0` completion release. |
| `protocol-iec104` | Compatibility maintenance and documented bug fixes only. |
| Runtime platform | Remains separate from SDK release gates. |

`0.6.0` should not claim full Modbus ecosystem coverage. It should claim a
defensible, documented, tested, and stable parser surface for common TCP and UDP
ADU/PDU parsing scenarios.

## Scope

In scope:

- Modbus TCP MBAP stream parsing with buffering, concatenation, max-frame
  limits, and malformed-frame recovery tests.
- Modbus-over-UDP datagram parsing using the same MBAP ADU model.
- Typed request and response payloads for common process-data function codes.
- Typed Modbus exception responses for standard and unknown exception codes.
- Raw-byte fallback for unknown, vendor-specific, or intentionally deferred
  function codes.
- Support matrix and usage guide that clearly separate typed, raw-only,
  unknown, and deferred behavior.
- Maven Central release readiness for all reactor modules at version `0.6.0`.

Out of scope:

- Modbus RTU and Modbus ASCII serial framing.
- Socket clients, connection pools, polling loops, retry policy, and request
  scheduling.
- Device registry, point model mapping, telemetry storage, alarm rules, or
  runtime ingestion adapters.
- Netty, Spring, MQTT, Kafka, HTTP server, database, Redis, or collector
  runtime dependencies inside SDK modules.

## Modbus Function-Code Gates

The stable target should include a support matrix before the release candidate
branch is prepared.

| Code | Name | `0.6.0` target |
| --- | --- | --- |
| `0x01` | Read coils | Typed request and response with quantity-limit tests. |
| `0x02` | Read discrete inputs | Typed request and response with quantity-limit tests. |
| `0x03` | Read holding registers | Typed request and response with quantity-limit tests. |
| `0x04` | Read input registers | Typed request and response with quantity-limit tests. |
| `0x05` | Write single coil | Typed request and response echo with valid coil-value checks. |
| `0x06` | Write single register | Typed request and response echo. |
| `0x0F` | Write multiple coils | Typed request and response with byte-count and quantity-limit tests. |
| `0x10` | Write multiple registers | Typed request and response with byte-count and quantity-limit tests. |
| `0x17` | Read/write multiple registers | Promote from raw-only to typed request and response. |
| `0x80`-style exception responses | Exception response | Typed exception response with original function code and exception code. |
| Unknown/vendor codes | Vendor-specific payloads | Preserve as raw-only when the ADU/PDU envelope is valid. |

Function codes outside this table should not block `0.6.0` unless they become
necessary to avoid misleading the stable claim. Deferred standard function
codes must be documented as raw-only or unknown rather than silently treated as
complete.

## Parser Behavior Gates

TCP stream decoder gates:

- Buffer incomplete MBAP headers and incomplete ADUs.
- Decode multiple concatenated ADUs from one input chunk.
- Preserve transaction id, protocol id, length, unit id, PDU, and raw ADU bytes.
- Enforce the configured maximum ADU length.
- Recover predictably from invalid protocol id, invalid MBAP length, oversized
  ADUs, and malformed PDUs.
- Document decoder statefulness and reset behavior.

UDP datagram decoder gates:

- Treat one datagram as one MBAP ADU.
- Reject datagrams shorter than the MBAP header.
- Enforce declared ADU length.
- Keep strict trailing-byte rejection by default, with documented relaxed
  behavior if the configuration supports it.
- Preserve the original datagram bytes for diagnostics.

Typed PDU gates:

- Validate function-specific payload length.
- Validate response byte counts.
- Validate register payloads have even byte counts.
- Validate standard quantity limits for typed function codes.
- Preserve raw PDU bytes for every typed and raw-only parse result.
- Keep unknown function codes parseable when the envelope is valid.

## Documentation Gates

Before tagging `0.6.0`, these docs should be current:

- `protocol-modbus/docs/function-support-matrix.md`
- `protocol-modbus/docs/api-usage.md`
- `docs/protocol-modbus-design.md`
- `docs/release-readiness-0.6.0.md`
- `docs/release-notes-0.6.0.md`
- README module status and Maven dependency examples

The README should remove the experimental label from `protocol-modbus` only
after the support matrix, usage guide, typed `0x17` work, malformed-frame tests,
and release-readiness audit are complete.

## Verification Gates

The release should not be tagged until these checks pass:

- `mvn -q -pl protocol-modbus -am test`
- `mvn -q verify`
- JDK 8 `mvn -q verify`
- GitHub Actions on JDK 8 and JDK 21
- Central release profile smoke check with publishing disabled
- signed dry run with `central.skipPublishing=true`
- external Maven Central dependency resolution after publication

## Recommended Work Order

1. Add a Modbus support matrix that records current typed, raw-only, unknown,
   and deferred function-code behavior.
2. Add Modbus API usage docs for TCP streams, UDP datagrams, request/response
   correlation, exception responses, and raw fallback.
3. Promote function code `0x17` to typed request and response parsing.
4. Expand malformed MBAP/PDU fixture coverage for TCP and UDP decoders.
5. Add explicit standard quantity-limit and byte-count validation tests.
6. Audit public Modbus model immutability and Java 8 compatibility.
7. Update README status from experimental to stable only when gates are met.
8. Prepare `docs/release-readiness-0.6.0.md` and `docs/release-notes-0.6.0.md`.
9. Run full release verification and publish `0.6.0` through the existing
   Maven Central manual publishing flow.

## Acceptance Criteria

This plan is complete when:

- The `0.6.0` scope is clearly Modbus TCP/UDP stable completion.
- RTU/ASCII, runtime polling, and ingestion concerns are explicitly out of
  scope.
- Function-code gates identify what must be typed before release.
- Documentation gates identify when the experimental label can be removed.
- Verification gates match the repository's Java 8 compatible release process.
