package io.github.qbsstg.protocol.iec104;

public enum Iec104CounterFreezeResetQualifier {
    READ(0),
    FREEZE_WITHOUT_RESET(1),
    FREEZE_WITH_RESET(2),
    RESET(3);

    private final int code;

    Iec104CounterFreezeResetQualifier(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Iec104CounterFreezeResetQualifier fromCode(int code) {
        switch (code & 0x03) {
            case 1:
                return FREEZE_WITHOUT_RESET;
            case 2:
                return FREEZE_WITH_RESET;
            case 3:
                return RESET;
            case 0:
            default:
                return READ;
        }
    }
}
