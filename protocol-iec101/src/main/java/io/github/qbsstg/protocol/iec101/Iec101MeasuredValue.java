package io.github.qbsstg.protocol.iec101;

public final class Iec101MeasuredValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final Iec101MeasuredValueKind kind;
    private final double value;
    private final int rawValue;
    private final Iec101QualityDescriptor quality;

    private Iec101MeasuredValue(Iec101AsduType asduType, Iec101MeasuredValueKind kind,
                                double value, int rawValue, Iec101QualityDescriptor quality) {
        this.asduType = asduType;
        this.kind = kind;
        this.value = value;
        this.rawValue = rawValue;
        this.quality = quality;
    }

    public static Iec101MeasuredValue normalized(Iec101AsduType asduType, short rawValue,
                                                 Iec101QualityDescriptor quality) {
        return new Iec101MeasuredValue(asduType, Iec101MeasuredValueKind.NORMALIZED,
                rawValue / 32768.0d, rawValue, quality);
    }

    public static Iec101MeasuredValue scaled(Iec101AsduType asduType, short rawValue,
                                             Iec101QualityDescriptor quality) {
        return new Iec101MeasuredValue(asduType, Iec101MeasuredValueKind.SCALED,
                rawValue, rawValue, quality);
    }

    public static Iec101MeasuredValue shortFloat(Iec101AsduType asduType, float value, int rawBits,
                                                 Iec101QualityDescriptor quality) {
        return new Iec101MeasuredValue(asduType, Iec101MeasuredValueKind.SHORT_FLOAT,
                value, rawBits, quality);
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
}
