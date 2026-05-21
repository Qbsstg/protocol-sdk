package io.github.qbsstg.protocol.iec104;

public final class Iec104StepPositionValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int value;
    private final boolean transientState;
    private final Iec104QualityDescriptor quality;
    private final Iec104Cp56Time2a timeTag;

    public Iec104StepPositionValue(Iec104AsduType asduType, int value, boolean transientState,
                                   Iec104QualityDescriptor quality, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.value = value;
        this.transientState = transientState;
        this.quality = quality;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getValue() {
        return value;
    }

    public boolean isTransientState() {
        return transientState;
    }

    public Iec104QualityDescriptor getQuality() {
        return quality;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
