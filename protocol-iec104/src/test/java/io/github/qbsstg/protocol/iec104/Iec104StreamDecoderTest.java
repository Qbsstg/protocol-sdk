package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Iec104StreamDecoderTest {

    @Test
    public void decodesStartDtActivation() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(0x68, 0x04, 0x07, 0x00, 0x00, 0x00));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(Iec104FrameType.STARTDT_ACT, results.get(0).getFrame().getType());
        assertEquals(6, results.get(0).getConsumedBytes());
    }

    @Test
    public void decodesSFormatReceiveSequence() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(0x68, 0x04, 0x01, 0x00, 0x02, 0x00));

        assertEquals(1, results.size());
        assertEquals(Iec104FrameType.S_FORMAT, results.get(0).getFrame().getType());
        assertEquals(Integer.valueOf(1), results.get(0).getFrame().getReceiveSequence());
    }

    @Test
    public void decodesIFormatInterrogationActivation() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x64, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00, 0x14));

        assertEquals(1, results.size());
        Iec104Frame frame = results.get(0).getFrame();
        assertEquals(Iec104FrameType.I_FORMAT, frame.getType());
        assertEquals(Integer.valueOf(0), frame.getSendSequence());
        assertEquals(Integer.valueOf(0), frame.getReceiveSequence());
        assertEquals(Integer.valueOf(100), frame.getAsduType());
        assertEquals(Integer.valueOf(6), frame.getCauseOfTransmission());

        Iec104Asdu asdu = frame.getAsdu();
        assertNotNull(asdu);
        assertEquals(Iec104AsduType.C_IC_NA_1, asdu.getType());
        assertEquals(Iec104CauseOfTransmission.ACTIVATION, asdu.getCauseOfTransmission());
        assertEquals(0, asdu.getOriginatorAddress());
        assertEquals(1, asdu.getCommonAddress());
        assertEquals(1, asdu.getVariableStructureQualifier().getNumberOfObjects());
        assertEquals(1, asdu.getInformationObjects().size());
        assertEquals(0, asdu.getInformationObjects().get(0).getAddress());
        assertArrayEquals(bytes(0x14), asdu.getInformationObjects().get(0).getElementBytes());
    }

    @Test
    public void decodesInterrogationConfirmationAndTermination() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x0E,
                0x02, 0x00, 0x02, 0x00,
                0x64, 0x01, 0x07, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00, 0x14,
                0x68, 0x0E,
                0x04, 0x00, 0x02, 0x00,
                0x64, 0x01, 0x0A, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00, 0x14));

        assertEquals(2, results.size());
        assertEquals(Iec104CauseOfTransmission.ACTIVATION_CONFIRMATION,
                results.get(0).getFrame().getAsdu().getCauseOfTransmission());
        assertEquals(Iec104CauseOfTransmission.ACTIVATION_TERMINATION,
                results.get(1).getFrame().getAsdu().getCauseOfTransmission());
        assertEquals(Integer.valueOf(1), results.get(0).getFrame().getSendSequence());
        assertEquals(Integer.valueOf(2), results.get(1).getFrame().getSendSequence());
    }

    @Test
    public void decodesSinglePointInformationAddress() {
        Iec104Frame frame = decodeOne(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x01, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00, 0x01));

        Iec104Asdu asdu = frame.getAsdu();
        assertEquals(Iec104AsduType.M_SP_NA_1, asdu.getType());
        assertEquals(Iec104CauseOfTransmission.SPONTANEOUS, asdu.getCauseOfTransmission());
        assertEquals(1, asdu.getInformationObjects().get(0).getAddress());
        assertArrayEquals(bytes(0x01), asdu.getInformationObjects().get(0).getElementBytes());

        Iec104SinglePointValue value = (Iec104SinglePointValue) asdu.getInformationObjects().get(0).getValue();
        assertEquals(Iec104AsduType.M_SP_NA_1, value.getAsduType());
        assertTrue(value.isOn());
        assertEquals(0x01, value.getQuality().getRawValue());
        assertFalse(value.getQuality().isInvalid());
        assertEquals(null, value.getTimeTag());
    }

    @Test
    public void decodesDoublePointInformationAddress() {
        Iec104Frame frame = decodeOne(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x03, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x02, 0x00, 0x00, 0x02));

        Iec104Asdu asdu = frame.getAsdu();
        assertEquals(Iec104AsduType.M_DP_NA_1, asdu.getType());
        assertEquals(2, asdu.getInformationObjects().get(0).getAddress());
        assertArrayEquals(bytes(0x02), asdu.getInformationObjects().get(0).getElementBytes());

        Iec104DoublePointValue value = (Iec104DoublePointValue) asdu.getInformationObjects().get(0).getValue();
        assertEquals(Iec104DoublePointState.ON, value.getState());
        assertTrue(value.isOn());
        assertEquals(0x02, value.getQuality().getRawValue());
    }

    @Test
    public void decodesMeasuredValueInformationAddresses() {
        Iec104MeasuredValue normalized = (Iec104MeasuredValue) assertMeasuredFrame(Iec104AsduType.M_ME_NA_1, 0x4001, bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x09, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x40, 0x00, 0x00, 0x40, 0x00), bytes(0x00, 0x40, 0x00)).getValue();
        assertEquals(Iec104MeasuredValueKind.NORMALIZED, normalized.getKind());
        assertEquals(0.5d, normalized.getValue(), 0.000001d);
        assertEquals(0x4000, normalized.getRawValue());
        assertFalse(normalized.getQuality().isOverflow());

        Iec104MeasuredValue scaled = (Iec104MeasuredValue) assertMeasuredFrame(Iec104AsduType.M_ME_NB_1, 0x4002, bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x0B, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x02, 0x40, 0x00, 0x34, 0x12, 0x00), bytes(0x34, 0x12, 0x00)).getValue();
        assertEquals(Iec104MeasuredValueKind.SCALED, scaled.getKind());
        assertEquals(4660d, scaled.getValue(), 0.000001d);
        assertEquals(0x1234, scaled.getRawValue());

        Iec104MeasuredValue shortFloat = (Iec104MeasuredValue) assertMeasuredFrame(Iec104AsduType.M_ME_NC_1, 0x4003, bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x0D, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x03, 0x40, 0x00, 0x00, 0x00, 0x20, 0x41, 0x00), bytes(0x00, 0x00, 0x20, 0x41, 0x00)).getValue();
        assertEquals(Iec104MeasuredValueKind.SHORT_FLOAT, shortFloat.getKind());
        assertEquals(10.0d, shortFloat.getValue(), 0.000001d);
    }

    @Test
    public void decodesSinglePointSoeInformationAddress() {
        Iec104Frame frame = decodeOne(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x1E, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x01, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        Iec104Asdu asdu = frame.getAsdu();
        assertEquals(Iec104AsduType.M_SP_TB_1, asdu.getType());
        assertEquals(1, asdu.getInformationObjects().get(0).getAddress());
        assertArrayEquals(bytes(0x01, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A),
                asdu.getInformationObjects().get(0).getElementBytes());

        Iec104SinglePointValue value = (Iec104SinglePointValue) asdu.getInformationObjects().get(0).getValue();
        assertTrue(value.isOn());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
        assertEquals(1000, value.getTimeTag().getMillisecondsWithinMinute());
        assertEquals(21, value.getTimeTag().getMinute());
        assertEquals(16, value.getTimeTag().getHour());
    }

    @Test
    public void decodesSequentialInformationObjectAddresses() {
        Iec104Frame frame = decodeOne(bytes(
                0x68, 0x0F,
                0x00, 0x00, 0x00, 0x00,
                0x01, 0x82, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x01, 0x00));

        Iec104Asdu asdu = frame.getAsdu();
        assertTrue(asdu.getVariableStructureQualifier().isSequence());
        assertEquals(2, asdu.getInformationObjects().size());
        assertEquals(1, asdu.getInformationObjects().get(0).getAddress());
        assertEquals(2, asdu.getInformationObjects().get(1).getAddress());
        assertArrayEquals(bytes(0x01), asdu.getInformationObjects().get(0).getElementBytes());
        assertArrayEquals(bytes(0x00), asdu.getInformationObjects().get(1).getElementBytes());
    }

    @Test
    public void keepsPartialFrameUntilMoreBytesArrive() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        assertTrue(decoder.decode(bytes(0x68, 0x04, 0x43)).isEmpty());
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(0x00, 0x00, 0x00));

        assertEquals(1, results.size());
        assertEquals(Iec104FrameType.TESTFR_ACT, results.get(0).getFrame().getType());
    }

    @Test
    public void skipsNoiseBeforeStartByte() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(0x01, 0x02, 0x68, 0x04, 0x0B, 0x00, 0x00, 0x00));

        assertEquals(1, results.size());
        assertEquals(Iec104FrameType.STARTDT_CON, results.get(0).getFrame().getType());
        assertArrayEquals(bytes(0x68, 0x04, 0x0B, 0x00, 0x00, 0x00), results.get(0).getFrame().getRawBytes());
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }

    private Iec104Frame decodeOne(byte[] bytes) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame();
    }

    private Iec104InformationObject assertMeasuredFrame(Iec104AsduType expectedType, int expectedAddress,
                                                        byte[] frameBytes, byte[] expectedElementBytes) {
        Iec104Frame frame = decodeOne(frameBytes);
        Iec104Asdu asdu = frame.getAsdu();
        assertEquals(expectedType, asdu.getType());
        assertEquals(expectedAddress, asdu.getInformationObjects().get(0).getAddress());
        assertArrayEquals(expectedElementBytes, asdu.getInformationObjects().get(0).getElementBytes());
        return asdu.getInformationObjects().get(0);
    }
}
