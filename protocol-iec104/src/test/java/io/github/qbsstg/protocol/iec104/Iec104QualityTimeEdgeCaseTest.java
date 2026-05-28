package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec104QualityTimeEdgeCaseTest {

    @Test
    public void parsesRepresentativeStatusQualityFlags() {
        Iec104DoublePointValue value = (Iec104DoublePointValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x03, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x11, 0x00, 0x00,
                0xF2)).getValue();

        assertEquals(Iec104DoublePointState.ON, value.getState());
        assertStatusQuality(value.getQuality(), 0xF2);
    }

    @Test
    public void parsesRepresentativeMeasurementQualityFlags() {
        Iec104MeasuredValue value = (Iec104MeasuredValue) decodeFirstObject(bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x09, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x12, 0x00, 0x00,
                0x00, 0x00, 0xF1)).getValue();

        assertEquals(Iec104MeasuredValueKind.NORMALIZED, value.getKind());
        assertEquals(0.0d, value.getValue(), 0.000001d);
        assertMeasurementQuality(value.getQuality(), 0xF1);
    }

    @Test
    public void parsesRepresentativeProtectionQualityFlags() {
        Iec104PackedStartEventsValue value = (Iec104PackedStartEventsValue) decodeFirstObject(bytes(
                0x68, 0x18,
                0x00, 0x00, 0x00, 0x00,
                0x27, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x13, 0x00, 0x00,
                0x01, 0xF8, 0x01, 0x00,
                0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertTrue(value.isStartEventSet(0));
        assertEquals(1, value.getRelayDurationMillis());
        assertProtectionQuality(value.getQuality(), 0xF8);
    }

    @Test
    public void parsesBoundaryCp56Time2aWithSummerTime() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x1E, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x14, 0x00, 0x00,
                0x01, 0x5F, 0xEA, 0x3B, 0x97, 0xFF, 0x0C, 0x63));

        assertArrayEquals(bytes(0x01, 0x5F, 0xEA, 0x3B, 0x97, 0xFF, 0x0C, 0x63),
                object.getElementBytes());

        Iec104SinglePointValue value = (Iec104SinglePointValue) object.getValue();
        Iec104Cp56Time2a timeTag = value.getTimeTag();
        assertArrayEquals(bytes(0x5F, 0xEA, 0x3B, 0x97, 0xFF, 0x0C, 0x63), timeTag.getRawBytes());
        assertEquals(59999, timeTag.getMillisecondsWithinMinute());
        assertEquals(59, timeTag.getSecond());
        assertEquals(999, timeTag.getMillisecond());
        assertEquals(59, timeTag.getMinute());
        assertEquals(23, timeTag.getHour());
        assertEquals(31, timeTag.getDayOfMonth());
        assertEquals(7, timeTag.getDayOfWeek());
        assertEquals(12, timeTag.getMonth());
        assertEquals(2099, timeTag.getYear());
        assertTrue(timeTag.isSummerTime());
        assertFalse(timeTag.isInvalid());
        assertEquals(LocalDateTime.of(2099, 12, 31, 23, 59, 59, 999000000), timeTag.getDateTime());
    }

    @Test
    public void preservesRawTimeTagBytesWhenCp56Time2aDateIsInvalid() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x17,
                0x00, 0x00, 0x00, 0x00,
                0x22, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x15, 0x00, 0x00,
                0x00, 0x00, 0xF1, 0x60, 0xEA, 0x80, 0x00, 0x00, 0x0D, 0x00));

        assertArrayEquals(bytes(0x00, 0x00, 0xF1, 0x60, 0xEA, 0x80, 0x00, 0x00, 0x0D, 0x00),
                object.getElementBytes());

        Iec104MeasuredValue value = (Iec104MeasuredValue) object.getValue();
        assertMeasurementQuality(value.getQuality(), 0xF1);

        Iec104Cp56Time2a timeTag = value.getTimeTag();
        assertArrayEquals(bytes(0x60, 0xEA, 0x80, 0x00, 0x00, 0x0D, 0x00), timeTag.getRawBytes());
        assertEquals(60000, timeTag.getMillisecondsWithinMinute());
        assertEquals(60, timeTag.getSecond());
        assertEquals(0, timeTag.getMillisecond());
        assertEquals(0, timeTag.getMinute());
        assertEquals(0, timeTag.getHour());
        assertEquals(0, timeTag.getDayOfMonth());
        assertEquals(13, timeTag.getMonth());
        assertEquals(2000, timeTag.getYear());
        assertTrue(timeTag.isInvalid());
        assertFalse(timeTag.isSummerTime());
        assertNull(timeTag.getDateTime());
    }

    private Iec104InformationObject decodeFirstObject(byte[] bytes) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(1, results.get(0).getFrame().getAsdu().getInformationObjects().size());
        return results.get(0).getFrame().getAsdu().getInformationObjects().get(0);
    }

    private void assertStatusQuality(Iec104QualityDescriptor quality, int rawValue) {
        assertEquals(rawValue, quality.getRawValue());
        assertTrue(quality.isInvalid());
        assertTrue(quality.isNotTopical());
        assertTrue(quality.isSubstituted());
        assertTrue(quality.isBlocked());
        assertFalse(quality.isOverflow());
    }

    private void assertMeasurementQuality(Iec104QualityDescriptor quality, int rawValue) {
        assertEquals(rawValue, quality.getRawValue());
        assertTrue(quality.isInvalid());
        assertTrue(quality.isNotTopical());
        assertTrue(quality.isSubstituted());
        assertTrue(quality.isBlocked());
        assertTrue(quality.isOverflow());
    }

    private void assertProtectionQuality(Iec104ProtectionQualityDescriptor quality, int rawValue) {
        assertEquals(rawValue, quality.getRawValue());
        assertTrue(quality.isInvalid());
        assertTrue(quality.isNotTopical());
        assertTrue(quality.isSubstituted());
        assertTrue(quality.isBlocked());
        assertTrue(quality.isElapsedTimeInvalid());
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
