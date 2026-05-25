# IEC104 Completeness Audit

This audit records the current `protocol-iec104` completeness state after the
`0.1.0` Maven Central release. It is intentionally scoped to the SDK's current
code, tests, and documentation. It does not claim full IEC 60870-5-104
conformance; formal conformance should still be checked against the licensed
standard text and real device traces.

## Audit Snapshot

| Area | Current state | Risk |
| --- | --- | --- |
| APDU framing | I-format, S-format, and U-format are decoded. STARTDT, STOPDT, and TESTFR U-format values are mapped. | Unknown U-format control values are represented as `UNKNOWN_U_FORMAT`; no state machine behavior is implemented. |
| Stream handling | The decoder buffers incomplete APDUs, skips noise before `0x68`, and can decode concatenated APDUs. | Malformed recognized ASDUs with truncated information objects are currently handled permissively by returning the objects that can be parsed. |
| ASDU catalog | 45 Type IDs are recognized by `Iec104AsduType`; all recognized types are classified as typed values by `Iec104AsduSupport`. | Type IDs outside the current enum are preserved as raw bytes, but some standard catalog types are not modeled yet. |
| Information objects | Three-octet IEC104 information object addresses are decoded for single and sequence layouts. | There are no explicit tests for invalid SQ usage by type; the decoder is intentionally permissive. |
| Cause of transmission | Codes 1-13 and 20-36 are modeled, with test and negative-confirm bits exposed separately. | Counter-interrogation return causes 37-41 and unknown-* causes 44-47 are not enum constants yet. Raw cause code is preserved. |
| Quality descriptors | Status/measurement quality flags and protection quality flags are modeled. | More fixture coverage is needed to prove every quality flag across every value family. |
| Time tags | `CP56Time2a` exposes raw bytes, invalid flag, summer time flag, date parts, and `LocalDateTime` when valid. | More direct tests are needed for time-tagged measured and double-point variants. |
| Raw bytes | Raw APDU, ASDU, and information-element bytes are preserved. | This is useful for diagnostics, but unsupported known catalog entries should be made explicit in docs. |

## Current Typed ASDU Coverage

The current support matrix is executable and documented:

- `Iec104AsduSupportMatrixTest` requires every known `Iec104AsduType` to be
  categorized.
- `protocol-iec104/docs/asdu-support-matrix.md` lists all 45 typed Type IDs.
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
| Boundary behavior | Negative numeric values, double-point state mapping, invalid CP56Time2a, unknown ASDU raw-byte preservation, packed bit index bounds. |

## Gaps and Follow-up Work

### G1. Add explicit tests for shared time-tagged variants

Some Type IDs are decoded through shared parser methods and are present in the
support matrix, but do not yet have direct frame-level tests. This is not a
known implementation bug, but it weakens regression confidence.

Priority direct tests:

- `M_DP_TB_1`
- `M_ME_TD_1`
- `M_ME_TE_1`
- `M_ME_TF_1`

Track under issue #10, which already covers reusable fixtures and regression
tests.

### G2. Expand cause-of-transmission enum coverage

`Iec104Asdu` preserves the raw cause code, but
`Iec104CauseOfTransmission.fromCode()` currently maps only codes 1-13 and
20-36 to named enum values.

Add enum constants and tests for:

- Counter interrogation return causes: 37-41.
- Unknown type/cause/common-address/information-object-address causes: 44-47.

This is a field-level completeness gap and should be addressed before calling
the IEC104 SDK "complete" for common integration diagnostics.

### G3. Decide scope for initialization and file-transfer Type IDs

The practical 0.1.0 parser does not model the IEC initialization/file-transfer
catalog entries, such as:

- `M_EI_NA_1` end of initialization, Type ID 70.
- File transfer Type IDs commonly listed in the 120-126 range.

Recommended handling for v0.2.0:

- Add them as explicit raw-only entries first if typed semantics are not needed.
- Promote to typed values only when real device traces or integration demand
  justify the model design.
- Keep unknown Type ID raw-byte preservation as the fallback for vendor-specific
  extensions.

### G4. Decide strictness for malformed recognized ASDUs

For recognized ASDU types, the decoder currently stops parsing information
objects when there are not enough bytes for the next object. It does not emit a
`ParseResult.error()` for that malformed ASDU.

This is acceptable for a permissive stream decoder, but users may need stricter
diagnostics. v0.2.0 should decide between:

- Preserve current permissive behavior and document it.
- Add a strict mode that returns errors for truncated information objects.
- Add metadata that records expected vs actual object count without breaking
  current behavior.

### G5. Document API expectations more explicitly

Issue #11 should cover public documentation for:

- Thread-safety: `Iec104StreamDecoder` is stateful because it buffers partial
  input; callers should use one decoder per stream/session.
- Raw-byte fallback behavior.
- `Iec104AsduSupport` usage.
- Difference between stable Maven Central version `0.1.0` and `main`
  development version.

## Recommended v0.2.0 Order

1. Add direct frame-level tests for `M_DP_TB_1` and `M_ME_TD_1` /
   `M_ME_TE_1` / `M_ME_TF_1`.
2. Expand `Iec104CauseOfTransmission` with codes 37-41 and 44-47 plus tests.
3. Decide whether Type IDs 70 and 120-126 should be raw-only or typed in
   `Iec104AsduSupport`.
4. Decide strict vs permissive malformed ASDU behavior.
5. Update README and module docs after the behavior decisions are implemented.

## Verification

Current verification command:

```bash
mvn -q verify
```

At the time of this audit, the project passes the full Maven verification suite
with 58 tests.
