package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Iec104IntegratedStepValueTest {

    @Test
    public void parsesStepPositionValue() {
        Iec104StepPositionValue value = (Iec104StepPositionValue) decodeFirstObject(bytes(
                0x68, 0x0F,
                0x00, 0x00, 0x00, 0x00,
                0x05, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x30, 0x00, 0x00,
                0x85, 0xF0)).getValue();

        assertEquals(Iec104AsduType.M_ST_NA_1, value.getAsduType());
        assertEquals(5, value.getValue());
        assertTrue(value.isTransientState());
        assertEquals(0xF0, value.getQuality().getRawValue());
        assertTrue(value.getQuality().isInvalid());
        assertTrue(value.getQuality().isNotTopical());
        assertTrue(value.getQuality().isSubstituted());
        assertTrue(value.getQuality().isBlocked());
    }

    @Test
    public void parsesNegativeStepPositionWithTimeTag() {
        Iec104StepPositionValue value = (Iec104StepPositionValue) decodeFirstObject(bytes(
                0x68, 0x16,
                0x00, 0x00, 0x00, 0x00,
                0x20, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x31, 0x00, 0x00,
                0x7F, 0x00, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertEquals(Iec104AsduType.M_ST_TB_1, value.getAsduType());
        assertEquals(-1, value.getValue());
        assertFalse(value.isTransientState());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
    }

    @Test
    public void parsesIntegratedTotalsValue() {
        Iec104IntegratedTotalsValue value = (Iec104IntegratedTotalsValue) decodeFirstObject(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x0F, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x40, 0x00, 0x00,
                0x78, 0x56, 0x34, 0x12, 0xE3)).getValue();

        assertEquals(Iec104AsduType.M_IT_NA_1, value.getAsduType());
        assertEquals(0x12345678, value.getCounterValue());
        assertEquals(3, value.getSequenceNumber());
        assertTrue(value.isCarry());
        assertTrue(value.isAdjusted());
        assertTrue(value.isInvalid());
    }

    @Test
    public void parsesNegativeIntegratedTotalsWithTimeTag() {
        Iec104IntegratedTotalsValue value = (Iec104IntegratedTotalsValue) decodeFirstObject(bytes(
                0x68, 0x19,
                0x00, 0x00, 0x00, 0x00,
                0x25, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x41, 0x00, 0x00,
                0xFF, 0xFF, 0xFF, 0xFF, 0x04,
                0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertEquals(Iec104AsduType.M_IT_TB_1, value.getAsduType());
        assertEquals(-1, value.getCounterValue());
        assertEquals(4, value.getSequenceNumber());
        assertFalse(value.isCarry());
        assertFalse(value.isAdjusted());
        assertFalse(value.isInvalid());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
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
