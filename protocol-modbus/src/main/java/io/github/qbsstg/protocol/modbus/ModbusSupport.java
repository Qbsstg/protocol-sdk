package io.github.qbsstg.protocol.modbus;

public final class ModbusSupport {

    private final int functionCode;
    private final ModbusFunctionCode knownFunctionCode;
    private final ModbusSupportStatus status;

    private ModbusSupport(int functionCode, ModbusFunctionCode knownFunctionCode, ModbusSupportStatus status) {
        this.functionCode = functionCode;
        this.knownFunctionCode = knownFunctionCode;
        this.status = status;
    }

    public static ModbusSupport forFunctionCode(int functionCode) {
        int normalized = functionCode & 0x7F;
        ModbusFunctionCode known = ModbusFunctionCode.fromCode(normalized);
        if (known == null) {
            return new ModbusSupport(normalized, null, ModbusSupportStatus.UNKNOWN);
        }
        if (known == ModbusFunctionCode.READ_WRITE_MULTIPLE_REGISTERS) {
            return new ModbusSupport(normalized, known, ModbusSupportStatus.RAW_ONLY);
        }
        return new ModbusSupport(normalized, known, ModbusSupportStatus.TYPED);
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public ModbusFunctionCode getKnownFunctionCode() {
        return knownFunctionCode;
    }

    public ModbusSupportStatus getStatus() {
        return status;
    }

    public boolean isTyped() {
        return status == ModbusSupportStatus.TYPED;
    }
}
