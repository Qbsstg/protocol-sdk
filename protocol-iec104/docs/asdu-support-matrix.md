# IEC104 ASDU Support Matrix

This document is the human-readable support matrix for
`protocol-iec104`. The executable counterpart is
`Iec104AsduSupportMatrixTest`, which fails when a known `Iec104AsduType` is not
categorized.

## Support Levels

| Level | Meaning |
| --- | --- |
| Typed value | `Iec104InformationObject.getValue()` returns a typed `Iec104InformationValue` implementation. |
| Raw bytes only | The ASDU type is recognized and information element bytes are preserved, but `getValue()` returns `null`. |
| Unknown type | The type ID is not listed in `Iec104AsduType`; ASDU raw bytes remain available for diagnostics. |

Applications can query support status with:

```java
Iec104AsduSupport support = Iec104AsduSupport.ofTypeId(typeId);
if (support.hasTypedValue()) {
    Class<? extends Iec104InformationValue> valueClass = support.getValueClass();
}
```

## Typed Value Coverage

| Type ID | ASDU type | Value class | Coverage |
| --- | --- | --- | --- |
| 1 | `M_SP_NA_1` | `Iec104SinglePointValue` | Single-point value and SIQ quality flags |
| 3 | `M_DP_NA_1` | `Iec104DoublePointValue` | Double-point state and DIQ quality flags |
| 5 | `M_ST_NA_1` | `Iec104StepPositionValue` | Step position value, transient flag, quality flags |
| 7 | `M_BO_NA_1` | `Iec104BitstringValue` | Bitstring of 32 bits and quality flags |
| 9 | `M_ME_NA_1` | `Iec104MeasuredValue` | Normalized measured value and QDS quality flags |
| 11 | `M_ME_NB_1` | `Iec104MeasuredValue` | Scaled measured value and QDS quality flags |
| 13 | `M_ME_NC_1` | `Iec104MeasuredValue` | Short floating point measured value and QDS quality flags |
| 15 | `M_IT_NA_1` | `Iec104IntegratedTotalsValue` | Binary counter reading and sequence/quality flags |
| 20 | `M_PS_NA_1` | `Iec104PackedSinglePointValue` | 16 single-point states, 16 change-detection bits, QDS quality flags |
| 21 | `M_ME_ND_1` | `Iec104MeasuredValue` | Normalized measured value without quality descriptor |
| 30 | `M_SP_TB_1` | `Iec104SinglePointValue` | Single-point SOE value, SIQ quality flags, CP56Time2a |
| 31 | `M_DP_TB_1` | `Iec104DoublePointValue` | Double-point SOE value, DIQ quality flags, CP56Time2a |
| 32 | `M_ST_TB_1` | `Iec104StepPositionValue` | Step position value, transient flag, quality flags, CP56Time2a |
| 33 | `M_BO_TB_1` | `Iec104BitstringValue` | Bitstring of 32 bits, quality flags, CP56Time2a |
| 34 | `M_ME_TD_1` | `Iec104MeasuredValue` | Normalized measured value, QDS quality flags, CP56Time2a |
| 35 | `M_ME_TE_1` | `Iec104MeasuredValue` | Scaled measured value, QDS quality flags, CP56Time2a |
| 36 | `M_ME_TF_1` | `Iec104MeasuredValue` | Short floating point measured value, QDS quality flags, CP56Time2a |
| 37 | `M_IT_TB_1` | `Iec104IntegratedTotalsValue` | Binary counter reading, sequence/quality flags, CP56Time2a |
| 45 | `C_SC_NA_1` | `Iec104SingleCommandValue` | Single command state, select/execute bit, command qualifier |
| 46 | `C_DC_NA_1` | `Iec104DoubleCommandValue` | Double command state, select/execute bit, command qualifier |
| 47 | `C_RC_NA_1` | `Iec104RegulatingStepCommandValue` | Regulating step command state, select/execute bit, command qualifier |
| 48 | `C_SE_NA_1` | `Iec104SetPointCommandValue` | Normalized set point value and set point qualifier |
| 49 | `C_SE_NB_1` | `Iec104SetPointCommandValue` | Scaled set point value and set point qualifier |
| 50 | `C_SE_NC_1` | `Iec104SetPointCommandValue` | Short floating point set point value and set point qualifier |
| 51 | `C_BO_NA_1` | `Iec104BitstringCommandValue` | Bitstring command and command qualifier |
| 58 | `C_SC_TA_1` | `Iec104SingleCommandValue` | Single command state, command qualifier, CP56Time2a |
| 59 | `C_DC_TA_1` | `Iec104DoubleCommandValue` | Double command state, command qualifier, CP56Time2a |
| 60 | `C_RC_TA_1` | `Iec104RegulatingStepCommandValue` | Regulating step command state, command qualifier, CP56Time2a |
| 61 | `C_SE_TA_1` | `Iec104SetPointCommandValue` | Normalized set point value, set point qualifier, CP56Time2a |
| 62 | `C_SE_TB_1` | `Iec104SetPointCommandValue` | Scaled set point value, set point qualifier, CP56Time2a |
| 63 | `C_SE_TC_1` | `Iec104SetPointCommandValue` | Short floating point set point value, set point qualifier, CP56Time2a |
| 64 | `C_BO_TA_1` | `Iec104BitstringCommandValue` | Bitstring command, command qualifier, CP56Time2a |
| 100 | `C_IC_NA_1` | `Iec104InterrogationCommandValue` | Station and group interrogation qualifier |
| 101 | `C_CI_NA_1` | `Iec104CounterInterrogationCommandValue` | Counter interrogation request and freeze/reset qualifier |
| 102 | `C_RD_NA_1` | `Iec104ReadCommandValue` | Read command; target address is the information object address |
| 103 | `C_CS_NA_1` | `Iec104ClockSynchronizationCommandValue` | CP56Time2a clock synchronization value |
| 105 | `C_RP_NA_1` | `Iec104ResetProcessCommandValue` | Reset process qualifier |
| 106 | `C_CD_NA_1` | `Iec104DelayAcquisitionCommandValue` | CP16Time2a delay acquisition value in milliseconds |

## Recognized Raw-only Types

There are no raw-only ASDU types in the current `Iec104AsduType` enum. Every
recognized ASDU type currently returns a typed value from
`Iec104InformationObject.getValue()`.

## Common ASDU Gaps

The following common IEC104 type IDs are not yet listed in `Iec104AsduType`.
They are useful candidates for future work. This is a practical backlog, not a
full IEC60870-5 catalog.

| Type ID | ASDU type | Description | Suggested priority |
| --- | --- | --- | --- |
| 38 | `M_EP_TD_1` | Event of protection equipment with CP56Time2a | Low |
| 39 | `M_EP_TE_1` | Packed start events of protection equipment with CP56Time2a | Low |
| 40 | `M_EP_TF_1` | Packed output circuit information with CP56Time2a | Low |
| 110 | `P_ME_NA_1` | Parameter of measured normalized value | Low |
| 111 | `P_ME_NB_1` | Parameter of measured scaled value | Low |
| 112 | `P_ME_NC_1` | Parameter of measured short floating point value | Low |
| 113 | `P_AC_NA_1` | Parameter activation | Low |

## Maintenance Rule

When adding an enum constant to `Iec104AsduType`, update all three places in the
same commit:

- `Iec104AsduSupport`
- `Iec104AsduSupportMatrixTest`
- this document
