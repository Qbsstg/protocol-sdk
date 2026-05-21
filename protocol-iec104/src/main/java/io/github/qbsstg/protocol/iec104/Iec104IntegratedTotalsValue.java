package io.github.qbsstg.protocol.iec104;

public final class Iec104IntegratedTotalsValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int counterValue;
    private final int sequenceNumber;
    private final boolean carry;
    private final boolean adjusted;
    private final boolean invalid;
    private final Iec104Cp56Time2a timeTag;

    public Iec104IntegratedTotalsValue(Iec104AsduType asduType, int counterValue, int sequenceNumber,
                                       boolean carry, boolean adjusted, boolean invalid,
                                       Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.counterValue = counterValue;
        this.sequenceNumber = sequenceNumber;
        this.carry = carry;
        this.adjusted = adjusted;
        this.invalid = invalid;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getCounterValue() {
        return counterValue;
    }

    public int getSequenceNumber() {
        return sequenceNumber;
    }

    public boolean isCarry() {
        return carry;
    }

    public boolean isAdjusted() {
        return adjusted;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
