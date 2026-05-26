# SDK Release Roadmap

This roadmap defines the next SDK release direction after the initial public
`0.1.0` Maven Central release and the first parser baselines for IEC101,
IEC103, and Modbus.

The working product decision is:

- `0.5.0` focuses on completing IEC101 and IEC103 as practical SDK modules.
- Modbus remains available as an experimental module before `0.5.0`, but it is
  not a `0.5.0` completion gate.
- Modbus stable completion moves to the next major SDK phase after `0.5.0`.
- The future JDK 21 collector runtime remains separate from SDK releases.

## Version Intent

| Version | Intent | Release posture |
| --- | --- | --- |
| `0.1.x` | IEC104 public baseline and Maven Central readiness. | Published/stabilization patch line. |
| `0.2.0` | SDK consolidation after IEC101, IEC103, and Modbus baselines exist in the repository. | Experimental expansion release; do not claim IEC101/IEC103 completeness yet. |
| `0.3.0` | IEC101 hardening release. | Add fixtures, support matrix, malformed-frame behavior, time-tagged values, and usage docs. |
| `0.4.0` | IEC103 hardening release. | Add protection relay fixtures, support matrix, relay event coverage, and clearer raw-only boundaries. |
| `0.5.0` | IEC101 and IEC103 completion target. | Practical open-source SDK release for IEC101, IEC103, and existing IEC104 use cases. |
| Next major phase | Modbus stable completion and broader runtime-facing integration polish. | Modbus becomes a release gate after IEC101/IEC103 are complete. |

`0.5.0` does not mean every standard corner case is complete. It means the SDK
has a defensible, documented, tested, and stable-enough parser surface for the
common IEC101 and IEC103 scenarios this project wants to support publicly.

## Module Status Target

| Module | Current status | `0.5.0` target |
| --- | --- | --- |
| `protocol-core` | Shared parser contracts. | Stable Java 8 API; no runtime dependencies. |
| `protocol-iec104` | Published typed parser. | Maintain compatibility; only add fixes and documented gap closures needed by IEC101/IEC103 reuse decisions. |
| `protocol-iec101` | Experimental FT1.2 and initial ASDU baseline. | Complete enough for common IEC101 link-frame and ASDU parsing scenarios. |
| `protocol-iec103` | Experimental FT1.2 and protection relay baseline. | Complete enough for common protection relay event, measurand, identification, and raw fallback scenarios. |
| `protocol-modbus` | Experimental TCP/UDP baseline. | Keep experimental; preserve tests and docs, but do not block `0.5.0`. |
| `protocol-http` | Planned. | Keep planned unless needed by runtime docs; do not block `0.5.0`. |

## `0.5.0` Release Gates

The release should not be tagged until these conditions are true.

General SDK gates:

- `mvn -q verify` passes locally and in CI on JDK 8 and JDK 21.
- Public APIs remain Java 8 compatible.
- No SDK module depends on Spring, Netty, databases, Redis, MQTT, Kafka, HTTP
  server frameworks, or runtime globals.
- Every parser module preserves raw bytes for diagnostics.
- Every experimental limitation is documented before publishing.
- Maven Central metadata, source jars, Javadoc jars, signatures, and checksums
  remain valid.

IEC101 gates:

- Link-frame parsing covers single-character, fixed-length, and variable-length
  FT1.2 frames with checksum validation, buffering, concatenation, and recovery.
- Balanced and unbalanced control-field interpretation is documented and
  fixture-backed.
- Configurable link, COT, common-address, and information-object address
  lengths have tests.
- ASDU support matrix exists for typed, raw-only, and unknown Type IDs.
- Time-tagged IEC101 values are modeled with IEC101-named public classes.
- General interrogation, clock synchronization, and common command paths are
  either typed or explicitly raw-only with tests.
- Usage guide includes serial/TCP-bridge caller responsibilities without adding
  runtime dependencies.

IEC103 gates:

- FT1.2 frame parsing covers common relay links with checksum validation,
  buffering, concatenation, max-frame limits, and recovery tests.
- ASDU header parsing exposes Type ID, VSQ, COT, common address, `FUN`, and
  `INF`.
- Protection event values cover time-tagged messages and relative-time event
  payloads with quality, fault number, and time tag metadata.
- Measurands I and II have typed value tests and documented conversion rules.
- Identification payloads preserve raw bytes and expose stable public fields
  where safe.
- Generic data, disturbance records, and vendor-specific Type IDs are
  classified as typed, raw-only, or unknown.
- Support matrix and usage guide clearly describe what is complete, raw-only,
  and deferred.

Modbus gates before `0.5.0`:

- Keep existing tests passing.
- Keep README status as experimental.
- Do not require Modbus support matrix completeness for `0.5.0`.
- Do not block IEC101/IEC103 release work on Modbus stable coverage.

## Recommended Work Order

1. Add `0.5.0` milestone issues for IEC101 completion.
2. Add `0.5.0` milestone issues for IEC103 completion.
3. Add support matrices for IEC101 and IEC103.
4. Add time-tagged IEC101 model and fixtures.
5. Expand IEC103 protection event fixtures and measured value coverage.
6. Audit docs so every module status matches actual parser behavior.
7. Run full release readiness checks without publishing.
8. Decide whether `0.5.0` publishes all modules or publishes only modules that
   should be consumed externally.
9. Prepare and execute the `0.5.0` Maven Central release.
10. Create the next major phase backlog for Modbus stable completion.

## Maven Central Publishing Decision

The repository currently builds modules together under one parent version. That
means a `0.5.0` release can publish experimental artifacts such as
`protocol-modbus` even if Modbus is not a stability gate.

Before the actual `0.5.0` release, choose one policy:

| Policy | Effect |
| --- | --- |
| Publish all modules with clear experimental labels | Simpler Maven release; Modbus artifact exists at `0.5.0` but docs say it is experimental. |
| Exclude experimental modules from publishing | Cleaner external signal, but requires Maven release profile work and more release complexity. |

The selected `0.5.0` readiness policy is to publish all current reactor modules
and mark Modbus experimental in README, module docs, and release notes. This
keeps the Maven release simple and makes Modbus available for early adopters
without turning it into a `0.5.0` stability gate.

If that feels misleading later, add a release profile that skips experimental
modules before tagging a future release.

## Runtime Relationship

The runtime roadmap does not block `0.5.0`.

`protocol-runtime` should consume released SDK artifacts, starting from the
latest stable SDK version that includes the parser module it needs. Runtime
work can proceed in parallel, but runtime dependencies must not enter SDK
modules.

## Release Notes Shape

`0.5.0` release notes should lead with:

- IEC101 parser completion scope and limitations.
- IEC103 parser completion scope and limitations.
- IEC104 compatibility status.
- Modbus experimental status and next major phase plan.
- Java 8 compatibility and JDK 21 build verification.
- Maven Central dependency examples per stable or experimental module.

## Acceptance Criteria

This roadmap is complete when:

- `0.5.0` scope is clearly IEC101 and IEC103 focused.
- Modbus is explicitly deferred from `0.5.0` completion gates.
- Release gates identify what must be true before tagging.
- The Maven Central publishing policy decision is visible.
- Runtime work remains separate from SDK release gating.
