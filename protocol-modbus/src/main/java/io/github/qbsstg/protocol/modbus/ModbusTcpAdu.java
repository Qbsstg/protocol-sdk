package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.ProtocolFrame;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusTcpAdu implements ProtocolFrame {

    private final int transactionId;
    private final int protocolId;
    private final int length;
    private final int unitId;
    private final ModbusPdu pdu;
    private final byte[] rawBytes;

    public ModbusTcpAdu(int transactionId, int protocolId, int length, int unitId,
                        ModbusPdu pdu, byte[] rawBytes) {
        this.transactionId = transactionId;
        this.protocolId = protocolId;
        this.length = length;
        this.unitId = unitId;
        this.pdu = pdu;
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
    }

    public int getTransactionId() {
        return transactionId;
    }

    public int getProtocolId() {
        return protocolId;
    }

    public int getLength() {
        return length;
    }

    public int getUnitId() {
        return unitId;
    }

    public ModbusPdu getPdu() {
        return pdu;
    }

    public ModbusRequestResponseKey getRequestResponseKey() {
        return new ModbusRequestResponseKey(transactionId, unitId, pdu.getOriginalFunctionCode());
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }

    public String getFrameType() {
        return "MODBUS_TCP_ADU";
    }
}
