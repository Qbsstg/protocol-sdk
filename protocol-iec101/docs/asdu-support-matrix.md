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
| 2 | `M_SP_TA_1` | `Iec101SinglePointValue` | Single-point value, SIQ quality flags, CP24Time2a |
| 3 | `M_DP_NA_1` | `Iec101DoublePointValue` | Double-point state and DIQ quality flags |
| 4 | `M_DP_TA_1` | `Iec101DoublePointValue` | Double-point state, DIQ quality flags, CP24Time2a |
| 9 | `M_ME_NA_1` | `Iec101MeasuredValue` | Normalized measured value and QDS quality flags |
| 10 | `M_ME_TA_1` | `Iec101MeasuredValue` | Normalized measured value, QDS quality flags, CP24Time2a |
| 11 | `M_ME_NB_1` | `Iec101MeasuredValue` | Scaled measured value and QDS quality flags |
| 12 | `M_ME_TB_1` | `Iec101MeasuredValue` | Scaled measured value, QDS quality flags, CP24Time2a |
| 13 | `M_ME_NC_1` | `Iec101MeasuredValue` | Short floating point measured value and QDS quality flags |
| 14 | `M_ME_TC_1` | `Iec101MeasuredValue` | Short floating point measured value, QDS quality flags, CP24Time2a |
| 30 | `M_SP_TB_1` | `Iec101SinglePointValue` | Single-point value, SIQ quality flags, CP56Time2a |
| 31 | `M_DP_TB_1` | `Iec101DoublePointValue` | Double-point state, DIQ quality flags, CP56Time2a |
| 34 | `M_ME_TD_1` | `Iec101MeasuredValue` | Normalized measured value, QDS quality flags, CP56Time2a |
| 35 | `M_ME_TE_1` | `Iec101MeasuredValue` | Scaled measured value, QDS quality flags, CP56Time2a |
| 36 | `M_ME_TF_1` | `Iec101MeasuredValue` | Short floating point measured value, QDS quality flags, CP56Time2a |
| 45 | `C_SC_NA_1` | `Iec101SingleCommandValue` | Single command state, select/execute bit, and command qualifier |
| 46 | `C_DC_NA_1` | `Iec101DoubleCommandValue` | Double command state, select/execute bit, and command qualifier |
| 100 | `C_IC_NA_1` | `Iec101InterrogationCommandValue` | Station and group interrogation qualifier |
| 101 | `C_CI_NA_1` | `Iec101CounterInterrogationCommandValue` | Counter interrogation qualifier and freeze/reset mode |
| 102 | `C_RD_NA_1` | `Iec101ReadCommandValue` | Read command; target address is the information object address |
| 103 | `C_CS_NA_1` | `Iec101ClockSynchronizationCommandValue` | Clock synchronization command with CP56Time2a |
| 105 | `C_RP_NA_1` | `Iec101ResetProcessCommandValue` | Reset process qualifier |
| 106 | `C_CD_NA_1` | `Iec101DelayAcquisitionCommandValue` | CP16Time2a delay acquisition value in milliseconds |

## Recognized Raw-only Types

No Type IDs are currently classified as recognized raw-only in the selected
IEC101 catalog. Deferred Type IDs remain unknown to the decoder until they are
added to `Iec101AsduType`, so ASDU raw bytes remain available for diagnostics.

## Deferred Time-tagged Types

These standard time-tagged Type IDs are intentionally deferred until the
corresponding non-time-tagged value model is added or real integration demand
justifies the public API.

| Type IDs | ASDU types | Reason |
| --- | --- | --- |
| 6, 8, 16 | `M_ST_TA_1`, `M_BO_TA_1`, `M_IT_TA_1` | Step position, bitstring, and integrated totals are not part of the IEC101 typed baseline yet. |
| 17, 18, 19 | `M_EP_TA_1`, `M_EP_TB_1`, `M_EP_TC_1` | Protection event payloads should be designed with IEC103/IEC104 event model reuse in mind. |
| 32, 33, 37 | `M_ST_TB_1`, `M_BO_TB_1`, `M_IT_TB_1` | CP56 variants of deferred value models. |
| 38, 39, 40 | `M_EP_TD_1`, `M_EP_TE_1`, `M_EP_TF_1` | CP56 protection event variants deferred with the protection event model. |
| 47-51 | `C_RC_NA_1`, `C_SE_NA_1`, `C_SE_NB_1`, `C_SE_NC_1`, `C_BO_NA_1` | Regulating step, set point, and bitstring commands are deferred until their value semantics are modeled. |
| 58-64 | Time-tagged command variants | Deferred until the matching non-time-tagged command models are added. |

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
| Time-tagged IEC101 values | Partially complete. Selected single-point, double-point, normalized/scaled/short-float measured values support CP24Time2a and CP56Time2a through IEC101-named timestamp classes. | Add deferred step, bitstring, integrated totals, protection event, and time-tagged command variants when their base public models are ready. |
| General interrogation, clock synchronization, and common commands | Single command, double command, interrogation, counter interrogation, read, clock synchronization, reset process, and delay acquisition are typed and fixture-backed. | Add deferred regulating, set point, bitstring, and time-tagged command variants when their base public models are ready. |
| Usage guide caller responsibilities | README states the module excludes serial handling, scheduling, retries, and runtime frameworks. | Expand into a dedicated API usage guide before `0.5.0`. |

## Fixture Gaps

- Information-object address length `2` fixture.
- Explicit balanced-mode control-field function fixtures.
- Deferred CP24/CP56 step, bitstring, integrated totals, and protection event
  variants.
- Regulating step, set point, bitstring, and time-tagged command variants.
- Additional malformed station-service payload variants as real traces justify
  them.
- Wider raw-only or deferred standard Type ID catalog when enum constants are
  added beyond the current baseline.

## Maintenance Rule

When adding an enum constant to `Iec101AsduType`, update all three places in the
same commit:

- `Iec101AsduSupport`
- parser/value tests under `protocol-iec101/src/test/java`
- this document
