package io.github.qbsstg.protocol.iec104;

public final class Iec104SingleProtectionEventValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int rawSingleEvent;
    private final Iec104SingleProtectionEventState eventState;
    private final Iec104ProtectionQualityDescriptor quality;
    private final int elapsedTimeMillis;
    private final Iec104Cp56Time2a timeTag;

    public Iec104SingleProtectionEventValue(Iec104AsduType asduType, int rawSingleEvent,
                                            int elapsedTimeMillis, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.rawSingleEvent = rawSingleEvent & 0xFF;
        this.eventState = Iec104SingleProtectionEventState.fromCode(rawSingleEvent);
        this.quality = new Iec104ProtectionQualityDescriptor(rawSingleEvent);
        this.elapsedTimeMillis = elapsedTimeMillis;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getRawSingleEvent() {
        return rawSingleEvent;
    }

    public Iec104SingleProtectionEventState getEventState() {
        return eventState;
    }

    public boolean isOn() {
        return Iec104SingleProtectionEventState.ON.equals(eventState);
    }

    public Iec104ProtectionQualityDescriptor getQuality() {
        return quality;
    }

    public int getElapsedTimeMillis() {
        return elapsedTimeMillis;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
