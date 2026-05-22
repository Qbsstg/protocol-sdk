package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusRawValue implements ModbusPduValue {

    private final byte[] rawBytes;

    public ModbusRawValue(byte[] rawBytes) {
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }
}
