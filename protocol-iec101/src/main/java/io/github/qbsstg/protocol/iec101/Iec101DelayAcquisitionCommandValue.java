package io.github.qbsstg.protocol.iec101;

public final class Iec101DelayAcquisitionCommandValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final int delayMilliseconds;

    public Iec101DelayAcquisitionCommandValue(Iec101AsduType asduType, int delayMilliseconds) {
        this.asduType = asduType;
        this.delayMilliseconds = delayMilliseconds;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public int getDelayMilliseconds() {
        return delayMilliseconds;
    }
}
