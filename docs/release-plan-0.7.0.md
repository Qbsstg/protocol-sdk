# SDK 0.7.0 Release Plan

`0.7.0` is the IEC104 hardening target after the published `0.6.0`
Modbus stable release. The goal is to bring the oldest public parser in the
SDK up to the same documentation, fixture, and boundary-behavior standard as
the newer IEC101, IEC103, and Modbus modules.

The release remains part of the Java 8 compatible SDK line. Runtime collectors,
network clients, polling schedulers, retry policies, MQTT/Kafka/HTTP adapters,
databases, Redis, Netty, Spring, and device registry concerns stay outside the
SDK modules.

## Release Intent

| Area | `0.7.0` target |
| --- | --- |
| `protocol-iec104` | Practical IEC104 parser hardening, stale audit cleanup, and fixture-backed gap closure. |
| `protocol-core` | Maintain shared Java 8 parser contracts without runtime dependencies. |
| `protocol-iec101` | Compatibility maintenance after the `0.5.0` completion release. |
| `protocol-iec103` | Compatibility maintenance after the `0.5.0` completion release. |
| `protocol-modbus` | Compatibility maintenance after the `0.6.0` stable release. |
| `protocol-http` | Planned only; do not add runtime HTTP server/client concerns to the SDK. |
| Runtime platform | Remains separate from SDK release gates. |

`0.7.0` should not claim formal IEC 60870-5-104 conformance certification. It
should claim a better audited, documented, and regression-tested practical
IEC104 parser surface for common integration work.

## Scope

In scope:

- Refresh the IEC104 completeness audit so it reflects the current post-`0.6.0`
  codebase rather than the original `0.1.0` baseline.
- Add direct fixture coverage for recognized raw-only IEC104 catalog entries,
  especially `M_EI_NA_1` and file-transfer Type IDs `120` through `126`.
- Keep raw-only initialization and file-transfer handling stable unless real
  device traces justify typed public models.
- Expand boundary fixtures around VSQ/SQ behavior, malformed recognized ASDUs,
  strict decoder diagnostics, cause-of-transmission diagnostics, and quality or
  time-tag edge cases.
- Improve IEC104 API documentation for decoder statefulness, strict versus
  permissive ASDU behavior, raw-byte fallback, and support-matrix lookup.
- Preserve Java 8 source compatibility and the current runtime-independent SDK
  module boundary.

Out of scope:

- Formal conformance certification against the licensed IEC standard text.
- IEC104 TCP session state machines, reconnect policy, polling loops,
  select-before-operate orchestration, telemetry storage, or command routing.
- Netty, Spring, MQTT, Kafka, HTTP server/client, database, Redis, or collector
  runtime dependencies inside SDK modules.
- Promoting IEC104 file transfer to typed public models without real traces or
  a separate design decision.
- Starting `protocol-http` implementation before a separate design clarifies
  what belongs in the Java 8 SDK versus the JDK 21 runtime.

## IEC104 Hardening Gates

| Gate | Target evidence |
| --- | --- |
| Audit refresh | `protocol-iec104/docs/iec104-completeness-audit.md` reflects post-`0.6.0` behavior, completed work, and remaining gaps. |
| Raw-only catalog fixtures | Direct tests prove raw-byte preservation for `M_EI_NA_1` and representative file-transfer Type IDs, not only the support matrix classification. |
| Strict diagnostics | Malformed recognized ASDU behavior is documented and covered for both permissive default decoding and strict mode. |
| VSQ/SQ behavior | Sequence and non-sequence information-object layouts have explicit boundary tests or documented permissive behavior. |
| Diagnostic enums | Cause-of-transmission codes and raw code preservation stay covered, including unknown diagnostic causes. |
| Quality and time tags | Representative quality flags and `CP56Time2a` edge cases have direct regression tests for typed value families. |
| API docs | README and IEC104 usage docs explain decoder lifecycle, thread-safety expectations, support matrix usage, and raw fallback. |

## HTTP And Runtime Boundary

HTTP, MQTT, Kafka, and Netty ingestion are runtime concerns unless the SDK has a
small, transport-independent parser/helper surface that can remain Java 8
compatible and dependency-free.

For `0.7.0`, `protocol-http` remains planned only. If HTTP work starts during
this line, the first PR should be a design note that answers:

- Whether HTTP belongs in the SDK at all, or only in the future JDK 21 runtime.
- Which payload forms are protocol parsing concerns rather than runtime request
  handling concerns.
- Whether the SDK can support useful HTTP helpers without JSON, servlet,
  Netty, Spring, or HTTP client/server dependencies.

## Remaining Work Order

1. Run full release verification before tagging.

Completed hardening work includes audit refresh, raw-only catalog fixtures,
strict/permissive malformed-ASDU coverage, VSQ/SQ boundary fixtures, quality
descriptor fixtures, `CP56Time2a` edge-case fixtures, and final IEC104
README/API support-posture documentation. Release-readiness and release-note
drafts now exist for the `0.7.0` release candidate.

## Verification Gates

The release should not be tagged until these checks pass:

- `mvn -q -pl protocol-iec104 -am test`
- `mvn -q verify`
- JDK 8 `mvn -q verify`
- GitHub Actions on JDK 8 and JDK 21
- Central release profile smoke check with publishing disabled
- signed dry run with `central.skipPublishing=true`
- external Maven Central dependency resolution after publication

## Acceptance Criteria

This plan is complete when:

- `0.7.0` scope is clearly IEC104 hardening rather than runtime ingestion.
- Remaining IEC104 gaps are current, visible, and ordered.
- HTTP/runtime boundaries stay explicit.
- Release gates match the repository's Java 8 compatible Maven Central process.
