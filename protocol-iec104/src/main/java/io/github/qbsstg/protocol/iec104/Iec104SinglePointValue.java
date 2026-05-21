package io.github.qbsstg.protocol.iec104;

public final class Iec104SinglePointValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final boolean on;
    private final Iec104QualityDescriptor quality;
    private final Iec104Cp56Time2a timeTag;

    public Iec104SinglePointValue(Iec104AsduType asduType, boolean on,
                                  Iec104QualityDescriptor quality, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.on = on;
        this.quality = quality;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public boolean isOn() {
        return on;
    }

    public Iec104QualityDescriptor getQuality() {
        return quality;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
