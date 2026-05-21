package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec104BoundaryTest {

    @Test
    public void parsesSignedNegativeMeasuredValues() {
        Iec104MeasuredValue normalized = (Iec104MeasuredValue) decodeFirstObject(bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x09, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x04, 0x40, 0x00,
                0x00, 0xC0, 0x00)).getValue();

        assertEquals(Iec104MeasuredValueKind.NORMALIZED, normalized.getKind());
        assertEquals(-0.5d, normalized.getValue(), 0.000001d);
        assertEquals(-16384, normalized.getRawValue());

        Iec104MeasuredValue scaled = (Iec104MeasuredValue) decodeFirstObject(bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x0B, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x05, 0x40, 0x00,
                0xFF, 0xFF, 0x00)).getValue();

        assertEquals(Iec104MeasuredValueKind.SCALED, scaled.getKind());
        assertEquals(-1.0d, scaled.getValue(), 0.000001d);
        assertEquals(-1, scaled.getRawValue());

        Iec104MeasuredValue shortFloat = (Iec104MeasuredValue) decodeFirstObject(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x0D, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x06, 0x40, 0x00,
                0x00, 0x00, 0x20, 0xC1, 0x00)).getValue();

        assertEquals(Iec104MeasuredValueKind.SHORT_FLOAT, shortFloat.getKind());
        assertEquals(-10.0d, shortFloat.getValue(), 0.000001d);
    }

    @Test
    public void exposesStatusAndMeasuredQualityFlags() {
        Iec104SinglePointValue singlePoint = (Iec104SinglePointValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x01, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0xF1)).getValue();

        assertTrue(singlePoint.isOn());
        assertQualityFlags(singlePoint.getQuality(), 0xF1, false);

        Iec104MeasuredValue measured = (Iec104MeasuredValue) decodeFirstObject(bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x09, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x40, 0x00,
                0x00, 0x00, 0xF1)).getValue();

        assertEquals(0.0d, measured.getValue(), 0.000001d);
        assertQualityFlags(measured.getQuality(), 0xF1, true);
    }

    @Test
    public void mapsAllDoublePointStates() {
        assertEquals(Iec104DoublePointState.INTERMEDIATE, decodeDoublePointState(0x00));
        assertEquals(Iec104DoublePointState.OFF, decodeDoublePointState(0x01));
        assertEquals(Iec104DoublePointState.ON, decodeDoublePointState(0x02));
        assertEquals(Iec104DoublePointState.INDETERMINATE, decodeDoublePointState(0x03));
    }

    @Test
    public void parsesInvalidCp56Time2aWithoutThrowing() {
        Iec104SinglePointValue value = (Iec104SinglePointValue) decodeFirstObject(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x1E, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x80, 0x00, 0x00, 0x00, 0x00)).getValue();

        Iec104Cp56Time2a timeTag = value.getTimeTag();
        assertTrue(timeTag.isInvalid());
        assertEquals(2000, timeTag.getYear());
        assertEquals(0, timeTag.getMonth());
        assertEquals(0, timeTag.getDayOfMonth());
        assertNull(timeTag.getDateTime());
    }

    @Test
    public void preservesUnknownAsduRawInformationBytes() {
        Iec104Frame frame = decodeOne(bytes(
                0x68, 0x0F,
                0x00, 0x00, 0x00, 0x00,
                0x7F, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x03, 0x02, 0x01,
                0xAA, 0xBB));

        Iec104Asdu asdu = frame.getAsdu();
        assertEquals(Iec104AsduType.UNKNOWN, asdu.getType());
        assertEquals(1, asdu.getInformationObjects().size());

        Iec104InformationObject object = asdu.getInformationObjects().get(0);
        assertEquals(0x010203, object.getAddress());
        assertArrayEquals(bytes(0xAA, 0xBB), object.getElementBytes());
        assertNull(object.getValue());
    }

    @Test
    public void recoversAfterNoiseAndInvalidLength() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x55,
                0x68, 0x03,
                0x68, 0x04, 0x43, 0x00, 0x00, 0x00,
                0x68, 0x04, 0x83, 0x00, 0x00, 0x00));

        assertEquals(3, results.size());
        assertTrue(results.get(0).isError());
        assertEquals(1, results.get(0).getConsumedBytes());
        assertTrue(results.get(1).isSuccess());
        assertTrue(results.get(2).isSuccess());
        assertEquals(Iec104FrameType.TESTFR_ACT, results.get(1).getFrame().getType());
        assertEquals(Iec104FrameType.TESTFR_CON, results.get(2).getFrame().getType());
    }

    private Iec104InformationObject decodeFirstObject(byte[] bytes) {
        return decodeOne(bytes).getAsdu().getInformationObjects().get(0);
    }

    private Iec104Frame decodeOne(byte[] bytes) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame();
    }

    private Iec104DoublePointState decodeDoublePointState(int rawValue) {
        Iec104DoublePointValue value = (Iec104DoublePointValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x03, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x02, 0x00, 0x00,
                rawValue)).getValue();
        return value.getState();
    }

    private void assertQualityFlags(Iec104QualityDescriptor quality, int rawValue, boolean overflow) {
        assertEquals(rawValue, quality.getRawValue());
        assertTrue(quality.isInvalid());
        assertTrue(quality.isNotTopical());
        assertTrue(quality.isSubstituted());
        assertTrue(quality.isBlocked());
        assertEquals(overflow, quality.isOverflow());
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
