package io.github.qbsstg.protocol.iec104;

public final class Iec104ParameterMeasuredValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final Iec104MeasuredValueKind kind;
    private final double value;
    private final int rawValue;
    private final Iec104ParameterQualifier qualifier;

    private Iec104ParameterMeasuredValue(Iec104AsduType asduType, Iec104MeasuredValueKind kind, double value,
                                         int rawValue, Iec104ParameterQualifier qualifier) {
        this.asduType = asduType;
        this.kind = kind;
        this.value = value;
        this.rawValue = rawValue;
        this.qualifier = qualifier;
    }

    public static Iec104ParameterMeasuredValue normalized(Iec104AsduType asduType, short rawValue,
                                                          Iec104ParameterQualifier qualifier) {
        return new Iec104ParameterMeasuredValue(asduType, Iec104MeasuredValueKind.NORMALIZED,
                rawValue / 32768.0d, rawValue, qualifier);
    }

    public static Iec104ParameterMeasuredValue scaled(Iec104AsduType asduType, short rawValue,
                                                      Iec104ParameterQualifier qualifier) {
        return new Iec104ParameterMeasuredValue(asduType, Iec104MeasuredValueKind.SCALED,
                rawValue, rawValue, qualifier);
    }

    public static Iec104ParameterMeasuredValue shortFloat(Iec104AsduType asduType, float value, int rawBits,
                                                          Iec104ParameterQualifier qualifier) {
        return new Iec104ParameterMeasuredValue(asduType, Iec104MeasuredValueKind.SHORT_FLOAT,
                value, rawBits, qualifier);
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

    public Iec104ParameterQualifier getQualifier() {
        return qualifier;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return null;
    }
}
