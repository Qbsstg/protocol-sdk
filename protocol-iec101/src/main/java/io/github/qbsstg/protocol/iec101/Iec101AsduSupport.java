package io.github.qbsstg.protocol.iec101;

public final class Iec101AsduSupport {

    private final Iec101AsduType asduType;
    private final Iec101AsduSupportStatus status;
    private final Class<? extends Iec101InformationValue> valueClass;
    private final String note;

    private Iec101AsduSupport(Iec101AsduType asduType, Iec101AsduSupportStatus status,
                              Class<? extends Iec101InformationValue> valueClass, String note) {
        this.asduType = asduType;
        this.status = status;
        this.valueClass = valueClass;
        this.note = note;
    }

    public static Iec101AsduSupport ofTypeId(int typeId) {
        return of(Iec101AsduType.fromTypeId(typeId));
    }

    public static Iec101AsduSupport of(Iec101AsduType asduType) {
        if (asduType == null) {
            return unknown();
        }

        switch (asduType) {
            case M_SP_NA_1:
            case M_SP_TA_1:
            case M_SP_TB_1:
                return typed(asduType, Iec101SinglePointValue.class);
            case M_DP_NA_1:
            case M_DP_TA_1:
            case M_DP_TB_1:
                return typed(asduType, Iec101DoublePointValue.class);
            case M_ME_NA_1:
            case M_ME_TA_1:
            case M_ME_NB_1:
            case M_ME_TB_1:
            case M_ME_NC_1:
            case M_ME_TC_1:
            case M_ME_TD_1:
            case M_ME_TE_1:
            case M_ME_TF_1:
                return typed(asduType, Iec101MeasuredValue.class);
            case C_SC_NA_1:
                return typed(asduType, Iec101SingleCommandValue.class);
            case C_DC_NA_1:
                return typed(asduType, Iec101DoubleCommandValue.class);
            case C_IC_NA_1:
                return typed(asduType, Iec101InterrogationCommandValue.class);
            case C_CS_NA_1:
                return typed(asduType, Iec101ClockSynchronizationCommandValue.class);
            case UNKNOWN:
            default:
                return unknown();
        }
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public Iec101AsduSupportStatus getStatus() {
        return status;
    }

    public Class<? extends Iec101InformationValue> getValueClass() {
        return valueClass;
    }

    public String getNote() {
        return note;
    }

    public boolean hasTypedValue() {
        return Iec101AsduSupportStatus.TYPED_VALUE.equals(status);
    }

    public boolean isRawBytesOnly() {
        return Iec101AsduSupportStatus.RAW_BYTES_ONLY.equals(status);
    }

    public boolean isUnknownType() {
        return Iec101AsduSupportStatus.UNKNOWN_TYPE.equals(status);
    }

    private static Iec101AsduSupport typed(Iec101AsduType asduType,
                                           Class<? extends Iec101InformationValue> valueClass) {
        return new Iec101AsduSupport(asduType, Iec101AsduSupportStatus.TYPED_VALUE, valueClass,
                "Decoder returns " + valueClass.getSimpleName() + " from Iec101InformationObject.getValue().");
    }

    private static Iec101AsduSupport rawBytesOnly(Iec101AsduType asduType) {
        return new Iec101AsduSupport(asduType, Iec101AsduSupportStatus.RAW_BYTES_ONLY, null,
                "ASDU type is recognized, but decoder exposes raw information bytes only.");
    }

    private static Iec101AsduSupport unknown() {
        return new Iec101AsduSupport(Iec101AsduType.UNKNOWN, Iec101AsduSupportStatus.UNKNOWN_TYPE, null,
                "Type ID is not listed in Iec101AsduType. Raw bytes are still available on the ASDU.");
    }
}
