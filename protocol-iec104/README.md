# Protocol IEC104

`protocol-iec104` is the IEC60870-5-104 protocol module for Protocol SDK. It
parses APDU frames, ASDU headers, information object
addresses, raw information element bytes, and typed values for common monitoring
ASDU types.

## Supported Frame Levels

| Level | Current support |
| --- | --- |
| U-format | STARTDT, STOPDT, TESTFR activation/confirmation |
| S-format | Receive sequence parsing |
| I-format | Send/receive sequence parsing and ASDU parsing |
| ASDU header | Type ID, VSQ, cause, test flag, negative confirmation, originator, common address |
| Information objects | Sequential and non-sequential object addressing |

## Supported Typed Values

| ASDU type | Value class | Notes |
| --- | --- | --- |
| `M_SP_NA_1` | `Iec104SinglePointValue` | Single-point value and SIQ quality bits |
| `M_SP_TB_1` | `Iec104SinglePointValue` | Single-point value, SIQ quality bits, CP56Time2a |
| `M_DP_NA_1` | `Iec104DoublePointValue` | Double-point state and DIQ quality bits |
| `M_DP_TB_1` | `Iec104DoublePointValue` | Double-point state, DIQ quality bits, CP56Time2a |
| `M_ST_NA_1` | `Iec104StepPositionValue` | Step position value, transient flag, quality bits |
| `M_ST_TB_1` | `Iec104StepPositionValue` | Step position value, transient flag, quality bits, CP56Time2a |
| `M_BO_NA_1` | `Iec104BitstringValue` | Bitstring of 32 bits and quality bits |
| `M_BO_TB_1` | `Iec104BitstringValue` | Bitstring of 32 bits, quality bits, CP56Time2a |
| `M_ME_NA_1` | `Iec104MeasuredValue` | Normalized signed value, QDS quality bits |
| `M_ME_TD_1` | `Iec104MeasuredValue` | Normalized signed value, QDS quality bits, CP56Time2a |
| `M_ME_NB_1` | `Iec104MeasuredValue` | Scaled signed integer value, QDS quality bits |
| `M_ME_TE_1` | `Iec104MeasuredValue` | Scaled signed integer value, QDS quality bits, CP56Time2a |
| `M_ME_NC_1` | `Iec104MeasuredValue` | IEEE 754 short float value, QDS quality bits |
| `M_ME_TF_1` | `Iec104MeasuredValue` | IEEE 754 short float value, QDS quality bits, CP56Time2a |
| `M_ME_ND_1` | `Iec104MeasuredValue` | Normalized signed value without quality descriptor |
| `M_IT_NA_1` | `Iec104IntegratedTotalsValue` | Binary counter reading and sequence/quality flags |
| `M_IT_TB_1` | `Iec104IntegratedTotalsValue` | Binary counter reading, sequence/quality flags, CP56Time2a |
| `M_PS_NA_1` | `Iec104PackedSinglePointValue` | 16 single-point states, 16 change-detection bits, QDS quality bits |
| `M_EP_TD_1` | `Iec104SingleProtectionEventValue` | Protection equipment event state, elapsed time, quality bits, CP56Time2a |
| `M_EP_TE_1` | `Iec104PackedStartEventsValue` | Packed start events, relay duration, QDP quality bits, CP56Time2a |
| `M_EP_TF_1` | `Iec104PackedOutputCircuitValue` | Packed output circuit information, relay operating time, QDP quality bits, CP56Time2a |
| `C_SC_NA_1` | `Iec104SingleCommandValue` | Single command state, select/execute bit, command qualifier |
| `C_DC_NA_1` | `Iec104DoubleCommandValue` | Double command state, select/execute bit, command qualifier |
| `C_RC_NA_1` | `Iec104RegulatingStepCommandValue` | Regulating step command state, select/execute bit, command qualifier |
| `C_SE_NA_1` | `Iec104SetPointCommandValue` | Normalized set point value and set point qualifier |
| `C_SE_NB_1` | `Iec104SetPointCommandValue` | Scaled set point value and set point qualifier |
| `C_SE_NC_1` | `Iec104SetPointCommandValue` | IEEE 754 short float set point value and set point qualifier |
| `C_BO_NA_1` | `Iec104BitstringCommandValue` | Bitstring command and command qualifier |
| `C_SC_TA_1` | `Iec104SingleCommandValue` | Single command state, command qualifier, CP56Time2a |
| `C_DC_TA_1` | `Iec104DoubleCommandValue` | Double command state, command qualifier, CP56Time2a |
| `C_RC_TA_1` | `Iec104RegulatingStepCommandValue` | Regulating step command state, command qualifier, CP56Time2a |
| `C_SE_TA_1` | `Iec104SetPointCommandValue` | Normalized set point value, qualifier, CP56Time2a |
| `C_SE_TB_1` | `Iec104SetPointCommandValue` | Scaled set point value, qualifier, CP56Time2a |
| `C_SE_TC_1` | `Iec104SetPointCommandValue` | IEEE 754 short float set point value, qualifier, CP56Time2a |
| `C_BO_TA_1` | `Iec104BitstringCommandValue` | Bitstring command, qualifier, CP56Time2a |
| `C_IC_NA_1` | `Iec104InterrogationCommandValue` | Station or group interrogation qualifier |
| `C_CI_NA_1` | `Iec104CounterInterrogationCommandValue` | Counter interrogation request and freeze/reset qualifier |
| `C_RD_NA_1` | `Iec104ReadCommandValue` | Read command; target address is the information object address |
| `C_CS_NA_1` | `Iec104ClockSynchronizationCommandValue` | CP56Time2a clock synchronization value |
| `C_RP_NA_1` | `Iec104ResetProcessCommandValue` | Reset process qualifier |
| `C_CD_NA_1` | `Iec104DelayAcquisitionCommandValue` | CP16Time2a delay acquisition value in milliseconds |

Unknown or unsupported ASDU types still expose raw frame, ASDU, and information
object bytes. Their typed value is `null`.

For the detailed typed/raw-only/unsupported matrix, see
[`docs/asdu-support-matrix.md`](docs/asdu-support-matrix.md).

## Stream Decoder Example

```java
Iec104StreamDecoder decoder = new Iec104StreamDecoder();
List<ParseResult<Iec104Frame>> results = decoder.decode(frameBytes);

for (ParseResult<Iec104Frame> result : results) {
    if (!result.isSuccess()) {
        continue;
    }

    Iec104Frame frame = result.getFrame();
    if (frame.getAsdu() == null) {
        continue;
    }

    for (Iec104InformationObject object : frame.getAsdu().getInformationObjects()) {
        Iec104InformationValue value = object.getValue();
        if (value instanceof Iec104MeasuredValue) {
            double measured = ((Iec104MeasuredValue) value).getValue();
        }
    }
}
```

Support status can also be queried before casting typed values:

```java
Iec104AsduSupport support = Iec104AsduSupport.ofTypeId(frame.getAsdu().getTypeId());
if (support.hasTypedValue()) {
    Class<? extends Iec104InformationValue> valueClass = support.getValueClass();
}
```

For a complete executable example, see:

`src/test/java/io/github/qbsstg/protocol/iec104/Iec104SdkUsageExampleTest.java`

## Value Semantics

`Iec104MeasuredValue` uses the following conversion rules:

- Normalized value: signed little-endian 16-bit integer divided by `32768.0`.
- Scaled value: signed little-endian 16-bit integer.
- Short float value: little-endian IEEE 754 32-bit float.

`Iec104StepPositionValue` parses the 7-bit signed step position value and the
transient-state flag.

`Iec104BitstringValue` parses the 32-bit bitstring and exposes
`isBitSet(index)` for bit indices `0..31`.

`Iec104IntegratedTotalsValue` parses the 32-bit signed binary counter reading,
sequence number, carry, adjusted, and invalid flags.

`Iec104PackedSinglePointValue` parses the SCD field as 16 single-point state
bits and 16 status-change detection bits. Use `isOn(index)` and
`hasStatusChanged(index)` for point indices `0..15`.

Protection event values parse `M_EP_TD_1`, `M_EP_TE_1`, and `M_EP_TF_1`.
They expose CP16Time2a elapsed/relay time as milliseconds, CP56Time2a event
time, protection quality flags, and packed bit accessors for the packed forms.

`Iec104Cp56Time2a` parses local date-time fields only. It does not assign a
time zone because IEC104 CP56Time2a does not carry one.

`Iec104QualityDescriptor` exposes common quality flags:

- `invalid`
- `notTopical`
- `substituted`
- `blocked`
- `overflow` for measured values

`Iec104ProtectionQualityDescriptor` exposes protection-event quality flags:

- `invalid`
- `notTopical`
- `substituted`
- `blocked`
- `elapsedTimeInvalid`

Command values use `Iec104CommandQualifier`:

- Single and double commands parse the select/execute bit plus command qualifier.
- Regulating step commands parse lower/higher state plus select/execute bit and
  command qualifier.
- Set point commands parse the select/execute bit plus set point qualifier.
- Bitstring commands parse the 32-bit command value plus select/execute bit and
  command qualifier.
- Time-tagged command variants reuse the same value classes and expose
  CP56Time2a through `getTimeTag()`.
- `Iec104InterrogationCommandValue` maps qualifier `20` to station interrogation
  and qualifiers `21..36` to group `1..16`.
- `Iec104CounterInterrogationCommandValue` maps request qualifiers to counter
  groups and exposes freeze/reset mode.
- `Iec104ClockSynchronizationCommandValue` exposes the CP56Time2a clock value.
- `Iec104ResetProcessCommandValue` exposes the qualifier of reset process;
  qualifier `1` is general reset process and qualifier `2` is reset event buffer.
- `Iec104DelayAcquisitionCommandValue` exposes the CP16Time2a delay acquisition
  value as milliseconds.

## Current Non-goals

This module does not open TCP connections, manage Netty channels, reconnect
devices, schedule collection tasks, write databases, or publish messages. Those
belong to collector runtime modules, not the protocol SDK.
