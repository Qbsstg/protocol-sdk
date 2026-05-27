package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.ParseResult;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ModbusDatagramDecoderTest {

    @Test
    public void decodesUdpDatagramResponse() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x07,
                0x11, 0x03, 0x04, 0x02, 0x2B, 0x00, 0x64));

        assertTrue(result.isSuccess());
        assertEquals(0x11, result.getFrame().getUnitId());

        ModbusRegisterValues values = (ModbusRegisterValues) result.getFrame().getPdu().getValue();
        assertArrayEquals(new int[]{555, 100}, values.getValues());
    }

    @Test
    public void rejectsTrailingBytesInStrictUdpDatagram() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03, 0xFF));

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("UDP datagram length"));
    }

    @Test
    public void rejectsInvalidProtocolId() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x01, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03));

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("Invalid Modbus protocol id"));
    }

    @Test
    public void rejectsDeclaredLengthSmallerThanUnitAndFunction() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x01, 0x11));

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("Invalid Modbus ADU length"));
    }

    @Test
    public void reportsIncompleteWhenDeclaredLengthExceedsDatagram() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03));

        assertTrue(result.isIncomplete());
    }

    @Test
    public void rejectsAduLengthAboveConfiguredMaximum() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder(
                new ModbusParserConfig(true, 8, true));

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03));

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("Invalid Modbus ADU length"));
    }

    @Test
    public void rejectsMalformedExceptionResponseLength() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x04,
                0x11, 0x83, 0x02, 0x00));

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("exception response length"));
    }

    @Test
    public void rejectsWriteMultipleRegistersOddByteCount() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x0A,
                0x11, 0x10, 0x00, 0x01, 0x00, 0x02, 0x03, 0x00, 0x0A, 0x01));

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("write multiple registers byte count"));
    }

    @Test
    public void allowsTrailingBytesWhenConfigured() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder(
                new ModbusParserConfig(true, ModbusParserConfig.DEFAULT_MAX_ADU_LENGTH, false));

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x01, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x03, 0xFF));

        assertTrue(result.isSuccess());
        assertEquals(0x03, result.getFrame().getPdu().getFunctionCode());
    }

    @Test
    public void reportsIncompleteShortDatagram() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(0x00, 0x01, 0x00));

        assertTrue(result.isIncomplete());
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
