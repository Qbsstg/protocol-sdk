package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusWriteMultipleRegistersValue implements ModbusPduValue {

    private final ModbusAddressRange range;
    private final int byteCount;
    private final int[] values;
    private final byte[] rawData;

    public ModbusWriteMultipleRegistersValue(ModbusAddressRange range, int byteCount, int[] values, byte[] rawData) {
        this.range = range;
        this.byteCount = byteCount;
        this.values = copy(values);
        this.rawData = ByteArrayUtil.copyOf(rawData);
    }

    public ModbusAddressRange getRange() {
        return range;
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
