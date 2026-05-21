package io.github.qbsstg.protocol.iec104;

public final class Iec104DoubleCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final Iec104DoubleCommandState state;
    private final Iec104CommandQualifier qualifier;
    private final Iec104Cp56Time2a timeTag;

    public Iec104DoubleCommandValue(Iec104AsduType asduType, Iec104DoubleCommandState state,
                                    Iec104CommandQualifier qualifier) {
        this(asduType, state, qualifier, null);
    }

    public Iec104DoubleCommandValue(Iec104AsduType asduType, Iec104DoubleCommandState state,
                                    Iec104CommandQualifier qualifier, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.state = state;
        this.qualifier = qualifier;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public Iec104DoubleCommandState getState() {
        return state;
    }

    public boolean isOn() {
        return Iec104DoubleCommandState.ON.equals(state);
    }

    public Iec104CommandQualifier getQualifier() {
        return qualifier;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
