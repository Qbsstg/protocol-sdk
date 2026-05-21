package io.github.qbsstg.protocol.iec104;

public final class Iec104ReadCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;

    public Iec104ReadCommandValue(Iec104AsduType asduType) {
        this.asduType = asduType;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return null;
    }
}
