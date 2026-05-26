package io.github.qbsstg.protocol.iec101;

public final class Iec101MeasuredValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final Iec101MeasuredValueKind kind;
    private final double value;
    private final int rawValue;
    private final Iec101QualityDescriptor quality;
    private final Iec101TimeTag timeTag;

    private Iec101MeasuredValue(Iec101AsduType asduType, Iec101MeasuredValueKind kind,
                                double value, int rawValue, Iec101QualityDescriptor quality,
                                Iec101TimeTag timeTag) {
        this.asduType = asduType;
        this.kind = kind;
        this.value = value;
        this.rawValue = rawValue;
        this.quality = quality;
        this.timeTag = timeTag;
    }

    public static Iec101MeasuredValue normalized(Iec101AsduType asduType, short rawValue,
                                                 Iec101QualityDescriptor quality) {
        return normalized(asduType, rawValue, quality, null);
    }

    public static Iec101MeasuredValue normalized(Iec101AsduType asduType, short rawValue,
                                                 Iec101QualityDescriptor quality, Iec101TimeTag timeTag) {
        return new Iec101MeasuredValue(asduType, Iec101MeasuredValueKind.NORMALIZED,
                rawValue / 32768.0d, rawValue, quality, timeTag);
    }

    public static Iec101MeasuredValue scaled(Iec101AsduType asduType, short rawValue,
                                             Iec101QualityDescriptor quality) {
        return scaled(asduType, rawValue, quality, null);
    }

    public static Iec101MeasuredValue scaled(Iec101AsduType asduType, short rawValue,
                                             Iec101QualityDescriptor quality, Iec101TimeTag timeTag) {
        return new Iec101MeasuredValue(asduType, Iec101MeasuredValueKind.SCALED,
                rawValue, rawValue, quality, timeTag);
    }

    public static Iec101MeasuredValue shortFloat(Iec101AsduType asduType, float value, int rawBits,
                                                 Iec101QualityDescriptor quality) {
        return shortFloat(asduType, value, rawBits, quality, null);
    }

    public static Iec101MeasuredValue shortFloat(Iec101AsduType asduType, float value, int rawBits,
                                                 Iec101QualityDescriptor quality, Iec101TimeTag timeTag) {
        return new Iec101MeasuredValue(asduType, Iec101MeasuredValueKind.SHORT_FLOAT,
                value, rawBits, quality, timeTag);
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public Iec101MeasuredValueKind getKind() {
        return kind;
    }

    public double getValue() {
        return value;
    }

    public int getRawValue() {
        return rawValue;
    }

    public Iec101QualityDescriptor getQuality() {
        return quality;
    }

    public Iec101TimeTag getTimeTag() {
        return timeTag;
    }
}
