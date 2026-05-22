package io.github.qbsstg.protocol.iec101;

public final class Iec101InterrogationCommandValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final int qualifier;

    public Iec101InterrogationCommandValue(Iec101AsduType asduType, int qualifier) {
        this.asduType = asduType;
        this.qualifier = qualifier & 0xFF;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public int getQualifier() {
        return qualifier;
    }

    public boolean isStationInterrogation() {
        return qualifier == 20;
    }
}
