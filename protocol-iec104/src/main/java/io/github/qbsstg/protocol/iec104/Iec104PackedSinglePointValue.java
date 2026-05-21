package io.github.qbsstg.protocol.iec104;

public final class Iec104PackedSinglePointValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int statusBits;
    private final int changeDetectionBits;
    private final Iec104QualityDescriptor quality;

    public Iec104PackedSinglePointValue(Iec104AsduType asduType, int statusBits,
                                        int changeDetectionBits, Iec104QualityDescriptor quality) {
        this.asduType = asduType;
        this.statusBits = statusBits & 0xFFFF;
        this.changeDetectionBits = changeDetectionBits & 0xFFFF;
        this.quality = quality;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getStatusBits() {
        return statusBits;
    }

    public int getChangeDetectionBits() {
        return changeDetectionBits;
    }

    public boolean isOn(int pointIndex) {
        validatePointIndex(pointIndex);
        return (statusBits & (1 << pointIndex)) != 0;
    }

    public boolean hasStatusChanged(int pointIndex) {
        validatePointIndex(pointIndex);
        return (changeDetectionBits & (1 << pointIndex)) != 0;
    }

    public Iec104QualityDescriptor getQuality() {
        return quality;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return null;
    }

    private void validatePointIndex(int pointIndex) {
        if (pointIndex < 0 || pointIndex > 15) {
            throw new IllegalArgumentException("pointIndex must be between 0 and 15");
        }
    }
}
