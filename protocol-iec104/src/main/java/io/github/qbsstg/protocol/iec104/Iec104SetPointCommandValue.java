package io.github.qbsstg.protocol.iec104;

public final class Iec104SetPointCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final Iec104MeasuredValueKind kind;
    private final double value;
    private final int rawValue;
    private final Iec104CommandQualifier qualifier;
    private final Iec104Cp56Time2a timeTag;

    private Iec104SetPointCommandValue(Iec104AsduType asduType, Iec104MeasuredValueKind kind, double value,
                                       int rawValue, Iec104CommandQualifier qualifier, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.kind = kind;
        this.value = value;
        this.rawValue = rawValue;
        this.qualifier = qualifier;
        this.timeTag = timeTag;
    }

    public static Iec104SetPointCommandValue normalized(Iec104AsduType asduType, short rawValue,
                                                        Iec104CommandQualifier qualifier) {
        return normalized(asduType, rawValue, qualifier, null);
    }

    public static Iec104SetPointCommandValue normalized(Iec104AsduType asduType, short rawValue,
                                                        Iec104CommandQualifier qualifier, Iec104Cp56Time2a timeTag) {
        return new Iec104SetPointCommandValue(asduType, Iec104MeasuredValueKind.NORMALIZED,
                rawValue / 32768.0d, rawValue, qualifier, timeTag);
    }

    public static Iec104SetPointCommandValue scaled(Iec104AsduType asduType, short rawValue,
                                                    Iec104CommandQualifier qualifier) {
        return scaled(asduType, rawValue, qualifier, null);
    }

    public static Iec104SetPointCommandValue scaled(Iec104AsduType asduType, short rawValue,
                                                    Iec104CommandQualifier qualifier, Iec104Cp56Time2a timeTag) {
        return new Iec104SetPointCommandValue(asduType, Iec104MeasuredValueKind.SCALED,
                rawValue, rawValue, qualifier, timeTag);
    }

    public static Iec104SetPointCommandValue shortFloat(Iec104AsduType asduType, float value, int rawBits,
                                                        Iec104CommandQualifier qualifier) {
        return shortFloat(asduType, value, rawBits, qualifier, null);
    }

    public static Iec104SetPointCommandValue shortFloat(Iec104AsduType asduType, float value, int rawBits,
                                                        Iec104CommandQualifier qualifier, Iec104Cp56Time2a timeTag) {
        return new Iec104SetPointCommandValue(asduType, Iec104MeasuredValueKind.SHORT_FLOAT,
                value, rawBits, qualifier, timeTag);
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

    public Iec104CommandQualifier getQualifier() {
        return qualifier;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
