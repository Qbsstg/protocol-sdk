package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.ParseResult;

import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModbusTcpStreamDecoderTest {

    @Test
    public void decodesReadHoldingRegistersRequest() {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());

        ModbusTcpAdu adu = results.get(0).getFrame();
        assertEquals(1, adu.getTransactionId());
        assertEquals(0, adu.getProtocolId());
        assertEquals(6, adu.getLength());
        assertEquals(0x11, adu.getUnitId());
        assertEquals(0x03, adu.getPdu().getFunctionCode());
        assertEquals(ModbusFunctionCode.READ_HOLDING_REGISTERS, adu.getPdu().getKnownFunctionCode());

        ModbusAddressRange range = (ModbusAddressRange) adu.getPdu().getValue();
        assertEquals(0x006B, range.getStartAddress());
        assertEquals(3, range.getQuantity());
        assertEquals(new ModbusRequestResponseKey(1, 0x11, 0x03), adu.getRequestResponseKey());
    }

    @Test
    public void buffersIncompleteTcpAdu() {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        assertTrue(decoder.decode(bytes(0x00, 0x01, 0x00, 0x00, 0x00)).isEmpty());

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x06, 0x11, 0x03, 0x00, 0x6B, 0x00, 0x03));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
    }

    @Test
    public void decodesConcatenatedTcpAdus() {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03,
                0x00, 0x02, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x01, 0x00, 0x13, 0x00, 0x13));

        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertEquals(1, results.get(0).getFrame().getTransactionId());
        assertEquals(2, results.get(1).getFrame().getTransactionId());
    }

    @Test
    public void decodesReadHoldingRegistersResponse() {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x09,
                0x11, 0x03, 0x06, 0x02, 0x2B, 0x00, 0x00, 0x00, 0x64));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());

        ModbusRegisterValues values = (ModbusRegisterValues) results.get(0).getFrame().getPdu().getValue();
        assertEquals(6, values.getByteCount());
        assertArrayEquals(new int[]{555, 0, 100}, values.getValues());
        assertArrayEquals(bytes(0x02, 0x2B, 0x00, 0x00, 0x00, 0x64), values.getRawData());
    }

    @Test
    public void decodesExceptionResponse() {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x03,
                0x11, 0x83, 0x02));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());

        ModbusPdu pdu = results.get(0).getFrame().getPdu();
        assertTrue(pdu.isExceptionResponse());
        assertEquals(0x83, pdu.getFunctionCode());
        assertEquals(0x03, pdu.getOriginalFunctionCode());

        ModbusExceptionResponse value = (ModbusExceptionResponse) pdu.getValue();
        assertEquals(0x83, value.getEncodedFunctionCode());
        assertEquals(0x03, value.getOriginalFunctionCode());
        assertEquals(ModbusExceptionCode.ILLEGAL_DATA_ADDRESS, value.getExceptionCode());
    }

    @Test
    public void keepsUnknownFunctionCodesAsRawPayload() {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x09, 0x00, 0x00, 0x00, 0x04,
                0x11, 0x41, 0xAA, 0x55));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(ModbusSupportStatus.UNKNOWN, results.get(0).getFrame().getPdu().getSupport().getStatus());

        ModbusRawValue value = (ModbusRawValue) results.get(0).getFrame().getPdu().getValue();
        assertArrayEquals(bytes(0xAA, 0x55), value.getRawBytes());
    }

    @Test
    public void returnsErrorForInvalidProtocolIdAndRecovers() {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x01, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03,
                0x00, 0x02, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03));

        assertFalse(results.isEmpty());
        assertTrue(results.get(0).isError());
        assertTrue(results.get(0).getMessage().contains("Invalid Modbus protocol id"));
        assertTrue(results.get(results.size() - 1).isSuccess());
        assertEquals(2, results.get(results.size() - 1).getFrame().getTransactionId());
    }

    @Test
    public void returnsErrorForMalformedRegisterByteCount() {
        ModbusTcpStreamDecoder decoder = new ModbusTcpStreamDecoder();

        List<ParseResult<ModbusTcpAdu>> results = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x04,
                0x11, 0x03, 0x02, 0x12));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isError());
        assertTrue(results.get(0).getMessage().contains("byte count"));
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
