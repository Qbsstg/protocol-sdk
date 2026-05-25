package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec103ProtectionEventValue implements Iec103InformationValue {

    private final Iec103AsduType asduType;
    private final int functionType;
    private final int informationNumber;
    private final int rawEvent;
    private final Iec103ProtectionEventState eventState;
    private final Iec103ProtectionQualityDescriptor quality;
    private final Integer relativeTimeMillis;
    private final Integer faultNumber;
    private final Iec103TimeTag timeTag;
    private final byte[] rawBytes;

    public Iec103ProtectionEventValue(Iec103AsduType asduType, int functionType, int informationNumber,
                                      int rawEvent, Integer relativeTimeMillis, Integer faultNumber,
                                      Iec103TimeTag timeTag, byte[] rawBytes) {
        this.asduType = asduType;
        this.functionType = functionType & 0xFF;
        this.informationNumber = informationNumber & 0xFF;
        this.rawEvent = rawEvent & 0xFF;
        this.eventState = Iec103ProtectionEventState.fromCode(rawEvent);
        this.quality = new Iec103ProtectionQualityDescriptor(rawEvent);
        this.relativeTimeMillis = relativeTimeMillis;
        this.faultNumber = faultNumber;
        this.timeTag = timeTag;
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
    }

    public Iec103AsduType getAsduType() {
        return asduType;
    }

    public int getFunctionType() {
        return functionType;
    }

    public int getInformationNumber() {
        return informationNumber;
    }

    public int getRawEvent() {
        return rawEvent;
    }

    public Iec103ProtectionEventState getEventState() {
        return eventState;
    }

    public boolean isOn() {
        return Iec103ProtectionEventState.ON.equals(eventState);
    }

    public Iec103ProtectionQualityDescriptor getQuality() {
        return quality;
    }

    public Integer getRelativeTimeMillis() {
        return relativeTimeMillis;
    }

    public Integer getFaultNumber() {
        return faultNumber;
    }

    public Iec103TimeTag getTimeTag() {
        return timeTag;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }
}
