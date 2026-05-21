package io.github.qbsstg.protocol.iec104;

public final class Iec104CommandQualifier {

    private final int rawValue;
    private final int qualifier;
    private final boolean select;

    private Iec104CommandQualifier(int rawValue, int qualifier, boolean select) {
        this.rawValue = rawValue & 0xFF;
        this.qualifier = qualifier;
        this.select = select;
    }

    public static Iec104CommandQualifier command(int rawValue) {
        return new Iec104CommandQualifier(rawValue, (rawValue >> 2) & 0x1F, (rawValue & 0x80) != 0);
    }

    public static Iec104CommandQualifier setPoint(int rawValue) {
        return new Iec104CommandQualifier(rawValue, rawValue & 0x7F, (rawValue & 0x80) != 0);
    }

    public int getRawValue() {
        return rawValue;
    }

    public int getQualifier() {
        return qualifier;
    }

    public boolean isSelect() {
        return select;
    }

    public boolean isExecute() {
        return !select;
    }
}
