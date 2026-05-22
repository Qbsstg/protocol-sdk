package io.github.qbsstg.protocol.modbus;

public final class ModbusAddressRange implements ModbusPduValue {

    private final int startAddress;
    private final int quantity;

    public ModbusAddressRange(int startAddress, int quantity) {
        this.startAddress = startAddress;
        this.quantity = quantity;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getQuantity() {
        return quantity;
    }
}
