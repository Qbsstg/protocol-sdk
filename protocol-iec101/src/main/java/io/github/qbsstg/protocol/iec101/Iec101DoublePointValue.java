package io.github.qbsstg.protocol.iec101;

public final class Iec101DoublePointValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final Iec101DoublePointState state;
    private final Iec101QualityDescriptor quality;

    public Iec101DoublePointValue(Iec101AsduType asduType, Iec101DoublePointState state,
                                  Iec101QualityDescriptor quality) {
        this.asduType = asduType;
        this.state = state;
        this.quality = quality;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public Iec101DoublePointState getState() {
        return state;
    }

    public boolean isOn() {
        return Iec101DoublePointState.ON.equals(state);
    }

    public Iec101QualityDescriptor getQuality() {
        return quality;
    }
}
