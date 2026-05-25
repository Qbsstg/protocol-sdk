package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec103StreamDecoderTest {

    @Test
    public void decodesSingleCharacterAcknowledgement() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();

        List<ParseResult<Iec103Frame>> results = decoder.decode(bytes(0xE5));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(Iec103FrameFormat.SINGLE_CHARACTER, results.get(0).getFrame().getFormat());
        assertArrayEquals(bytes(0xE5), results.get(0).getFrame().getRawBytes());
        assertNull(results.get(0).getFrame().getAsdu());
    }

    @Test
    public void decodesFixedLengthFrameWithOneOctetLinkAddress() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();

        List<ParseResult<Iec103Frame>> results = decoder.decode(bytes(0x10, 0x49, 0x01, 0x4A, 0x16));

        assertEquals(1, results.size());
        Iec103Frame frame = results.get(0).getFrame();
        assertEquals(Iec103FrameFormat.FIXED_LENGTH, frame.getFormat());
        assertEquals(Integer.valueOf(1), frame.getLinkAddress());
        assertTrue(frame.getLinkControl().isPrimary());
        assertEquals(Iec103LinkFunction.REQUEST_LINK_STATUS, frame.getLinkControl().getFunction());
        assertNull(frame.getAsdu());
    }

    @Test
    public void decodesFixedLengthFrameWithTwoOctetLinkAddress() {
        Iec103ParserConfig config = Iec103ParserConfig.builder().linkAddressLength(2).build();
        Iec103StreamDecoder decoder = new Iec103StreamDecoder(config);

        List<ParseResult<Iec103Frame>> results = decoder.decode(bytes(0x10, 0x0B, 0x34, 0x12, 0x51, 0x16));

        assertEquals(1, results.size());
        Iec103Frame frame = results.get(0).getFrame();
        assertEquals(Integer.valueOf(0x1234), frame.getLinkAddress());
        assertEquals(Iec103LinkFunction.RESPOND_LINK_STATUS, frame.getLinkControl().getFunction());
    }

    @Test
    public void decodesVariableLengthTimeTaggedProtectionEvent() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();

        List<ParseResult<Iec103Frame>> results = decoder.decode(variableFrame(0x08, 0x01, bytes(
                0x01, 0x01, 0x01, 0x01,
                0x10, 0x01, 0xD2, 0xE8, 0x03, 0x15, 0x10)));

        assertEquals(1, results.size());
        Iec103Frame frame = results.get(0).getFrame();
        assertEquals(Iec103FrameFormat.VARIABLE_LENGTH, frame.getFormat());
        assertEquals(Iec103LinkFunction.RESPOND_USER_DATA, frame.getLinkControl().getFunction());
        assertEquals(Iec103AsduType.TIME_TAGGED_MESSAGE, frame.getAsdu().getType());
        assertEquals(Iec103CauseOfTransmission.SPONTANEOUS, frame.getAsdu().getCauseOfTransmission());
        assertEquals(1, frame.getAsdu().getCommonAddress());

        Iec103InformationElement element = frame.getAsdu().getInformationElements().get(0);
        assertEquals(0x10, element.getFunctionType());
        assertEquals(0x01, element.getInformationNumber());
        Iec103ProtectionEventValue value = (Iec103ProtectionEventValue) element.getValue();
        assertEquals(Iec103ProtectionEventState.ON, value.getEventState());
        assertTrue(value.isOn());
        assertEquals(0xD2, value.getQuality().getRawValue());
        assertTrue(value.getQuality().isInvalid());
        assertTrue(value.getQuality().isNotTopical());
        assertFalse(value.getQuality().isSubstituted());
        assertTrue(value.getQuality().isBlocked());
        assertEquals(1000, value.getTimeTag().getMillisecondsWithinMinute());
        assertEquals(21, value.getTimeTag().getMinute());
        assertEquals(16, value.getTimeTag().getHour());
    }

    @Test
    public void buffersIncompleteVariableFrameUntilMoreBytesArrive() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();
        byte[] frame = variableFrame(0x08, 0x01, bytes(
                0x01, 0x01, 0x01, 0x01,
                0x10, 0x01, 0xD2, 0xE8, 0x03, 0x15, 0x10));

        assertTrue(decoder.decode(bytes(frame, 0, 5)).isEmpty());
        List<ParseResult<Iec103Frame>> results = decoder.decode(bytes(frame, 5, frame.length - 5));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(Iec103AsduType.TIME_TAGGED_MESSAGE, results.get(0).getFrame().getAsdu().getType());
    }

    @Test
    public void decodesConcatenatedFrames() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();

        List<ParseResult<Iec103Frame>> results = decoder.decode(concat(bytes(0xE5),
                variableFrame(0x08, 0x01, bytes(
                        0x01, 0x01, 0x01, 0x01,
                        0x10, 0x01, 0xD2, 0xE8, 0x03, 0x15, 0x10))));

        assertEquals(2, results.size());
        assertEquals(Iec103FrameFormat.SINGLE_CHARACTER, results.get(0).getFrame().getFormat());
        assertEquals(Iec103FrameFormat.VARIABLE_LENGTH, results.get(1).getFrame().getFormat());
    }

    @Test
    public void rejectsBadChecksumAndContinues() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();
        byte[] bad = variableFrame(0x08, 0x01, bytes(
                0x01, 0x01, 0x01, 0x01,
                0x10, 0x01, 0xD2, 0xE8, 0x03, 0x15, 0x10));
        bad[bad.length - 2] = 0x7F;

        List<ParseResult<Iec103Frame>> results = decoder.decode(concat(bad, bytes(0xE5)));

        assertEquals(2, results.size());
        assertTrue(results.get(0).isError());
        assertTrue(results.get(0).getMessage().contains("Invalid IEC103 checksum"));
        assertTrue(results.get(1).isSuccess());
        assertEquals(Iec103FrameFormat.SINGLE_CHARACTER, results.get(1).getFrame().getFormat());
    }

    @Test
    public void recoversFromNoiseBeforeFrame() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();

        List<ParseResult<Iec103Frame>> results = decoder.decode(bytes(0x55, 0xE5));

        assertEquals(2, results.size());
        assertTrue(results.get(0).isError());
        assertEquals(1, results.get(0).getConsumedBytes());
        assertTrue(results.get(1).isSuccess());
    }

    @Test
    public void rejectsInvalidRepeatedLength() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();

        List<ParseResult<Iec103Frame>> results = decoder.decode(bytes(0x68, 0x02, 0x03, 0xE5));

        assertEquals(3, results.size());
        assertTrue(results.get(0).isError());
        assertTrue(results.get(1).isError());
        assertEquals(2, results.get(1).getConsumedBytes());
        assertTrue(results.get(2).isSuccess());
        assertEquals(Iec103FrameFormat.SINGLE_CHARACTER, results.get(2).getFrame().getFormat());
    }

    @Test
    public void rejectsFramesAboveConfiguredMaximumLength() {
        Iec103ParserConfig config = Iec103ParserConfig.builder().maxFrameLength(10).build();
        Iec103StreamDecoder decoder = new Iec103StreamDecoder(config);

        List<ParseResult<Iec103Frame>> results = decoder.decode(variableFrame(0x08, 0x01, bytes(
                0x01, 0x01, 0x01, 0x01,
                0x10, 0x01, 0xD2, 0xE8, 0x03, 0x15, 0x10)));

        assertTrue(results.get(0).isError());
        assertTrue(results.get(0).getMessage().contains("maxFrameLength"));
    }

    static byte[] variableFrame(int control, int linkAddress, byte[] asdu) {
        byte[] payload = new byte[2 + asdu.length];
        payload[0] = (byte) control;
        payload[1] = (byte) linkAddress;
        System.arraycopy(asdu, 0, payload, 2, asdu.length);
        int checksum = checksum(payload);
        byte[] frame = new byte[6 + payload.length];
        frame[0] = 0x68;
        frame[1] = (byte) payload.length;
        frame[2] = (byte) payload.length;
        frame[3] = 0x68;
        System.arraycopy(payload, 0, frame, 4, payload.length);
        frame[4 + payload.length] = (byte) checksum;
        frame[5 + payload.length] = 0x16;
        return frame;
    }

    static byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }

    static byte[] bytes(byte[] source, int offset, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(source, offset, bytes, 0, length);
        return bytes;
    }

    static byte[] concat(byte[] first, byte[] second) {
        byte[] bytes = new byte[first.length + second.length];
        System.arraycopy(first, 0, bytes, 0, first.length);
        System.arraycopy(second, 0, bytes, first.length, second.length);
        return bytes;
    }

    private static int checksum(byte[] bytes) {
        int value = 0;
        for (int i = 0; i < bytes.length; i++) {
            value = (value + (bytes[i] & 0xFF)) & 0xFF;
        }
        return value;
    }
}
