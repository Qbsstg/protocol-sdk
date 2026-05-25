package io.github.qbsstg.protocol.iec103;

public final class Iec103Support {

    private final Iec103AsduType asduType;
    private final Iec103AsduSupportStatus status;
    private final Class<? extends Iec103InformationValue> valueClass;
    private final String note;

    private Iec103Support(Iec103AsduType asduType, Iec103AsduSupportStatus status,
                          Class<? extends Iec103InformationValue> valueClass, String note) {
        this.asduType = asduType;
        this.status = status;
        this.valueClass = valueClass;
        this.note = note;
    }

    public static Iec103Support ofTypeId(int typeId) {
        return of(Iec103AsduType.fromTypeId(typeId));
    }

    public static Iec103Support of(Iec103AsduType asduType) {
        if (asduType == null) {
            return unknown();
        }

        switch (asduType) {
            case TIME_TAGGED_MESSAGE:
            case TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME:
                return typed(asduType, Iec103ProtectionEventValue.class);
            case MEASURANDS_I:
            case MEASURANDS_II:
                return typed(asduType, Iec103MeasuredValue.class);
            case IDENTIFICATION:
                return typed(asduType, Iec103IdentificationValue.class);
            case TIME_TAGGED_MEASURANDS_WITH_RELATIVE_TIME:
                return rawBytesOnly(asduType);
            case UNKNOWN:
            default:
                return unknown();
        }
    }

    public Iec103AsduType getAsduType() {
        return asduType;
    }

    public Iec103AsduSupportStatus getStatus() {
        return status;
    }

    public Class<? extends Iec103InformationValue> getValueClass() {
        return valueClass;
    }

    public String getNote() {
        return note;
    }

    public boolean hasTypedValue() {
        return Iec103AsduSupportStatus.TYPED_VALUE.equals(status);
    }

    public boolean isRawBytesOnly() {
        return Iec103AsduSupportStatus.RAW_BYTES_ONLY.equals(status);
    }

    public boolean isUnknownType() {
        return Iec103AsduSupportStatus.UNKNOWN_TYPE.equals(status);
    }

    private static Iec103Support typed(Iec103AsduType asduType,
                                       Class<? extends Iec103InformationValue> valueClass) {
        return new Iec103Support(asduType, Iec103AsduSupportStatus.TYPED_VALUE, valueClass,
                "Decoder returns " + valueClass.getSimpleName() + " from Iec103InformationElement.getValue().");
    }

    private static Iec103Support rawBytesOnly(Iec103AsduType asduType) {
        return new Iec103Support(asduType, Iec103AsduSupportStatus.RAW_BYTES_ONLY, null,
                "ASDU type is recognized, but decoder exposes raw information bytes only.");
    }

    private static Iec103Support unknown() {
        return new Iec103Support(Iec103AsduType.UNKNOWN, Iec103AsduSupportStatus.UNKNOWN_TYPE, null,
                "Type ID is not listed in Iec103AsduType. Raw bytes are still available on the ASDU.");
    }
}
