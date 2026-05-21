package io.github.qbsstg.protocol.iec104;

public final class Iec104DelayAcquisitionCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int delayMilliseconds;

    public Iec104DelayAcquisitionCommandValue(Iec104AsduType asduType, int delayMilliseconds) {
        this.asduType = asduType;
        this.delayMilliseconds = delayMilliseconds;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getDelayMilliseconds() {
        return delayMilliseconds;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return null;
    }
}
