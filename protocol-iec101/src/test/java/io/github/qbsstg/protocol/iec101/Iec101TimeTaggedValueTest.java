package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iec101TimeTaggedValueTest {

    private static final LocalDateTime FIXTURE_TIME = LocalDateTime.of(2026, 5, 21, 16, 21, 1);

    @Test
    public void parsesSinglePointInformationWithCp24TimeTag() {
        Iec101InformationObject object = decodeFirstObject(bytes(
                0x02, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x21, 0x00, 0x00,
                0x81, 0xE8, 0x03, 0x95));

        assertEquals(0x21, object.getAddress());
        assertArrayEquals(bytes(0x81, 0xE8, 0x03, 0x95), object.getElementBytes());

        Iec101SinglePointValue value = (Iec101SinglePointValue) object.getValue();
        assertEquals(Iec101AsduType.M_SP_TA_1, value.getAsduType());
        assertTrue(value.isOn());
        assertTrue(value.getQuality().isInvalid());

        Iec101Cp24Time2a timeTag = (Iec101Cp24Time2a) value.getTimeTag();
        assertArrayEquals(bytes(0xE8, 0x03, 0x95), timeTag.getRawBytes());
        assertEquals(1000, timeTag.getMillisecondsWithinMinute());
        assertEquals(1, timeTag.getSecond());
        assertEquals(0, timeTag.getMillisecond());
        assertEquals(21, timeTag.getMinute());
        assertTrue(timeTag.isInvalid());
    }

    @Test
    public void parsesScaledMeasuredValueWithCp56TimeTag() {
        Iec101InformationObject object = decodeFirstObject(bytes(
                0x23, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x22, 0x00, 0x00,
                0x34, 0x12, 0xF1, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        assertEquals(0x22, object.getAddress());
        assertArrayEquals(bytes(0x34, 0x12, 0xF1, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A),
                object.getElementBytes());

        Iec101MeasuredValue value = (Iec101MeasuredValue) object.getValue();
        assertEquals(Iec101AsduType.M_ME_TE_1, value.getAsduType());
        assertEquals(Iec101MeasuredValueKind.SCALED, value.getKind());
        assertEquals(4660.0d, value.getValue(), 0.000001d);
        assertEquals(0x1234, value.getRawValue());
        assertTrue(value.getQuality().isInvalid());
        assertTrue(value.getQuality().isNotTopical());
        assertTrue(value.getQuality().isSubstituted());
        assertTrue(value.getQuality().isBlocked());
        assertTrue(value.getQuality().isOverflow());
        assertCp56TimeTag((Iec101Cp56Time2a) value.getTimeTag());
    }

    @Test
    public void parsesClockSynchronizationTime() {
        Iec101InformationObject object = decodeFirstObject(bytes(
                0x67, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        assertEquals(0, object.getAddress());
        assertArrayEquals(bytes(0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A), object.getElementBytes());

        Iec101ClockSynchronizationCommandValue value =
                (Iec101ClockSynchronizationCommandValue) object.getValue();
        assertEquals(Iec101AsduType.C_CS_NA_1, value.getAsduType());
        assertCp56TimeTag(value.getTimeTag());
    }

    @Test
    public void preservesRawAsduWhenTimeTaggedElementIsTruncated() {
        byte[] asduBytes = bytes(
                0x1E, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x23, 0x00, 0x00,
                0x01, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05);
        Iec101Asdu asdu = decodeAsdu(asduBytes);

        assertEquals(Iec101AsduType.M_SP_TB_1, asdu.getType());
        assertArrayEquals(asduBytes, asdu.getRawBytes());
        assertTrue(asdu.getInformationObjects().isEmpty());
    }

    private Iec101InformationObject decodeFirstObject(byte[] asduBytes) {
        Iec101Asdu asdu = decodeAsdu(asduBytes);
        assertEquals(1, asdu.getInformationObjects().size());
        return asdu.getInformationObjects().get(0);
    }

    private Iec101Asdu decodeAsdu(byte[] asduBytes) {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();
        List<ParseResult<Iec101Frame>> results = decoder.decode(
                Iec101StreamDecoderTest.variableFrame(0x08, 0x01, asduBytes));
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame().getAsdu();
    }

    private void assertCp56TimeTag(Iec101Cp56Time2a timeTag) {
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
        return Iec101StreamDecoderTest.bytes(values);
    }
}
