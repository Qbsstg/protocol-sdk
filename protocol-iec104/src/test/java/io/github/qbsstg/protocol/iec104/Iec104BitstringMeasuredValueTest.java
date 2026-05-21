package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec104BitstringMeasuredValueTest {

    @Test
    public void parsesBitstringValue() {
        Iec104BitstringValue value = (Iec104BitstringValue) decodeFirstObject(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x07, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x50, 0x00, 0x00,
                0x05, 0x00, 0x00, 0x80, 0xF1)).getValue();

        assertEquals(Iec104AsduType.M_BO_NA_1, value.getAsduType());
        assertEquals(0x80000005, value.getRawValue());
        assertTrue(value.isBitSet(0));
        assertFalse(value.isBitSet(1));
        assertTrue(value.isBitSet(2));
        assertTrue(value.isBitSet(31));
        assertEquals(0xF1, value.getQuality().getRawValue());
        assertTrue(value.getQuality().isInvalid());
        assertTrue(value.getQuality().isOverflow());
    }

    @Test
    public void parsesBitstringValueWithTimeTag() {
        Iec104BitstringValue value = (Iec104BitstringValue) decodeFirstObject(bytes(
                0x68, 0x19,
                0x00, 0x00, 0x00, 0x00,
                0x21, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x51, 0x00, 0x00,
                0x78, 0x56, 0x34, 0x12, 0x00,
                0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertEquals(Iec104AsduType.M_BO_TB_1, value.getAsduType());
        assertEquals(0x12345678, value.getRawValue());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
    }

    @Test
    public void parsesNormalizedMeasuredValueWithoutQualityDescriptor() {
        Iec104MeasuredValue value = (Iec104MeasuredValue) decodeFirstObject(bytes(
                0x68, 0x0F,
                0x00, 0x00, 0x00, 0x00,
                0x15, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x52, 0x00, 0x00,
                0x00, 0x40)).getValue();

        assertEquals(Iec104AsduType.M_ME_ND_1, value.getAsduType());
        assertEquals(Iec104MeasuredValueKind.NORMALIZED, value.getKind());
        assertEquals(0.5d, value.getValue(), 0.000001d);
        assertEquals(0x4000, value.getRawValue());
        assertNull(value.getQuality());
        assertNull(value.getTimeTag());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsBitIndexOutsideBitstringRange() {
        Iec104BitstringValue value = new Iec104BitstringValue(Iec104AsduType.M_BO_NA_1, 0, null, null);

        value.isBitSet(32);
    }

    private Iec104InformationObject decodeFirstObject(byte[] bytes) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame().getAsdu().getInformationObjects().get(0);
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
