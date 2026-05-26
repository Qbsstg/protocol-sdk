# IEC101 ASDU Support Matrix

This document is the human-readable support matrix and completion audit for
`protocol-iec101`. The executable counterpart is the parser and value fixture
coverage under `protocol-iec101/src/test/java`.

## Support Levels

| Level | Meaning |
| --- | --- |
| Typed value | `Iec101InformationObject.getValue()` returns a typed `Iec101InformationValue` implementation. |
| Raw bytes only | The ASDU type is recognized and information element bytes are preserved, but `getValue()` returns `null`. |
| Unknown type | The type ID is not listed in `Iec101AsduType`; ASDU raw bytes remain available for diagnostics. |

Applications can query support status with:

```java
Iec101AsduSupport support = Iec101AsduSupport.ofTypeId(typeId);
if (support.hasTypedValue()) {
    Class<? extends Iec101InformationValue> valueClass = support.getValueClass();
} else if (support.isRawBytesOnly() || support.isUnknownType()) {
    // Read raw bytes from Iec101Asdu.getRawBytes() or
    // Iec101InformationObject.getElementBytes().
}
```

## Typed Value Coverage

| Type ID | ASDU type | Value class | Coverage |
| --- | --- | --- | --- |
| 1 | `M_SP_NA_1` | `Iec101SinglePointValue` | Single-point value and SIQ quality flags |
| 3 | `M_DP_NA_1` | `Iec101DoublePointValue` | Double-point state and DIQ quality flags |
| 9 | `M_ME_NA_1` | `Iec101MeasuredValue` | Normalized measured value and QDS quality flags |
| 11 | `M_ME_NB_1` | `Iec101MeasuredValue` | Scaled measured value and QDS quality flags |
| 13 | `M_ME_NC_1` | `Iec101MeasuredValue` | Short floating point measured value and QDS quality flags |
| 45 | `C_SC_NA_1` | `Iec101SingleCommandValue` | Single command state, select/execute bit, and command qualifier |
| 46 | `C_DC_NA_1` | `Iec101DoubleCommandValue` | Double command state, select/execute bit, and command qualifier |
| 100 | `C_IC_NA_1` | `Iec101InterrogationCommandValue` | Station and group interrogation qualifier |

## Recognized Raw-only Types

These Type IDs are listed in `Iec101AsduType` and classified by
`Iec101AsduSupport`, but the decoder intentionally exposes raw information bytes
only until typed IEC101-specific models are added.

| Type ID | ASDU type | Coverage |
| --- | --- | --- |
| 103 | `C_CS_NA_1` | Clock synchronization command; raw bytes are preserved until an IEC101 timestamp value model is added |

## Unknown Type Behavior

Unknown Type IDs are returned as `Iec101AsduType.UNKNOWN`. The decoder still
preserves ASDU raw bytes and information-object element bytes so callers can log
or handle vendor-specific payloads outside the SDK.

## Completion Audit Against `0.5.0` Gates

| Gate | Current status | Follow-up |
| --- | --- | --- |
| Single-character, fixed-length, and variable-length FT1.2 frames | Covered by parser fixtures for `0xE5`, fixed frames, variable frames, checksum validation, buffering, concatenation, and recovery. | Add more real-device traces when available. |
| Balanced and unbalanced control-field interpretation | Public model/configuration exists through `Iec101TransmissionMode`, `Iec101LinkControl`, and `Iec101LinkFunction`. | Add explicit balanced-mode function fixtures and document caller selection rules. |
| Configurable link, COT, common-address, and information-object address lengths | Existing fixtures cover one- and two-octet link addresses plus COT, common-address, and information-object address variants. | Add an explicit two-octet information-object address fixture. |
| ASDU support matrix | This document defines typed, raw-only, and unknown behavior for the current IEC101 catalog. | Keep this document aligned with `Iec101AsduType` and `Iec101AsduSupport`. |
| Time-tagged IEC101 values | Not complete. Current typed values cover non-time-tagged process values and commands. | Add IEC101-named CP24/CP56 time-tagged value classes and fixtures. |
| General interrogation, clock synchronization, and common commands | General interrogation, single command, and double command are typed. Clock synchronization is raw-only. | Promote `C_CS_NA_1` after timestamp modeling, and add more command/station-service variants. |
| Usage guide caller responsibilities | README states the module excludes serial handling, scheduling, retries, and runtime frameworks. | Expand into a dedicated API usage guide before `0.5.0`. |

## Fixture Gaps

- Information-object address length `2` fixture.
- Explicit balanced-mode control-field function fixtures.
- CP24/CP56 time-tagged process values.
- Typed `C_CS_NA_1` timestamp model.
- Additional command and station-service variants, including malformed command
  payloads.
- Wider raw-only or deferred standard Type ID catalog when enum constants are
  added beyond the current baseline.

## Maintenance Rule

When adding an enum constant to `Iec101AsduType`, update all three places in the
same commit:

- `Iec101AsduSupport`
- parser/value tests under `protocol-iec101/src/test/java`
- this document
