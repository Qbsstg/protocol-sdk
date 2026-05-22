package io.github.qbsstg.protocol.iec101;

public final class Iec101CommandQualifier {

    private final int rawValue;
    private final boolean select;
    private final int qualifier;

    public Iec101CommandQualifier(int rawValue) {
        this.rawValue = rawValue & 0xFF;
        this.select = (this.rawValue & 0x80) != 0;
        this.qualifier = (this.rawValue >> 2) & 0x1F;
    }

    public int getRawValue() {
        return rawValue;
    }

    public boolean isSelect() {
        return select;
    }

    public int getQualifier() {
        return qualifier;
    }
}
