package io.github.qbsstg.protocol.iec101;

public final class Iec101LinkControl {

    private final int rawValue;
    private final Iec101TransmissionMode transmissionMode;
    private final boolean primary;
    private final boolean fcbOrAcd;
    private final boolean fcvOrDfc;
    private final int functionCode;
    private final Iec101LinkFunction function;

    public Iec101LinkControl(int rawValue, Iec101TransmissionMode transmissionMode) {
        this.rawValue = rawValue & 0xFF;
        this.transmissionMode = transmissionMode;
        this.primary = (this.rawValue & 0x40) != 0;
        this.fcbOrAcd = (this.rawValue & 0x20) != 0;
        this.fcvOrDfc = (this.rawValue & 0x10) != 0;
        this.functionCode = this.rawValue & 0x0F;
        this.function = Iec101LinkFunction.from(transmissionMode, primary, functionCode);
    }

    public int getRawValue() {
        return rawValue;
    }

    public Iec101TransmissionMode getTransmissionMode() {
        return transmissionMode;
    }

    public boolean isPrimary() {
        return primary;
    }

    public boolean isFcbOrAcd() {
        return fcbOrAcd;
    }

    public boolean isFcvOrDfc() {
        return fcvOrDfc;
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public Iec101LinkFunction getFunction() {
        return function;
    }
}
