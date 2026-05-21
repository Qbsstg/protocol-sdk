package io.github.qbsstg.protocol.iec104;

public final class Iec104RegulatingStepCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final Iec104RegulatingStepCommandState state;
    private final Iec104CommandQualifier qualifier;
    private final Iec104Cp56Time2a timeTag;

    public Iec104RegulatingStepCommandValue(Iec104AsduType asduType, Iec104RegulatingStepCommandState state,
                                            Iec104CommandQualifier qualifier) {
        this(asduType, state, qualifier, null);
    }

    public Iec104RegulatingStepCommandValue(Iec104AsduType asduType, Iec104RegulatingStepCommandState state,
                                            Iec104CommandQualifier qualifier, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.state = state;
        this.qualifier = qualifier;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public Iec104RegulatingStepCommandState getState() {
        return state;
    }

    public boolean isLower() {
        return Iec104RegulatingStepCommandState.LOWER.equals(state);
    }

    public boolean isHigher() {
        return Iec104RegulatingStepCommandState.HIGHER.equals(state);
    }

    public Iec104CommandQualifier getQualifier() {
        return qualifier;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
