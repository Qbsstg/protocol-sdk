package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Iec104SdkUsageExampleTest {

    @Test
    public void parsesSinglePointSoeFrame() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x1E, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x01, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A));

        assertEquals(1, results.size());
        ParseResult<Iec104Frame> result = results.get(0);
        assertTrue(result.isSuccess());

        Iec104Frame frame = result.getFrame();
        assertEquals(Iec104FrameType.I_FORMAT, frame.getType());
        assertNotNull(frame.getAsdu());
        assertEquals(Iec104AsduType.M_SP_TB_1, frame.getAsdu().getType());

        Iec104InformationObject object = frame.getAsdu().getInformationObjects().get(0);
        Iec104SinglePointValue value = (Iec104SinglePointValue) object.getValue();

        assertEquals(1, object.getAddress());
        assertTrue(value.isOn());
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), value.getTimeTag().getDateTime());
    }

    @Test
    public void buffersIncompleteTcpPayloads() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        assertTrue(decoder.decode(bytes(0x68, 0x0E, 0x00, 0x00)).isEmpty());

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x00, 0x00,
                0x01, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00, 0x01));

        assertEquals(1, results.size());
        Iec104InformationObject object = results.get(0).getFrame().getAsdu().getInformationObjects().get(0);
        Iec104SinglePointValue value = (Iec104SinglePointValue) object.getValue();
        assertTrue(value.isOn());
    }

    @Test
    public void parsesMeasuredShortFloatValue() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();

        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x0D, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x03, 0x40, 0x00,
                0x00, 0x00, 0x20, 0x41, 0x00));

        Iec104InformationObject object = results.get(0).getFrame().getAsdu().getInformationObjects().get(0);
        Iec104MeasuredValue value = (Iec104MeasuredValue) object.getValue();

        assertEquals(0x4003, object.getAddress());
        assertEquals(Iec104MeasuredValueKind.SHORT_FLOAT, value.getKind());
        assertEquals(10.0d, value.getValue(), 0.000001d);
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
