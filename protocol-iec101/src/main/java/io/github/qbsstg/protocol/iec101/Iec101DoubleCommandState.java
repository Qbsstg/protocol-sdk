package io.github.qbsstg.protocol.iec101;

public enum Iec101DoubleCommandState {
    NOT_PERMITTED(0),
    OFF(1),
    ON(2),
    NOT_PERMITTED_2(3);

    private final int code;

    Iec101DoubleCommandState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Iec101DoubleCommandState fromCode(int code) {
        switch (code & 0x03) {
            case 1:
                return OFF;
            case 2:
                return ON;
            case 3:
                return NOT_PERMITTED_2;
            case 0:
            default:
                return NOT_PERMITTED;
        }
    }
}
