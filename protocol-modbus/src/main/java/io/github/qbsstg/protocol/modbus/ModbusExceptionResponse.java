package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusExceptionResponse implements ModbusPduValue {

    private final int encodedFunctionCode;
    private final int originalFunctionCode;
    private final ModbusFunctionCode knownFunctionCode;
    private final int rawExceptionCode;
    private final ModbusExceptionCode exceptionCode;
    private final byte[] rawBytes;

    public ModbusExceptionResponse(int encodedFunctionCode, int rawExceptionCode, byte[] rawBytes) {
        this.encodedFunctionCode = encodedFunctionCode;
        this.originalFunctionCode = encodedFunctionCode & 0x7F;
        this.knownFunctionCode = ModbusFunctionCode.fromCode(this.originalFunctionCode);
        this.rawExceptionCode = rawExceptionCode;
        this.exceptionCode = ModbusExceptionCode.fromCode(rawExceptionCode);
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
    }

    public int getEncodedFunctionCode() {
        return encodedFunctionCode;
    }

    public int getOriginalFunctionCode() {
        return originalFunctionCode;
    }

    public ModbusFunctionCode getKnownFunctionCode() {
        return knownFunctionCode;
    }

    public int getRawExceptionCode() {
        return rawExceptionCode;
    }

    public ModbusExceptionCode getExceptionCode() {
        return exceptionCode;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }
}
