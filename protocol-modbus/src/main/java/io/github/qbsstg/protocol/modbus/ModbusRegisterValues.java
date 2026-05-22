package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusRegisterValues implements ModbusPduValue {

    private final int byteCount;
    private final int[] values;
    private final byte[] rawData;

    public ModbusRegisterValues(int byteCount, int[] values, byte[] rawData) {
        this.byteCount = byteCount;
        this.values = copy(values);
        this.rawData = ByteArrayUtil.copyOf(rawData);
    }

    public int getByteCount() {
        return byteCount;
    }

    public int[] getValues() {
        return copy(values);
    }

    public byte[] getRawData() {
        return ByteArrayUtil.copyOf(rawData);
    }

    private int[] copy(int[] source) {
        if (source == null) {
            return new int[0];
        }
        int[] copy = new int[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }
}
