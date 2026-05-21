package io.github.qbsstg.protocol.iec104;

public enum Iec104RegulatingStepCommandState {
    NOT_PERMITTED_ZERO(0),
    LOWER(1),
    HIGHER(2),
    NOT_PERMITTED_THREE(3);

    private final int code;

    Iec104RegulatingStepCommandState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Iec104RegulatingStepCommandState fromCode(int code) {
        switch (code & 0x03) {
            case 1:
                return LOWER;
            case 2:
                return HIGHER;
            case 3:
                return NOT_PERMITTED_THREE;
            case 0:
            default:
                return NOT_PERMITTED_ZERO;
        }
    }
}
