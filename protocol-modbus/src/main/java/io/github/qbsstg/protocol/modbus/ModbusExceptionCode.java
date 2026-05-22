package io.github.qbsstg.protocol.modbus;

public enum ModbusExceptionCode {

    ILLEGAL_FUNCTION(0x01),
    ILLEGAL_DATA_ADDRESS(0x02),
    ILLEGAL_DATA_VALUE(0x03),
    SERVER_DEVICE_FAILURE(0x04),
    ACKNOWLEDGE(0x05),
    SERVER_DEVICE_BUSY(0x06),
    MEMORY_PARITY_ERROR(0x08),
    GATEWAY_PATH_UNAVAILABLE(0x0A),
    GATEWAY_TARGET_DEVICE_FAILED_TO_RESPOND(0x0B);

    private final int code;

    ModbusExceptionCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ModbusExceptionCode fromCode(int code) {
        ModbusExceptionCode[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].code == code) {
                return values[i];
            }
        }
        return null;
    }
}
