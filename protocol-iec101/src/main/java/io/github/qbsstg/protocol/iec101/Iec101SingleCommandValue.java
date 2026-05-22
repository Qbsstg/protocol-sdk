package io.github.qbsstg.protocol.iec101;

public final class Iec101SingleCommandValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final boolean on;
    private final Iec101CommandQualifier qualifier;

    public Iec101SingleCommandValue(Iec101AsduType asduType, boolean on, Iec101CommandQualifier qualifier) {
        this.asduType = asduType;
        this.on = on;
        this.qualifier = qualifier;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public boolean isOn() {
        return on;
    }

    public Iec101CommandQualifier getQualifier() {
        return qualifier;
    }
}
