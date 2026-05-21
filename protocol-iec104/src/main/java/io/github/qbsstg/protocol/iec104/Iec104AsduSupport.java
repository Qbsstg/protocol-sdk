package io.github.qbsstg.protocol.iec104;

public final class Iec104AsduSupport {

    private final Iec104AsduType asduType;
    private final Iec104AsduSupportStatus status;
    private final Class<? extends Iec104InformationValue> valueClass;
    private final String note;

    private Iec104AsduSupport(Iec104AsduType asduType, Iec104AsduSupportStatus status,
                              Class<? extends Iec104InformationValue> valueClass, String note) {
        this.asduType = asduType;
        this.status = status;
        this.valueClass = valueClass;
        this.note = note;
    }

    public static Iec104AsduSupport ofTypeId(int typeId) {
        return of(Iec104AsduType.fromTypeId(typeId));
    }

    public static Iec104AsduSupport of(Iec104AsduType asduType) {
        if (asduType == null) {
            return unknown();
        }

        switch (asduType) {
            case M_SP_NA_1:
            case M_SP_TB_1:
                return typed(asduType, Iec104SinglePointValue.class);
            case M_DP_NA_1:
            case M_DP_TB_1:
                return typed(asduType, Iec104DoublePointValue.class);
            case M_ST_NA_1:
            case M_ST_TB_1:
                return typed(asduType, Iec104StepPositionValue.class);
            case M_BO_NA_1:
            case M_BO_TB_1:
                return typed(asduType, Iec104BitstringValue.class);
            case M_ME_NA_1:
            case M_ME_NB_1:
            case M_ME_NC_1:
            case M_ME_ND_1:
            case M_ME_TD_1:
            case M_ME_TE_1:
            case M_ME_TF_1:
                return typed(asduType, Iec104MeasuredValue.class);
            case M_IT_NA_1:
            case M_IT_TB_1:
                return typed(asduType, Iec104IntegratedTotalsValue.class);
            case M_PS_NA_1:
                return typed(asduType, Iec104PackedSinglePointValue.class);
            case C_SC_NA_1:
            case C_SC_TA_1:
                return typed(asduType, Iec104SingleCommandValue.class);
            case C_DC_NA_1:
            case C_DC_TA_1:
                return typed(asduType, Iec104DoubleCommandValue.class);
            case C_RC_NA_1:
            case C_RC_TA_1:
                return typed(asduType, Iec104RegulatingStepCommandValue.class);
            case C_SE_NA_1:
            case C_SE_NB_1:
            case C_SE_NC_1:
            case C_SE_TA_1:
            case C_SE_TB_1:
            case C_SE_TC_1:
                return typed(asduType, Iec104SetPointCommandValue.class);
            case C_BO_NA_1:
            case C_BO_TA_1:
                return typed(asduType, Iec104BitstringCommandValue.class);
            case C_IC_NA_1:
                return typed(asduType, Iec104InterrogationCommandValue.class);
            case C_CI_NA_1:
                return typed(asduType, Iec104CounterInterrogationCommandValue.class);
            case C_RD_NA_1:
                return typed(asduType, Iec104ReadCommandValue.class);
            case C_CS_NA_1:
                return typed(asduType, Iec104ClockSynchronizationCommandValue.class);
            case C_RP_NA_1:
                return typed(asduType, Iec104ResetProcessCommandValue.class);
            case C_CD_NA_1:
                return typed(asduType, Iec104DelayAcquisitionCommandValue.class);
            case UNKNOWN:
            default:
                return unknown();
        }
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public Iec104AsduSupportStatus getStatus() {
        return status;
    }

    public Class<? extends Iec104InformationValue> getValueClass() {
        return valueClass;
    }

    public String getNote() {
        return note;
    }

    public boolean hasTypedValue() {
        return Iec104AsduSupportStatus.TYPED_VALUE.equals(status);
    }

    public boolean isRawBytesOnly() {
        return Iec104AsduSupportStatus.RAW_BYTES_ONLY.equals(status);
    }

    public boolean isUnknownType() {
        return Iec104AsduSupportStatus.UNKNOWN_TYPE.equals(status);
    }

    private static Iec104AsduSupport typed(Iec104AsduType asduType,
                                           Class<? extends Iec104InformationValue> valueClass) {
        return new Iec104AsduSupport(asduType, Iec104AsduSupportStatus.TYPED_VALUE, valueClass,
                "Decoder returns " + valueClass.getSimpleName() + " from Iec104InformationObject.getValue().");
    }

    private static Iec104AsduSupport unknown() {
        return new Iec104AsduSupport(Iec104AsduType.UNKNOWN, Iec104AsduSupportStatus.UNKNOWN_TYPE, null,
                "Type ID is not listed in Iec104AsduType. Raw bytes are still available on the ASDU.");
    }
}
