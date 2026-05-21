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
| 38 | `M_EP_TD_1` | `Iec104SingleProtectionEventValue` | Protection equipment event state, elapsed time, quality flags, CP56Time2a |
| 39 | `M_EP_TE_1` | `Iec104PackedStartEventsValue` | Packed start events, relay duration, QDP quality flags, CP56Time2a |
| 40 | `M_EP_TF_1` | `Iec104PackedOutputCircuitValue` | Packed output circuit information, relay operating time, QDP quality flags, CP56Time2a |
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
| 110 | `P_ME_NA_1` | `Iec104ParameterMeasuredValue` | Normalized measured parameter value and QPM qualifier |
| 111 | `P_ME_NB_1` | `Iec104ParameterMeasuredValue` | Scaled measured parameter value and QPM qualifier |
| 112 | `P_ME_NC_1` | `Iec104ParameterMeasuredValue` | Short floating point measured parameter value and QPM qualifier |
| 113 | `P_AC_NA_1` | `Iec104ParameterActivationValue` | Parameter activation qualifier |

## Recognized Raw-only Types

These Type IDs are listed in `Iec104AsduType` and classified by
`Iec104AsduSupport`, but the decoder intentionally exposes raw information bytes
only until typed models are justified by real integration demand.

| Type ID | ASDU type | Coverage |
| --- | --- | --- |
| 70 | `M_EI_NA_1` | End of initialization; information element bytes are preserved raw |
| 120 | `F_FR_NA_1` | File ready; information element bytes are preserved raw |
| 121 | `F_SR_NA_1` | Section ready; information element bytes are preserved raw |
| 122 | `F_SC_NA_1` | File call, select, and directory command; information element bytes are preserved raw |
| 123 | `F_LS_NA_1` | Last section or segment; information element bytes are preserved raw |
| 124 | `F_AF_NA_1` | File or section acknowledgement; information element bytes are preserved raw |
| 125 | `F_SG_NA_1` | File segment; information element bytes are preserved raw |
| 126 | `F_DR_TA_1` | File directory with CP56Time2a; information element bytes are preserved raw |

## Common ASDU Gaps

The remaining IEC104 gaps are behavior and documentation decisions rather than
missing support-matrix categories:

- Decide whether malformed recognized ASDUs should stay permissive or gain a
  strict diagnostic mode.
- Promote raw-only initialization or file-transfer entries to typed values only
  after real device traces or integration demand justify the public model.

## Maintenance Rule

When adding an enum constant to `Iec104AsduType`, update all three places in the
same commit:

- `Iec104AsduSupport`
- `Iec104AsduSupportMatrixTest`
- this document
