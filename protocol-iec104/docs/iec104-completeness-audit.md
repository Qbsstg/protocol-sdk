# IEC104 Completeness Audit

This audit records the current `protocol-iec104` completeness state after the
published `0.6.0` SDK release. It is intentionally scoped to the SDK's current
code, tests, and documentation. It does not claim full IEC 60870-5-104
conformance; formal conformance should still be checked against the licensed
standard text and real device traces.

## Audit Snapshot

| Area | Current state | Risk |
| --- | --- | --- |
| APDU framing | I-format, S-format, and U-format are decoded. STARTDT, STOPDT, and TESTFR U-format values are mapped. | Unknown U-format control values are represented as `UNKNOWN_U_FORMAT`; no session state machine behavior is implemented. |
| Stream handling | The decoder buffers incomplete APDUs, skips noise before `0x68`, and can decode concatenated APDUs. | Session lifecycle, reconnect, heartbeat policy, and flow control remain runtime concerns. |
| ASDU catalog | 45 Type IDs are typed values and 8 recognized Type IDs are raw-only catalog entries. The support matrix is executable through `Iec104AsduSupportMatrixTest`, and raw-only catalog entries have direct parser fixtures. | Raw-only initialization and file-transfer entries should stay raw-only until real traces justify typed models. |
| Information objects | Three-octet IEC104 information object addresses are decoded for single and sequence layouts. Representative VSQ/SQ boundary fixtures cover typed sequence, typed non-sequence, and raw-only sequence behavior. | Additional fixtures may still be useful for rare type-family-specific SQ constraints. |
| Cause of transmission | Codes 1-13, 20-41, and 44-47 are modeled, with test and negative-confirm bits exposed separately. | Raw cause code preservation should remain part of diagnostic regression coverage. |
| Quality descriptors | Status/measurement quality flags and protection quality flags are modeled. | More fixture coverage is useful to prove every quality flag across every value family. |
| Time tags | `CP56Time2a` exposes raw bytes, invalid flag, summer time flag, date parts, and `LocalDateTime` when valid. | Additional edge-case fixtures should cover invalid dates and boundary values across value families. |
| Raw bytes | Raw APDU, ASDU, and information-element bytes are preserved for typed, raw-only, and unknown cases. | Raw-only catalog coverage should stay explicit so unsupported entries do not look silently complete. |

## Current Typed ASDU Coverage

The current support matrix is executable and documented:

- `Iec104AsduSupportMatrixTest` requires every known `Iec104AsduType` to be
  categorized.
- `protocol-iec104/docs/asdu-support-matrix.md` lists all 45 typed Type IDs
  and 8 recognized raw-only Type IDs.
- Unknown Type IDs are decoded as `Iec104AsduType.UNKNOWN`; raw ASDU and
  information-element bytes remain available.

Current groups with typed models:

- Process information: single point, double point, step position, bitstring,
  measured values, integrated totals, packed single point, protection events.
- Time-tagged process information: single point, double point, step position,
  bitstring, measured values, integrated totals, protection events.
- Commands: single, double, regulating step, set point, bitstring,
  interrogation, counter interrogation, read, clock synchronization, reset
  process, delay acquisition.
- Time-tagged commands: single, double, regulating step, set point, bitstring.
- Parameters: normalized/scaled/short-float measured parameters and parameter
  activation.

## Direct Test Coverage

The test suite has good coverage for parser mechanics and most value families:

| Test area | Covered examples |
| --- | --- |
| APDU mechanics | U-format mapping, S-format receive sequence, I-format sequence numbers, concatenated APDUs, split TCP payloads, noise recovery, invalid APDU length recovery. |
| ASDU header | Type ID, VSQ, cause code, test/negative bits, originator address, common address, sequence object addressing. |
| Process values | Single point, double point, step position, bitstring, normalized/scaled/short-float measured values, integrated totals. |
| Time-tagged process values | Single point SOE, step position, bitstring, integrated totals, protection events. |
| Commands | Single, double, regulating step, set point, bitstring, interrogation, counter interrogation, read, clock synchronization, reset process, delay acquisition. |
| Time-tagged commands | Single, double, regulating step, set point, bitstring. |
| Parameters | Normalized/scaled/short-float measured parameters, parameter activation, sequential parameters. |
| Boundary behavior | Negative numeric values, double-point state mapping, invalid CP56Time2a, unknown ASDU raw-byte preservation, VSQ/SQ addressing, packed bit index bounds. |
| Raw-only catalog | `M_EI_NA_1` and file-transfer Type IDs `120` through `126` preserve raw information bytes without typed values. |

## Gaps and Follow-up Work

### G1. Decide typed scope for raw-only catalog entries

`Iec104AsduSupport` and the support matrix classify `M_EI_NA_1` and
file-transfer Type IDs `120` through `126` as recognized raw-only entries.
Direct parser fixtures now cover raw-byte preservation for every raw-only
catalog entry.

Recommended `0.7.0` handling:

- Keep these entries raw-only unless real device traces justify typed public
  models.
- Promote a raw-only entry to a typed model only with a focused design note and
  fixture set for that public model.

### G2. Decide stricter VSQ/SQ validation scope

The decoder supports single and sequence information-object layouts and is
intentionally permissive in several edge cases. Direct fixtures now lock down
representative typed sequence, typed non-sequence, and raw-only sequence
behavior.

Remaining `0.7.0` decision:

- Keep SQ permissive for type families where sequence addressing is uncommon or
  not useful.
- Add stricter validation only if real traces or integration failures show that
  current permissive parsing hides actionable diagnostics.

### G3. Keep malformed-ASDU strictness documented

The default decoder preserves permissive behavior for truncated recognized
ASDUs, while the strict constructor can reject truncated information elements
with `ParseResult.error()`.

`0.7.0` should make this behavior visible in README/API docs and extend the
fixtures where needed so strict mode remains predictable.

### G4. Broaden quality and time-tag edge-case coverage

Status, measurement, and protection quality descriptors expose their raw bytes
and named flags. `CP56Time2a` exposes raw bytes, invalid flag, summer time flag,
date parts, and `LocalDateTime` when the value is valid.

More direct fixtures should cover:

- Every quality flag for representative process, measured, and protection
  value families.
- Invalid `CP56Time2a` dates and boundary values.
- Raw-byte preservation for time-tagged values when the parsed date is invalid.

### G5. Document API expectations more explicitly

Public documentation should cover:

- Thread-safety: `Iec104StreamDecoder` is stateful because it buffers partial
  input; callers should use one decoder per stream/session.
- Strict versus permissive malformed ASDU behavior.
- Raw-byte fallback behavior.
- `Iec104AsduSupport` usage.
- Difference between parser SDK behavior and runtime session behavior.

## Completed Since The Original Audit

- Direct tests now cover time-tagged double-point and measured variants:
  `M_DP_TB_1`, `M_ME_TD_1`, `M_ME_TE_1`, and `M_ME_TF_1`.
- `Iec104CauseOfTransmission` now names counter-interrogation return causes
  `37` through `41` and diagnostic unknown causes `44` through `47`.
- `M_EI_NA_1` and file-transfer Type IDs `120` through `126` are recognized
  raw-only support-matrix entries.
- Direct fixtures now cover raw-byte preservation for every recognized
  raw-only catalog entry.
- Direct fixtures now cover representative VSQ/SQ typed and raw-only boundary
  behavior.
- Strict malformed ASDU mode exists for truncated information elements while
  the default decoder remains permissive.

## Recommended `0.7.0` Order

1. Expand strict malformed-ASDU fixtures where diagnostics are still thin.
2. Add quality descriptor and `CP56Time2a` edge-case fixtures.
3. Update README and API docs with the final behavior decisions.

## Verification

Current verification command:

```bash
mvn -q -pl protocol-iec104 -am test
```
