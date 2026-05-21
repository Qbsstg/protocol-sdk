package io.github.qbsstg.protocol.iec104;

public final class Iec104BitstringCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int rawValue;
    private final Iec104CommandQualifier qualifier;
    private final Iec104Cp56Time2a timeTag;

    public Iec104BitstringCommandValue(Iec104AsduType asduType, int rawValue, Iec104CommandQualifier qualifier) {
        this(asduType, rawValue, qualifier, null);
    }

    public Iec104BitstringCommandValue(Iec104AsduType asduType, int rawValue,
                                       Iec104CommandQualifier qualifier, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.rawValue = rawValue;
        this.qualifier = qualifier;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getRawValue() {
        return rawValue;
    }

    public boolean isBitSet(int bitIndex) {
        if (bitIndex < 0 || bitIndex > 31) {
            throw new IllegalArgumentException("bitIndex must be between 0 and 31");
        }
        return (rawValue & (1 << bitIndex)) != 0;
    }

    public Iec104CommandQualifier getQualifier() {
        return qualifier;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
