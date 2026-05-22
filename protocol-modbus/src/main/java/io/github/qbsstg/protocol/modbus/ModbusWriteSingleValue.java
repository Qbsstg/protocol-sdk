package io.github.qbsstg.protocol.modbus;

public final class ModbusWriteSingleValue implements ModbusPduValue {

    private final int address;
    private final int value;

    public ModbusWriteSingleValue(int address, int value) {
        this.address = address;
        this.value = value;
    }

    public int getAddress() {
        return address;
    }

    public int getValue() {
        return value;
    }
}
