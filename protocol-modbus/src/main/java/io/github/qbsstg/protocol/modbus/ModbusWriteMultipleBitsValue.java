package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusWriteMultipleBitsValue implements ModbusPduValue {

    private final ModbusAddressRange range;
    private final int byteCount;
    private final boolean[] values;
    private final byte[] rawData;

    public ModbusWriteMultipleBitsValue(ModbusAddressRange range, int byteCount, boolean[] values, byte[] rawData) {
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

    public boolean[] getValues() {
        return copy(values);
    }

    public byte[] getRawData() {
        return ByteArrayUtil.copyOf(rawData);
    }

    private boolean[] copy(boolean[] source) {
        if (source == null) {
            return new boolean[0];
        }
        boolean[] copy = new boolean[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }
}
