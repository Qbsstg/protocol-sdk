package io.github.qbsstg.protocol.iec104;

public enum Iec104CauseOfTransmission {
    PERIODIC(1),
    BACKGROUND_SCAN(2),
    SPONTANEOUS(3),
    INITIALIZED(4),
    REQUEST(5),
    ACTIVATION(6),
    ACTIVATION_CONFIRMATION(7),
    DEACTIVATION(8),
    DEACTIVATION_CONFIRMATION(9),
    ACTIVATION_TERMINATION(10),
    RETURN_INFORMATION_REMOTE(11),
    RETURN_INFORMATION_LOCAL(12),
    FILE_TRANSFER(13),
    INTERROGATED_BY_STATION(20),
    INTERROGATED_BY_GROUP_1(21),
    INTERROGATED_BY_GROUP_2(22),
    INTERROGATED_BY_GROUP_3(23),
    INTERROGATED_BY_GROUP_4(24),
    INTERROGATED_BY_GROUP_5(25),
    INTERROGATED_BY_GROUP_6(26),
    INTERROGATED_BY_GROUP_7(27),
    INTERROGATED_BY_GROUP_8(28),
    INTERROGATED_BY_GROUP_9(29),
    INTERROGATED_BY_GROUP_10(30),
    INTERROGATED_BY_GROUP_11(31),
    INTERROGATED_BY_GROUP_12(32),
    INTERROGATED_BY_GROUP_13(33),
    INTERROGATED_BY_GROUP_14(34),
    INTERROGATED_BY_GROUP_15(35),
    INTERROGATED_BY_GROUP_16(36),
    UNKNOWN(-1);

    private final int code;

    Iec104CauseOfTransmission(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static Iec104CauseOfTransmission fromCode(int code) {
        for (Iec104CauseOfTransmission cause : values()) {
            if (cause.code == code) {
                return cause;
            }
        }
        return UNKNOWN;
    }
}
