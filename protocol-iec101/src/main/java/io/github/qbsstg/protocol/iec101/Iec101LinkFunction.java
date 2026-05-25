package io.github.qbsstg.protocol.iec101;

public enum Iec101LinkFunction {
    RESET_REMOTE_LINK,
    RESET_USER_PROCESS,
    TEST_LINK_FUNCTION,
    USER_DATA_CONFIRMED,
    USER_DATA_NO_REPLY,
    REQUEST_ACCESS_DEMAND,
    REQUEST_LINK_STATUS,
    REQUEST_USER_DATA_CLASS_1,
    REQUEST_USER_DATA_CLASS_2,
    ACK,
    NACK,
    RESPOND_USER_DATA,
    RESPOND_NO_DATA,
    RESPOND_LINK_STATUS,
    UNKNOWN;

    public static Iec101LinkFunction from(Iec101TransmissionMode mode, boolean primary, int functionCode) {
        if (primary) {
            return primaryFunction(functionCode);
        }
        return secondaryFunction(functionCode);
    }

    private static Iec101LinkFunction primaryFunction(int functionCode) {
        switch (functionCode & 0x0F) {
            case 0:
                return RESET_REMOTE_LINK;
            case 1:
                return RESET_USER_PROCESS;
            case 2:
                return TEST_LINK_FUNCTION;
            case 3:
                return USER_DATA_CONFIRMED;
            case 4:
                return USER_DATA_NO_REPLY;
            case 8:
                return REQUEST_ACCESS_DEMAND;
            case 9:
                return REQUEST_LINK_STATUS;
            case 10:
                return REQUEST_USER_DATA_CLASS_1;
            case 11:
                return REQUEST_USER_DATA_CLASS_2;
            default:
                return UNKNOWN;
        }
    }

    private static Iec101LinkFunction secondaryFunction(int functionCode) {
        switch (functionCode & 0x0F) {
            case 0:
                return ACK;
            case 1:
                return NACK;
            case 8:
                return RESPOND_USER_DATA;
            case 9:
                return RESPOND_NO_DATA;
            case 11:
                return RESPOND_LINK_STATUS;
            default:
                return UNKNOWN;
        }
    }
}
