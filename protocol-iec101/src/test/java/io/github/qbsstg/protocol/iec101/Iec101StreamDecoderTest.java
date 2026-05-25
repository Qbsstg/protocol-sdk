package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec101StreamDecoderTest {

    @Test
    public void decodesSingleCharacterAcknowledgement() {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();

        List<ParseResult<Iec101Frame>> results = decoder.decode(bytes(0xE5));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(Iec101FrameFormat.SINGLE_CHARACTER, results.get(0).getFrame().getFormat());
        assertArrayEquals(bytes(0xE5), results.get(0).getFrame().getRawBytes());
        assertNull(results.get(0).getFrame().getAsdu());
    }

    @Test
    public void decodesFixedLengthFrameWithOneOctetLinkAddress() {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();

        List<ParseResult<Iec101Frame>> results = decoder.decode(bytes(0x10, 0x49, 0x01, 0x4A, 0x16));

        assertEquals(1, results.size());
        Iec101Frame frame = results.get(0).getFrame();
        assertEquals(Iec101FrameFormat.FIXED_LENGTH, frame.getFormat());
        assertEquals(Integer.valueOf(1), frame.getLinkAddress());
        assertTrue(frame.getLinkControl().isPrimary());
        assertEquals(Iec101LinkFunction.REQUEST_LINK_STATUS, frame.getLinkControl().getFunction());
        assertNull(frame.getAsdu());
    }

    @Test
    public void decodesFixedLengthFrameWithTwoOctetLinkAddress() {
        Iec101ParserConfig config = Iec101ParserConfig.builder().linkAddressLength(2).build();
        Iec101StreamDecoder decoder = new Iec101StreamDecoder(config);

        List<ParseResult<Iec101Frame>> results = decoder.decode(bytes(0x10, 0x0B, 0x34, 0x12, 0x51, 0x16));

        assertEquals(1, results.size());
        Iec101Frame frame = results.get(0).getFrame();
        assertEquals(Integer.valueOf(0x1234), frame.getLinkAddress());
        assertEquals(Iec101LinkFunction.RESPOND_LINK_STATUS, frame.getLinkControl().getFunction());
    }

    @Test
    public void decodesVariableLengthSinglePointAsdu() {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();

        List<ParseResult<Iec101Frame>> results = decoder.decode(variableFrame(0x08, 0x01, bytes(
                0x01, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x01)));

        assertEquals(1, results.size());
        Iec101Frame frame = results.get(0).getFrame();
        assertEquals(Iec101FrameFormat.VARIABLE_LENGTH, frame.getFormat());
        assertEquals(Iec101LinkFunction.RESPOND_USER_DATA, frame.getLinkControl().getFunction());
        assertEquals(Iec101AsduType.M_SP_NA_1, frame.getAsdu().getType());
        assertEquals(3, frame.getAsdu().getCauseCode());
        assertEquals(1, frame.getAsdu().getCommonAddress());

        Iec101InformationObject object = frame.getAsdu().getInformationObjects().get(0);
        Iec101SinglePointValue value = (Iec101SinglePointValue) object.getValue();
        assertEquals(1, object.getAddress());
        assertTrue(value.isOn());
        assertEquals(0x01, value.getQuality().getRawValue());
    }

    @Test
    public void buffersIncompleteVariableFrameUntilMoreBytesArrive() {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();
        byte[] frame = variableFrame(0x08, 0x01, bytes(
                0x01, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x01));

        assertTrue(decoder.decode(bytes(frame, 0, 5)).isEmpty());
        List<ParseResult<Iec101Frame>> results = decoder.decode(bytes(frame, 5, frame.length - 5));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(Iec101AsduType.M_SP_NA_1, results.get(0).getFrame().getAsdu().getType());
    }

    @Test
    public void decodesConcatenatedFrames() {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();

        List<ParseResult<Iec101Frame>> results = decoder.decode(concat(bytes(0xE5),
                variableFrame(0x08, 0x01, bytes(
                        0x01, 0x01, 0x03, 0x00,
                        0x01, 0x00, 0x01, 0x00, 0x00,
                        0x01))));

        assertEquals(2, results.size());
        assertEquals(Iec101FrameFormat.SINGLE_CHARACTER, results.get(0).getFrame().getFormat());
        assertEquals(Iec101FrameFormat.VARIABLE_LENGTH, results.get(1).getFrame().getFormat());
    }

    @Test
    public void rejectsBadChecksumAndContinues() {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();
        byte[] bad = variableFrame(0x08, 0x01, bytes(
                0x01, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x01));
        bad[bad.length - 2] = 0x00;

        List<ParseResult<Iec101Frame>> results = decoder.decode(concat(bad, bytes(0xE5)));

        assertEquals(2, results.size());
        assertTrue(results.get(0).isError());
        assertTrue(results.get(0).getMessage().contains("Invalid IEC101 checksum"));
        assertTrue(results.get(1).isSuccess());
        assertEquals(Iec101FrameFormat.SINGLE_CHARACTER, results.get(1).getFrame().getFormat());
    }

    @Test
    public void recoversFromNoiseBeforeFrame() {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();

        List<ParseResult<Iec101Frame>> results = decoder.decode(bytes(0x55, 0xE5));

        assertEquals(2, results.size());
        assertTrue(results.get(0).isError());
        assertEquals(1, results.get(0).getConsumedBytes());
        assertTrue(results.get(1).isSuccess());
    }

    @Test
    public void rejectsInvalidRepeatedLength() {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();

        List<ParseResult<Iec101Frame>> results = decoder.decode(bytes(0x68, 0x02, 0x03, 0xE5));

        assertEquals(3, results.size());
        assertTrue(results.get(0).isError());
        assertTrue(results.get(1).isError());
        assertEquals(2, results.get(1).getConsumedBytes());
        assertTrue(results.get(2).isSuccess());
        assertEquals(Iec101FrameFormat.SINGLE_CHARACTER, results.get(2).getFrame().getFormat());
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
