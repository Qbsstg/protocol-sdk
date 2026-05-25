package io.github.qbsstg.protocol.iec101;

public final class Iec101DoubleCommandValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final Iec101DoubleCommandState state;
    private final Iec101CommandQualifier qualifier;

    public Iec101DoubleCommandValue(Iec101AsduType asduType, Iec101DoubleCommandState state,
                                    Iec101CommandQualifier qualifier) {
        this.asduType = asduType;
        this.state = state;
        this.qualifier = qualifier;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public Iec101DoubleCommandState getState() {
        return state;
    }

    public Iec101CommandQualifier getQualifier() {
        return qualifier;
    }
}
