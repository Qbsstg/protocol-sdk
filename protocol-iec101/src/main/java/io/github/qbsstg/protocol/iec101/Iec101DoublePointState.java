package io.github.qbsstg.protocol.iec101;

public enum Iec101DoublePointState {
    INTERMEDIATE(0),
    OFF(1),
    ON(2),
    INDETERMINATE(3);

    private final int code;

    Iec101DoublePointState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Iec101DoublePointState fromCode(int code) {
        switch (code & 0x03) {
            case 1:
                return OFF;
            case 2:
                return ON;
            case 3:
                return INDETERMINATE;
            case 0:
            default:
                return INTERMEDIATE;
        }
    }
}
