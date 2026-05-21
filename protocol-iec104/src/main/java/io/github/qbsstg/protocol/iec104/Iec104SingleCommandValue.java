package io.github.qbsstg.protocol.iec104;

public final class Iec104SingleCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final boolean on;
    private final Iec104CommandQualifier qualifier;
    private final Iec104Cp56Time2a timeTag;

    public Iec104SingleCommandValue(Iec104AsduType asduType, boolean on, Iec104CommandQualifier qualifier) {
        this(asduType, on, qualifier, null);
    }

    public Iec104SingleCommandValue(Iec104AsduType asduType, boolean on,
                                    Iec104CommandQualifier qualifier, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.on = on;
        this.qualifier = qualifier;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public boolean isOn() {
        return on;
    }

    public Iec104CommandQualifier getQualifier() {
        return qualifier;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
