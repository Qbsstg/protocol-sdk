package io.github.qbsstg.protocol.iec101;

public final class Iec101SinglePointValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final boolean on;
    private final Iec101QualityDescriptor quality;
    private final Iec101TimeTag timeTag;

    public Iec101SinglePointValue(Iec101AsduType asduType, boolean on, Iec101QualityDescriptor quality) {
        this(asduType, on, quality, null);
    }

    public Iec101SinglePointValue(Iec101AsduType asduType, boolean on, Iec101QualityDescriptor quality,
                                  Iec101TimeTag timeTag) {
        this.asduType = asduType;
        this.on = on;
        this.quality = quality;
        this.timeTag = timeTag;
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

    public Iec101TimeTag getTimeTag() {
        return timeTag;
    }
}
