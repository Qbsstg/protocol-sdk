package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Iec104TimeTaggedProcessValueTest {

    private static final LocalDateTime FIXTURE_TIME = LocalDateTime.of(2026, 5, 21, 16, 21, 1);

    @Test
    public void parsesDoublePointInformationWithTimeTag() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x1F, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x21, 0x00, 0x00,
                0x82, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        assertEquals(0x21, object.getAddress());
        assertArrayEquals(bytes(0x82, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A),
                object.getElementBytes());

        Iec104DoublePointValue value = (Iec104DoublePointValue) object.getValue();
        assertEquals(Iec104AsduType.M_DP_TB_1, value.getAsduType());
        assertEquals(Iec104DoublePointState.ON, value.getState());
        assertTrue(value.isOn());
        assertEquals(0x82, value.getQuality().getRawValue());
        assertTrue(value.getQuality().isInvalid());
        assertFalse(value.getQuality().isBlocked());
        assertTimeTag(value.getTimeTag());
    }

    @Test
    public void parsesNormalizedMeasuredValueWithTimeTag() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x17,
                0x00, 0x00, 0x00, 0x00,
                0x22, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x22, 0x00, 0x00,
                0x00, 0x40, 0xF1, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        assertEquals(0x22, object.getAddress());
        assertArrayEquals(bytes(0x00, 0x40, 0xF1, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A),
                object.getElementBytes());

        Iec104MeasuredValue value = (Iec104MeasuredValue) object.getValue();
        assertEquals(Iec104AsduType.M_ME_TD_1, value.getAsduType());
        assertEquals(Iec104MeasuredValueKind.NORMALIZED, value.getKind());
        assertEquals(0.5d, value.getValue(), 0.000001d);
        assertEquals(0x4000, value.getRawValue());
        assertMeasuredQuality(value.getQuality(), 0xF1);
        assertTimeTag(value.getTimeTag());
    }

    @Test
    public void parsesScaledMeasuredValueWithTimeTag() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x17,
                0x00, 0x00, 0x00, 0x00,
                0x23, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x23, 0x00, 0x00,
                0x34, 0x12, 0xF1, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        assertEquals(0x23, object.getAddress());
        assertArrayEquals(bytes(0x34, 0x12, 0xF1, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A),
                object.getElementBytes());

        Iec104MeasuredValue value = (Iec104MeasuredValue) object.getValue();
        assertEquals(Iec104AsduType.M_ME_TE_1, value.getAsduType());
        assertEquals(Iec104MeasuredValueKind.SCALED, value.getKind());
        assertEquals(4660.0d, value.getValue(), 0.000001d);
        assertEquals(0x1234, value.getRawValue());
        assertMeasuredQuality(value.getQuality(), 0xF1);
        assertTimeTag(value.getTimeTag());
    }

    @Test
    public void parsesShortFloatMeasuredValueWithTimeTag() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x19,
                0x00, 0x00, 0x00, 0x00,
                0x24, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x24, 0x00, 0x00,
                0x00, 0x00, 0x20, 0x41, 0xF1, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        assertEquals(0x24, object.getAddress());
        assertArrayEquals(bytes(0x00, 0x00, 0x20, 0x41, 0xF1, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A),
                object.getElementBytes());

        Iec104MeasuredValue value = (Iec104MeasuredValue) object.getValue();
        assertEquals(Iec104AsduType.M_ME_TF_1, value.getAsduType());
        assertEquals(Iec104MeasuredValueKind.SHORT_FLOAT, value.getKind());
        assertEquals(10.0d, value.getValue(), 0.000001d);
        assertEquals(0x41200000, value.getRawValue());
        assertMeasuredQuality(value.getQuality(), 0xF1);
        assertTimeTag(value.getTimeTag());
    }

    private Iec104InformationObject decodeFirstObject(byte[] bytes) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(1, results.get(0).getFrame().getAsdu().getInformationObjects().size());
        return results.get(0).getFrame().getAsdu().getInformationObjects().get(0);
    }

    private void assertMeasuredQuality(Iec104QualityDescriptor quality, int rawValue) {
        assertEquals(rawValue, quality.getRawValue());
        assertTrue(quality.isInvalid());
        assertTrue(quality.isNotTopical());
        assertTrue(quality.isSubstituted());
        assertTrue(quality.isBlocked());
        assertTrue(quality.isOverflow());
    }

    private void assertTimeTag(Iec104Cp56Time2a timeTag) {
        assertEquals(FIXTURE_TIME, timeTag.getDateTime());
        assertEquals(1000, timeTag.getMillisecondsWithinMinute());
        assertEquals(1, timeTag.getSecond());
        assertEquals(0, timeTag.getMillisecond());
        assertEquals(21, timeTag.getMinute());
        assertEquals(16, timeTag.getHour());
        assertEquals(21, timeTag.getDayOfMonth());
        assertEquals(5, timeTag.getMonth());
        assertEquals(2026, timeTag.getYear());
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
