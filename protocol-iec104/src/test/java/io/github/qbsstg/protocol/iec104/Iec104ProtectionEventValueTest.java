package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Iec104ProtectionEventValueTest {

    @Test
    public void parsesSingleProtectionEventWithTimeTag() {
        Iec104SingleProtectionEventValue value = (Iec104SingleProtectionEventValue) decodeFirstObject(bytes(
                0x68, 0x17,
                0x00, 0x00, 0x00, 0x00,
                0x26, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x80, 0x00, 0x00,
                0xDA, 0x34, 0x12,
                0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertEquals(Iec104AsduType.M_EP_TD_1, value.getAsduType());
        assertEquals(0xDA, value.getRawSingleEvent());
        assertEquals(Iec104SingleProtectionEventState.ON, value.getEventState());
        assertTrue(value.isOn());
        assertEquals(0xDA, value.getQuality().getRawValue());
        assertTrue(value.getQuality().isInvalid());
        assertTrue(value.getQuality().isNotTopical());
        assertFalse(value.getQuality().isSubstituted());
        assertTrue(value.getQuality().isBlocked());
        assertTrue(value.getQuality().isElapsedTimeInvalid());
        assertEquals(0x1234, value.getElapsedTimeMillis());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
    }

    @Test
    public void parsesPackedStartEventsWithTimeTag() {
        Iec104PackedStartEventsValue value = (Iec104PackedStartEventsValue) decodeFirstObject(bytes(
                0x68, 0x18,
                0x00, 0x00, 0x00, 0x00,
                0x27, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x81, 0x00, 0x00,
                0x35, 0x98, 0xD0, 0x07,
                0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertEquals(Iec104AsduType.M_EP_TE_1, value.getAsduType());
        assertEquals(0x35, value.getRawStartEvents());
        assertTrue(value.isStartEventSet(0));
        assertFalse(value.isStartEventSet(1));
        assertTrue(value.isStartEventSet(2));
        assertTrue(value.isStartEventSet(4));
        assertTrue(value.isStartEventSet(5));
        assertEquals(0x98, value.getQuality().getRawValue());
        assertTrue(value.getQuality().isInvalid());
        assertFalse(value.getQuality().isNotTopical());
        assertFalse(value.getQuality().isSubstituted());
        assertTrue(value.getQuality().isBlocked());
        assertTrue(value.getQuality().isElapsedTimeInvalid());
        assertEquals(2000, value.getRelayDurationMillis());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
    }

    @Test
    public void parsesPackedOutputCircuitInformationWithTimeTag() {
        Iec104PackedOutputCircuitValue value = (Iec104PackedOutputCircuitValue) decodeFirstObject(bytes(
                0x68, 0x18,
                0x00, 0x00, 0x00, 0x00,
                0x28, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x82, 0x00, 0x00,
                0x0B, 0x60, 0x10, 0x27,
                0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertEquals(Iec104AsduType.M_EP_TF_1, value.getAsduType());
        assertEquals(0x0B, value.getRawOutputCircuitInformation());
        assertTrue(value.isOutputCircuitSet(0));
        assertTrue(value.isOutputCircuitSet(1));
        assertFalse(value.isOutputCircuitSet(2));
        assertTrue(value.isOutputCircuitSet(3));
        assertEquals(0x60, value.getQuality().getRawValue());
        assertFalse(value.getQuality().isInvalid());
        assertTrue(value.getQuality().isNotTopical());
        assertTrue(value.getQuality().isSubstituted());
        assertFalse(value.getQuality().isBlocked());
        assertFalse(value.getQuality().isElapsedTimeInvalid());
        assertEquals(10000, value.getRelayOperatingTimeMillis());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsStartEventBitIndexOutsidePackedRange() {
        Iec104PackedStartEventsValue value = new Iec104PackedStartEventsValue(
                Iec104AsduType.M_EP_TE_1, 0, null, 0, null);

        value.isStartEventSet(8);
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsOutputCircuitBitIndexOutsidePackedRange() {
        Iec104PackedOutputCircuitValue value = new Iec104PackedOutputCircuitValue(
                Iec104AsduType.M_EP_TF_1, 0, null, 0, null);

        value.isOutputCircuitSet(-1);
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
