package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.ParseResult;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ModbusPduValueTest {

    @Test
    public void decodesReadCoilsResponseBitsLeastSignificantBitFirst() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x02, 0x00, 0x00, 0x00, 0x04,
                0x11, 0x01, 0x01, 0x05));

        assertTrue(result.isSuccess());

        ModbusBitValues values = (ModbusBitValues) result.getFrame().getPdu().getValue();
        assertEquals(1, values.getByteCount());
        boolean[] bits = values.getValues();
        assertTrue(bits[0]);
        assertFalse(bits[1]);
        assertTrue(bits[2]);
        assertFalse(bits[3]);
    }

    @Test
    public void decodesWriteSingleCoilEcho() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x03, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x05, 0x00, 0xAC, 0xFF, 0x00));

        assertTrue(result.isSuccess());

        ModbusWriteSingleValue value = (ModbusWriteSingleValue) result.getFrame().getPdu().getValue();
        assertEquals(0x00AC, value.getAddress());
        assertEquals(0xFF00, value.getValue());
    }

    @Test
    public void decodesWriteMultipleCoilsRequest() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x04, 0x00, 0x00, 0x00, 0x09,
                0x11, 0x0F, 0x00, 0x13, 0x00, 0x0A, 0x02, 0xCD, 0x01));

        assertTrue(result.isSuccess());

        ModbusWriteMultipleBitsValue value =
                (ModbusWriteMultipleBitsValue) result.getFrame().getPdu().getValue();
        assertEquals(0x0013, value.getRange().getStartAddress());
        assertEquals(10, value.getRange().getQuantity());
        assertEquals(2, value.getByteCount());
        assertEquals(10, value.getValues().length);
    }

    @Test
    public void decodesWriteMultipleRegistersRequest() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x05, 0x00, 0x00, 0x00, 0x0B,
                0x11, 0x10, 0x00, 0x01, 0x00, 0x02, 0x04, 0x00, 0x0A, 0x01, 0x02));

        assertTrue(result.isSuccess());

        ModbusWriteMultipleRegistersValue value =
                (ModbusWriteMultipleRegistersValue) result.getFrame().getPdu().getValue();
        assertEquals(0x0001, value.getRange().getStartAddress());
        assertEquals(2, value.getRange().getQuantity());
        assertEquals(4, value.getByteCount());
        assertArrayEquals(new int[]{10, 258}, value.getValues());
    }

    @Test
    public void decodesReadWriteMultipleRegistersRequest() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x06, 0x00, 0x00, 0x00, 0x0F,
                0x11, 0x17,
                0x00, 0x01, 0x00, 0x02,
                0x00, 0x10, 0x00, 0x02,
                0x04, 0x00, 0x0A, 0x01, 0x02));

        assertTrue(result.isSuccess());
        assertEquals(ModbusSupportStatus.TYPED, result.getFrame().getPdu().getSupport().getStatus());

        ModbusReadWriteMultipleRegistersValue value =
                (ModbusReadWriteMultipleRegistersValue) result.getFrame().getPdu().getValue();
        assertEquals(0x0001, value.getReadRange().getStartAddress());
        assertEquals(2, value.getReadRange().getQuantity());
        assertEquals(0x0010, value.getWriteRange().getStartAddress());
        assertEquals(2, value.getWriteRange().getQuantity());
        assertEquals(4, value.getByteCount());
        assertArrayEquals(new int[]{10, 258}, value.getValues());
    }

    @Test
    public void decodesReadWriteMultipleRegistersResponse() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x07, 0x00, 0x00, 0x00, 0x07,
                0x11, 0x17, 0x04, 0x00, 0x0A, 0x01, 0x02));

        assertTrue(result.isSuccess());
        assertEquals(ModbusSupportStatus.TYPED, result.getFrame().getPdu().getSupport().getStatus());

        ModbusRegisterValues value = (ModbusRegisterValues) result.getFrame().getPdu().getValue();
        assertEquals(4, value.getByteCount());
        assertArrayEquals(new int[]{10, 258}, value.getValues());
    }

    @Test
    public void rejectsReadWriteMultipleRegistersOddRequestByteCount() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x08, 0x00, 0x00, 0x00, 0x0E,
                0x11, 0x17,
                0x00, 0x01, 0x00, 0x02,
                0x00, 0x10, 0x00, 0x02,
                0x03, 0x00, 0x0A, 0x01));

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("read/write multiple registers byte count"));
    }

    @Test
    public void rejectsReadWriteMultipleRegistersOddResponseByteCount() {
        ModbusDatagramDecoder decoder = new ModbusDatagramDecoder();

        ParseResult<ModbusTcpAdu> result = decoder.decode(bytes(
                0x00, 0x09, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x17, 0x03, 0x00, 0x0A, 0x01));

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains("read/write multiple registers response byte count"));
    }

    @Test
    public void rejectsReadCoilsQuantityOutsideStandardRange() {
        assertErrorContains(bytes(
                0x00, 0x10, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x01, 0x00, 0x13, 0x00, 0x00),
                "bit read quantity");

        assertErrorContains(bytes(
                0x00, 0x11, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x02, 0x00, 0x13, 0x07, 0xD1),
                "bit read quantity");
    }

    @Test
    public void rejectsReadRegistersQuantityOutsideStandardRange() {
        assertErrorContains(bytes(
                0x00, 0x12, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x03, 0x00, 0x6B, 0x00, 0x00),
                "register read quantity");

        assertErrorContains(bytes(
                0x00, 0x13, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x04, 0x00, 0x6B, 0x00, 0x7E),
                "register read quantity");
    }

    @Test
    public void rejectsInvalidReadResponseByteCounts() {
        assertErrorContains(bytes(
                0x00, 0x14, 0x00, 0x00, 0x00, 0x03,
                0x11, 0x01, 0x00),
                "bit response byte count");

        assertErrorContains(bytes(
                0x00, 0x15, 0x00, 0x00, 0x00, 0x03,
                0x11, 0x03, 0x00),
                "register response byte count");
    }

    @Test
    public void rejectsWriteSingleCoilValuesOutsideStandardSet() {
        assertErrorContains(bytes(
                0x00, 0x16, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x05, 0x00, 0xAC, 0x00, 0x01),
                "write single coil value");
    }

    @Test
    public void rejectsWriteMultipleCoilsQuantityAndByteCountMismatch() {
        assertErrorContains(bytes(
                0x00, 0x17, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x0F, 0x00, 0x13, 0x07, 0xB1),
                "write multiple coils quantity");

        assertErrorContains(bytes(
                0x00, 0x18, 0x00, 0x00, 0x00, 0x08,
                0x11, 0x0F, 0x00, 0x13, 0x00, 0x0A, 0x01, 0xCD),
                "write multiple coils byte count");
    }

    @Test
    public void rejectsWriteMultipleRegistersQuantityAndByteCountMismatch() {
        assertErrorContains(bytes(
                0x00, 0x19, 0x00, 0x00, 0x00, 0x06,
                0x11, 0x10, 0x00, 0x01, 0x00, 0x7C),
                "write multiple registers quantity");

        assertErrorContains(bytes(
                0x00, 0x1A, 0x00, 0x00, 0x00, 0x09,
                0x11, 0x10, 0x00, 0x01, 0x00, 0x02, 0x02, 0x00, 0x0A),
                "write multiple registers byte count");
    }

    private static void assertErrorContains(byte[] datagram, String message) {
        ParseResult<ModbusTcpAdu> result = new ModbusDatagramDecoder().decode(datagram);

        assertTrue(result.isError());
        assertTrue(result.getMessage().contains(message));
    }

    private static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
