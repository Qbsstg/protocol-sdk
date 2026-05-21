package io.github.qbsstg.protocol.iec104;

public enum Iec104SingleProtectionEventState {
    INDETERMINATE_ZERO(0),
    OFF(1),
    ON(2),
    INDETERMINATE_THREE(3);

    private final int code;

    Iec104SingleProtectionEventState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Iec104SingleProtectionEventState fromCode(int code) {
        switch (code & 0x03) {
            case 1:
                return OFF;
            case 2:
                return ON;
            case 3:
                return INDETERMINATE_THREE;
            case 0:
            default:
                return INDETERMINATE_ZERO;
        }
    }
}
