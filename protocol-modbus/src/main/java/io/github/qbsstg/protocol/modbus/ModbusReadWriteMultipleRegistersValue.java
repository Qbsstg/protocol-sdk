package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusReadWriteMultipleRegistersValue implements ModbusPduValue {

    private final ModbusAddressRange readRange;
    private final ModbusAddressRange writeRange;
    private final int byteCount;
    private final int[] values;
    private final byte[] rawData;

    public ModbusReadWriteMultipleRegistersValue(ModbusAddressRange readRange,
                                                 ModbusAddressRange writeRange,
                                                 int byteCount,
                                                 int[] values,
                                                 byte[] rawData) {
        this.readRange = readRange;
        this.writeRange = writeRange;
        this.byteCount = byteCount;
        this.values = copy(values);
        this.rawData = ByteArrayUtil.copyOf(rawData);
    }

    public ModbusAddressRange getReadRange() {
        return readRange;
    }

    public ModbusAddressRange getWriteRange() {
        return writeRange;
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
