# SDK Release Roadmap

This roadmap defines the SDK release direction after the published `0.6.0`
Maven Central release.

The working product decision is:

- `0.5.0` completed IEC101 and IEC103 as practical SDK modules and published
  Modbus as an experimental module.
- `0.6.0` promoted Modbus TCP/UDP parsing from experimental to a stable SDK
  module for the documented parser-only scope.
- `0.7.0` should harden the IEC104 parser and documentation before starting a
  new HTTP/runtime-oriented SDK surface.
- The future JDK 21 collector runtime remains separate from SDK releases.

## Version Intent

| Version | Intent | Release posture |
| --- | --- | --- |
| `0.1.x` | IEC104 public baseline and Maven Central readiness. | Published/stabilization patch line. |
| `0.2.0` | SDK consolidation after IEC101, IEC103, and Modbus baselines exist in the repository. | Experimental expansion release; do not claim IEC101/IEC103 completeness yet. |
| `0.3.0` | IEC101 hardening release. | Add fixtures, support matrix, malformed-frame behavior, time-tagged values, and usage docs. |
| `0.4.0` | IEC103 hardening release. | Add protection relay fixtures, support matrix, relay event coverage, and clearer raw-only boundaries. |
| `0.5.0` | IEC101 and IEC103 completion target. | Practical open-source SDK release for IEC101, IEC103, and existing IEC104 use cases. |
| `0.6.0` | Modbus stable completion. | Promote `protocol-modbus` from experimental to a stable TCP/UDP parser module for the documented parser-only scope. |
| `0.7.0` | IEC104 hardening. | Refresh the stale IEC104 audit, expand direct fixtures, document strict/permissive behavior, and keep runtime concerns out of the SDK. |

The `0.6.0` plan is tracked in
[`release-plan-0.6.0.md`](release-plan-0.6.0.md).

The `0.7.0` plan is tracked in
[`release-plan-0.7.0.md`](release-plan-0.7.0.md).

`0.6.0` should not mean every Modbus ecosystem corner case is complete. It
means the SDK has a defensible, documented, tested, and stable-enough parser
surface for common Modbus TCP and Modbus-over-UDP scenarios.

`0.7.0` should not mean formal IEC104 certification. It should mean the IEC104
SDK has a current audit, fixture-backed diagnostics, and clear public
documentation for practical integration use.

## Module Status Target

| Module | Current status | Next target |
| --- | --- | --- |
| `protocol-core` | Shared parser contracts. | Stable Java 8 API; no runtime dependencies. |
| `protocol-iec104` | Published typed parser with recognized raw-only catalog entries. | `0.7.0` hardening: current audit, raw-only fixtures, strict/permissive diagnostics, and documentation cleanup. |
| `protocol-iec101` | Published `0.5.0` completion target. | Compatibility maintenance and bug fixes. |
| `protocol-iec103` | Published `0.5.0` completion target. | Compatibility maintenance and bug fixes. |
| `protocol-modbus` | Published `0.6.0` stable TCP/UDP parser surface. | Compatibility maintenance and targeted function-code expansion. |
| `protocol-http` | Planned. | Keep planned until a design proves what belongs in the Java 8 SDK rather than the JDK 21 runtime. |

## `0.7.0` Release Gates

The `0.7.0` release should not be tagged until these conditions are true.

General SDK gates:

- `mvn -q verify` passes locally and in CI on JDK 8 and JDK 21.
- Public APIs remain Java 8 compatible.
- No SDK module depends on Spring, Netty, databases, Redis, MQTT, Kafka, HTTP
  server/client frameworks, or runtime globals.
- Maven Central metadata, source jars, Javadoc jars, signatures, and checksums
  remain valid.

IEC104 gates:

- The IEC104 completeness audit reflects the current post-`0.6.0` codebase.
- Recognized raw-only initialization and file-transfer Type IDs have direct
  fixture coverage for raw-byte preservation.
- Strict and permissive malformed ASDU behavior is documented and tested.
- VSQ/SQ boundary behavior is covered by tests or explicitly documented as
  permissive.
- API docs explain decoder statefulness, raw fallback, support matrix usage,
  and diagnostic cause handling.

## `0.6.0` Release Gates

The `0.6.0` release was tagged after these conditions were true.

General SDK gates:

- `mvn -q verify` passes locally and in CI on JDK 8 and JDK 21.
- Public APIs remain Java 8 compatible.
- No SDK module depends on Spring, Netty, databases, Redis, MQTT, Kafka, HTTP
  server frameworks, or runtime globals.
- Every parser module preserves raw bytes for diagnostics.
- Every limitation that remains after Modbus stabilization is documented before
  publishing.
- Maven Central metadata, source jars, Javadoc jars, signatures, and checksums
  remain valid.

Modbus gates:

- Support matrix exists for typed, raw-only, unknown, and deferred function
  codes.
- Usage guide covers TCP stream decoding, UDP datagram decoding,
  request/response correlation, exception responses, and raw fallback.
- Function codes `0x01`, `0x02`, `0x03`, `0x04`, `0x05`, `0x06`, `0x0F`, and
  `0x10` have typed request/response fixture coverage.
- Function code `0x17` is promoted to typed request/response parsing.
- Standard exception responses expose encoded function code, original function
  code, exception code, raw exception code, and raw bytes.
- TCP and UDP malformed-frame behavior is fixture-backed and documented.
- Standard quantity limits, byte counts, and PDU length validation are tested
  for typed function codes.
- Unknown and vendor-specific function codes preserve raw bytes when the ADU
  and PDU envelope is valid.

## Remaining `0.7.0` Work Order

1. Prepare and execute the `0.7.0` Maven Central release.

The IEC104 hardening work through final README/API support-posture
documentation, release-readiness documentation, and release-note drafting is
complete for the current `0.7.0` line.

## Maven Central Publishing Decision

The repository currently builds modules together under one parent version. The
`0.5.0` release published experimental `protocol-modbus` artifacts with clear
documentation. The `0.6.0` release removed that experimental label after the
selected Modbus gates passed.

The selected `0.6.0` policy is to keep publishing the full reactor as one
versioned SDK release. The release decision is about documentation and support
posture: `protocol-modbus` becomes stable only when the `0.6.0` gates pass.

## Runtime Relationship

The runtime roadmap does not block `0.6.0`.

`protocol-runtime` should consume released SDK artifacts, starting from the
latest stable SDK version that includes the parser module it needs. Runtime
work can proceed in parallel, but runtime dependencies must not enter SDK
modules.

## Release Notes Shape

`0.6.0` release notes should lead with:

- Modbus TCP/UDP stable parser scope and limitations.
- Function-code support matrix summary.
- TCP stream and UDP datagram decoder behavior.
- IEC101, IEC103, and IEC104 compatibility status.
- Java 8 compatibility and JDK 21 build verification.
- Maven Central dependency examples.

## Acceptance Criteria

This roadmap is complete when:

- `0.6.0` scope is clearly Modbus TCP/UDP stable completion.
- Modbus stability gates are explicit.
- Release gates identify what must be true before tagging.
- The Maven Central publishing policy decision is visible.
- Runtime work remains separate from SDK release gating.
