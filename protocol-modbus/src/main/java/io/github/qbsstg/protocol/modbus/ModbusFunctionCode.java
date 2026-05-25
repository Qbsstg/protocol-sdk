package io.github.qbsstg.protocol.modbus;

public enum ModbusFunctionCode {

    READ_COILS(0x01),
    READ_DISCRETE_INPUTS(0x02),
    READ_HOLDING_REGISTERS(0x03),
    READ_INPUT_REGISTERS(0x04),
    WRITE_SINGLE_COIL(0x05),
    WRITE_SINGLE_REGISTER(0x06),
    WRITE_MULTIPLE_COILS(0x0F),
    WRITE_MULTIPLE_REGISTERS(0x10),
    READ_WRITE_MULTIPLE_REGISTERS(0x17);

    private final int code;

    ModbusFunctionCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static ModbusFunctionCode fromCode(int code) {
        int normalized = code & 0x7F;
        ModbusFunctionCode[] values = values();
        for (int i = 0; i < values.length; i++) {
            if (values[i].code == normalized) {
                return values[i];
            }
        }
        return null;
    }
}
