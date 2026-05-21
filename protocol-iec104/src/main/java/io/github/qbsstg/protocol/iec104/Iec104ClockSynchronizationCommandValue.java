package io.github.qbsstg.protocol.iec104;

public final class Iec104ClockSynchronizationCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final Iec104Cp56Time2a timeTag;

    public Iec104ClockSynchronizationCommandValue(Iec104AsduType asduType, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
