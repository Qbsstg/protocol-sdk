package io.github.qbsstg.protocol.iec101;

public final class Iec101ReadCommandValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;

    public Iec101ReadCommandValue(Iec101AsduType asduType) {
        this.asduType = asduType;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }
}
