package io.github.qbsstg.protocol.iec101;

public final class Iec101ClockSynchronizationCommandValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final Iec101Cp56Time2a timeTag;

    public Iec101ClockSynchronizationCommandValue(Iec101AsduType asduType, Iec101Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.timeTag = timeTag;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public Iec101Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
