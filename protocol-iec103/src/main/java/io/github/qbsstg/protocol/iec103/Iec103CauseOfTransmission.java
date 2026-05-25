package io.github.qbsstg.protocol.iec103;

public enum Iec103CauseOfTransmission {
    SPONTANEOUS(1),
    CYCLIC(2),
    RESET_FRAME_COUNT_BIT(3),
    RESET_COMMUNICATION_UNIT(4),
    START_RESTART(5),
    POWER_ON(6),
    TEST_MODE(7),
    TIME_SYNCHRONIZATION(8),
    GENERAL_INTERROGATION(9),
    TERMINATION_OF_GENERAL_INTERROGATION(10),
    UNKNOWN(-1);

    private final int code;

    Iec103CauseOfTransmission(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Iec103CauseOfTransmission fromCode(int code) {
        for (Iec103CauseOfTransmission cause : values()) {
            if (cause.code == code) {
                return cause;
            }
        }
        return UNKNOWN;
    }
}
