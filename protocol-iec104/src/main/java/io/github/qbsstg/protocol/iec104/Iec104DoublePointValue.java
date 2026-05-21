package io.github.qbsstg.protocol.iec104;

public final class Iec104DoublePointValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final Iec104DoublePointState state;
    private final Iec104QualityDescriptor quality;
    private final Iec104Cp56Time2a timeTag;

    public Iec104DoublePointValue(Iec104AsduType asduType, Iec104DoublePointState state,
                                  Iec104QualityDescriptor quality, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.state = state;
        this.quality = quality;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public Iec104DoublePointState getState() {
        return state;
    }

    public boolean isOn() {
        return Iec104DoublePointState.ON.equals(state);
    }

    public Iec104QualityDescriptor getQuality() {
        return quality;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
