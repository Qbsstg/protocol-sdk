package io.github.qbsstg.protocol.iec104;

public final class Iec104MeasuredValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final Iec104MeasuredValueKind kind;
    private final double value;
    private final int rawValue;
    private final Iec104QualityDescriptor quality;
    private final Iec104Cp56Time2a timeTag;

    private Iec104MeasuredValue(Iec104AsduType asduType, Iec104MeasuredValueKind kind, double value,
                                int rawValue, Iec104QualityDescriptor quality, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.kind = kind;
        this.value = value;
        this.rawValue = rawValue;
        this.quality = quality;
        this.timeTag = timeTag;
    }

    public static Iec104MeasuredValue normalized(Iec104AsduType asduType, short rawValue,
                                                 Iec104QualityDescriptor quality, Iec104Cp56Time2a timeTag) {
        return new Iec104MeasuredValue(asduType, Iec104MeasuredValueKind.NORMALIZED,
                rawValue / 32768.0d, rawValue, quality, timeTag);
    }

    public static Iec104MeasuredValue scaled(Iec104AsduType asduType, short rawValue,
                                             Iec104QualityDescriptor quality, Iec104Cp56Time2a timeTag) {
        return new Iec104MeasuredValue(asduType, Iec104MeasuredValueKind.SCALED,
                rawValue, rawValue, quality, timeTag);
    }

    public static Iec104MeasuredValue shortFloat(Iec104AsduType asduType, float value, int rawBits,
                                                 Iec104QualityDescriptor quality, Iec104Cp56Time2a timeTag) {
        return new Iec104MeasuredValue(asduType, Iec104MeasuredValueKind.SHORT_FLOAT,
                value, rawBits, quality, timeTag);
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public Iec104MeasuredValueKind getKind() {
        return kind;
    }

    public double getValue() {
        return value;
    }

    public int getRawValue() {
        return rawValue;
    }

    public Iec104QualityDescriptor getQuality() {
        return quality;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
