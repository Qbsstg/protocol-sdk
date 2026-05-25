package io.github.qbsstg.protocol.iec101;

public final class Iec101SinglePointValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final boolean on;
    private final Iec101QualityDescriptor quality;

    public Iec101SinglePointValue(Iec101AsduType asduType, boolean on, Iec101QualityDescriptor quality) {
        this.asduType = asduType;
        this.on = on;
        this.quality = quality;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public boolean isOn() {
        return on;
    }

    public Iec101QualityDescriptor getQuality() {
        return quality;
    }
}
