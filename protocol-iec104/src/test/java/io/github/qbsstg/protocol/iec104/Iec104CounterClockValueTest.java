package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec104CounterClockValueTest {

    @Test
    public void parsesCounterInterrogationGeneralRead() {
        Iec104CounterInterrogationCommandValue value = (Iec104CounterInterrogationCommandValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x65, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x05)).getValue();

        assertEquals(Iec104AsduType.C_CI_NA_1, value.getAsduType());
        assertEquals(0x05, value.getRawQualifier());
        assertEquals(5, value.getRequestQualifier());
        assertTrue(value.isGeneralCounterInterrogation());
        assertEquals(Iec104CounterFreezeResetQualifier.READ, value.getFreezeResetQualifier());
        assertNull(value.getCounterGroupNumber());
    }

    @Test
    public void parsesCounterInterrogationGroupFreezeAndReset() {
        Iec104CounterInterrogationCommandValue value = (Iec104CounterInterrogationCommandValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x65, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x82)).getValue();

        assertEquals(0x82, value.getRawQualifier());
        assertEquals(2, value.getRequestQualifier());
        assertEquals(Integer.valueOf(2), value.getCounterGroupNumber());
        assertEquals(Iec104CounterFreezeResetQualifier.FREEZE_WITH_RESET, value.getFreezeResetQualifier());
    }

    @Test
    public void parsesClockSynchronizationTime() {
        Iec104ClockSynchronizationCommandValue value = (Iec104ClockSynchronizationCommandValue) decodeFirstObject(bytes(
                0x68, 0x14,
                0x00, 0x00, 0x00, 0x00,
                0x67, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertEquals(Iec104AsduType.C_CS_NA_1, value.getAsduType());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
        assertEquals(1000, value.getTimeTag().getMillisecondsWithinMinute());
        assertEquals(21, value.getTimeTag().getMinute());
        assertEquals(16, value.getTimeTag().getHour());
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
