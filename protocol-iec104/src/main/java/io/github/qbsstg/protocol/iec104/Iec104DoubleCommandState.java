package io.github.qbsstg.protocol.iec104;

public enum Iec104DoubleCommandState {
    NOT_PERMITTED_ZERO(0),
    OFF(1),
    ON(2),
    NOT_PERMITTED_THREE(3);

    private final int code;

    Iec104DoubleCommandState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Iec104DoubleCommandState fromCode(int code) {
        switch (code & 0x03) {
            case 1:
                return OFF;
            case 2:
                return ON;
            case 3:
                return NOT_PERMITTED_THREE;
            case 0:
            default:
                return NOT_PERMITTED_ZERO;
        }
    }
}
