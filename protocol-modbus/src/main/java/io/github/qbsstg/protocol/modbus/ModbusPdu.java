package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusPdu {

    private final int functionCode;
    private final ModbusFunctionCode knownFunctionCode;
    private final boolean exceptionResponse;
    private final ModbusPduValue value;
    private final ModbusSupport support;
    private final byte[] rawBytes;

    public ModbusPdu(int functionCode, ModbusPduValue value, byte[] rawBytes) {
        this.functionCode = functionCode;
        this.knownFunctionCode = ModbusFunctionCode.fromCode(functionCode);
        this.exceptionResponse = (functionCode & 0x80) != 0;
        this.value = value;
        this.support = ModbusSupport.forFunctionCode(functionCode);
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public int getOriginalFunctionCode() {
        return functionCode & 0x7F;
    }

    public ModbusFunctionCode getKnownFunctionCode() {
        return knownFunctionCode;
    }

    public boolean isExceptionResponse() {
        return exceptionResponse;
    }

    public ModbusPduValue getValue() {
        return value;
    }

    public ModbusSupport getSupport() {
        return support;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }
}
