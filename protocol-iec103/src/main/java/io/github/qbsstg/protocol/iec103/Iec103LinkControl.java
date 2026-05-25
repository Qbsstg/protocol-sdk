package io.github.qbsstg.protocol.iec103;

public final class Iec103LinkControl {

    private final int rawValue;
    private final boolean primary;
    private final boolean fcbOrAcd;
    private final boolean fcvOrDfc;
    private final int functionCode;
    private final Iec103LinkFunction function;

    public Iec103LinkControl(int rawValue) {
        this.rawValue = rawValue & 0xFF;
        this.primary = (this.rawValue & 0x40) != 0;
        this.fcbOrAcd = (this.rawValue & 0x20) != 0;
        this.fcvOrDfc = (this.rawValue & 0x10) != 0;
        this.functionCode = this.rawValue & 0x0F;
        this.function = Iec103LinkFunction.from(primary, functionCode);
    }

    public int getRawValue() {
        return rawValue;
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

    public Iec103LinkFunction getFunction() {
        return function;
    }
}
