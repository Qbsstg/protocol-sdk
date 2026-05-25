package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iec104MalformedAsduModeTest {

    @Test
    public void keepsPermissiveMalformedAsduBehaviorByDefault() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(truncatedSecondSinglePointObject());

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        assertEquals(1, results.get(0).getFrame().getAsdu().getInformationObjects().size());
        assertEquals(1, results.get(0).getFrame().getAsdu().getInformationObjects().get(0).getAddress());
    }

    @Test
    public void strictModeRejectsTruncatedInformationElement() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder(true);

        List<ParseResult<Iec104Frame>> results = decoder.decode(truncatedSecondSinglePointObject());

        assertEquals(1, results.size());
        assertTrue(results.get(0).isError());
        assertEquals(19, results.get(0).getConsumedBytes());
        assertTrue(results.get(0).getMessage().contains("Truncated IEC104 information element"));
        assertTrue(results.get(0).getMessage().contains("M_SP_NA_1"));
    }

    @Test
    public void strictModeConsumesMalformedFrameAndContinues() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder(true);

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x11,
                0x00, 0x00, 0x00, 0x00,
                0x01, 0x02, 0x03, 0x00,
                0x01, 0x00,
                0x01, 0x00, 0x00, 0x01,
                0x02, 0x00, 0x00,
                0x68, 0x04, 0x43, 0x00, 0x00, 0x00));

        assertEquals(2, results.size());
        assertTrue(results.get(0).isError());
        assertEquals(19, results.get(0).getConsumedBytes());
        assertTrue(results.get(1).isSuccess());
        assertEquals(Iec104FrameType.TESTFR_ACT, results.get(1).getFrame().getType());
    }

    @Test
    public void strictModeRejectsTruncatedSequentialInformationElements() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder(true);

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x01, 0x82, 0x03, 0x00,
                0x01, 0x00,
                0x01, 0x00, 0x00, 0x01));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isError());
        assertTrue(results.get(0).getMessage().contains("Truncated IEC104 information element"));
        assertTrue(results.get(0).getMessage().contains("expected 2 bytes, available 1"));
    }

    private byte[] truncatedSecondSinglePointObject() {
        return bytes(
                0x68, 0x11,
                0x00, 0x00, 0x00, 0x00,
                0x01, 0x02, 0x03, 0x00,
                0x01, 0x00,
                0x01, 0x00, 0x00, 0x01,
                0x02, 0x00, 0x00);
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
