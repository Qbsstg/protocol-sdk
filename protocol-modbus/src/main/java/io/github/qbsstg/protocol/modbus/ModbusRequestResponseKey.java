package io.github.qbsstg.protocol.modbus;

public final class ModbusRequestResponseKey {

    private final int transactionId;
    private final int unitId;
    private final int functionCode;

    public ModbusRequestResponseKey(int transactionId, int unitId, int functionCode) {
        this.transactionId = transactionId;
        this.unitId = unitId;
        this.functionCode = functionCode;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getUnitId() {
        return unitId;
    }

    public int getFunctionCode() {
        return functionCode;
    }

    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ModbusRequestResponseKey)) {
            return false;
        }
        ModbusRequestResponseKey that = (ModbusRequestResponseKey) other;
        return transactionId == that.transactionId
                && unitId == that.unitId
                && functionCode == that.functionCode;
    }

    public int hashCode() {
        int result = transactionId;
        result = 31 * result + unitId;
        result = 31 * result + functionCode;
        return result;
    }

    public String toString() {
        return "ModbusRequestResponseKey{"
                + "transactionId=" + transactionId
                + ", unitId=" + unitId
                + ", functionCode=" + functionCode
                + '}';
    }
}
