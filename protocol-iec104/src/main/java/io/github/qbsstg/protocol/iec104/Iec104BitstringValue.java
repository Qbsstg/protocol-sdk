package io.github.qbsstg.protocol.iec104;

public final class Iec104BitstringValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int rawValue;
    private final Iec104QualityDescriptor quality;
    private final Iec104Cp56Time2a timeTag;

    public Iec104BitstringValue(Iec104AsduType asduType, int rawValue,
                                Iec104QualityDescriptor quality, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.rawValue = rawValue;
        this.quality = quality;
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

    public Iec104QualityDescriptor getQuality() {
        return quality;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
