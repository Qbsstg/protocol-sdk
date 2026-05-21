package io.github.qbsstg.protocol.iec104;

public final class Iec104PackedStartEventsValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int rawStartEvents;
    private final Iec104ProtectionQualityDescriptor quality;
    private final int relayDurationMillis;
    private final Iec104Cp56Time2a timeTag;

    public Iec104PackedStartEventsValue(Iec104AsduType asduType, int rawStartEvents,
                                        Iec104ProtectionQualityDescriptor quality,
                                        int relayDurationMillis, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.rawStartEvents = rawStartEvents & 0xFF;
        this.quality = quality;
        this.relayDurationMillis = relayDurationMillis;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getRawStartEvents() {
        return rawStartEvents;
    }

    public boolean isStartEventSet(int bitIndex) {
        if (bitIndex < 0 || bitIndex > 7) {
            throw new IllegalArgumentException("bitIndex must be between 0 and 7");
        }
        return (rawStartEvents & (1 << bitIndex)) != 0;
    }

    public Iec104ProtectionQualityDescriptor getQuality() {
        return quality;
    }

    public int getRelayDurationMillis() {
        return relayDurationMillis;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
