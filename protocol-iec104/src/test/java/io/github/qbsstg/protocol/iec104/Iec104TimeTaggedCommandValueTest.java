package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iec104TimeTaggedCommandValueTest {

    @Test
    public void parsesSingleDoubleAndRegulatingCommandsWithTimeTag() {
        Iec104SingleCommandValue single = (Iec104SingleCommandValue) decodeFirstObject(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x3A, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x70, 0x00, 0x00,
                0x81, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();
        assertEquals(Iec104AsduType.C_SC_TA_1, single.getAsduType());
        assertTrue(single.isOn());
        assertTrue(single.getQualifier().isSelect());
        assertDefaultTime(single.getTimeTag());

        Iec104DoubleCommandValue doubleCommand = (Iec104DoubleCommandValue) decodeFirstObject(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x3B, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x71, 0x00, 0x00,
                0x82, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();
        assertEquals(Iec104AsduType.C_DC_TA_1, doubleCommand.getAsduType());
        assertEquals(Iec104DoubleCommandState.ON, doubleCommand.getState());
        assertDefaultTime(doubleCommand.getTimeTag());

        Iec104RegulatingStepCommandValue regulating = (Iec104RegulatingStepCommandValue) decodeFirstObject(bytes(
                0x68, 0x15,
                0x00, 0x00, 0x00, 0x00,
                0x3C, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x72, 0x00, 0x00,
                0x82, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();
        assertEquals(Iec104AsduType.C_RC_TA_1, regulating.getAsduType());
        assertEquals(Iec104RegulatingStepCommandState.HIGHER, regulating.getState());
        assertDefaultTime(regulating.getTimeTag());
    }

    @Test
    public void parsesSetPointCommandsWithTimeTag() {
        Iec104SetPointCommandValue normalized = (Iec104SetPointCommandValue) decodeFirstObject(bytes(
                0x68, 0x17,
                0x00, 0x00, 0x00, 0x00,
                0x3D, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x73, 0x00, 0x00,
                0x00, 0x40, 0x82, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();
        assertEquals(Iec104AsduType.C_SE_TA_1, normalized.getAsduType());
        assertEquals(0.5d, normalized.getValue(), 0.000001d);
        assertDefaultTime(normalized.getTimeTag());

        Iec104SetPointCommandValue scaled = (Iec104SetPointCommandValue) decodeFirstObject(bytes(
                0x68, 0x17,
                0x00, 0x00, 0x00, 0x00,
                0x3E, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x74, 0x00, 0x00,
                0xFF, 0xFF, 0x03, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();
        assertEquals(Iec104AsduType.C_SE_TB_1, scaled.getAsduType());
        assertEquals(-1.0d, scaled.getValue(), 0.000001d);
        assertDefaultTime(scaled.getTimeTag());

        Iec104SetPointCommandValue shortFloat = (Iec104SetPointCommandValue) decodeFirstObject(bytes(
                0x68, 0x19,
                0x00, 0x00, 0x00, 0x00,
                0x3F, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x75, 0x00, 0x00,
                0x00, 0x00, 0x20, 0x41, 0x84, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();
        assertEquals(Iec104AsduType.C_SE_TC_1, shortFloat.getAsduType());
        assertEquals(10.0d, shortFloat.getValue(), 0.000001d);
        assertDefaultTime(shortFloat.getTimeTag());
    }

    @Test
    public void parsesBitstringCommandWithTimeTag() {
        Iec104BitstringCommandValue value = (Iec104BitstringCommandValue) decodeFirstObject(bytes(
                0x68, 0x19,
                0x00, 0x00, 0x00, 0x00,
                0x40, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x76, 0x00, 0x00,
                0x05, 0x00, 0x00, 0x80, 0x84, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A)).getValue();

        assertEquals(Iec104AsduType.C_BO_TA_1, value.getAsduType());
        assertEquals(0x80000005, value.getRawValue());
        assertTrue(value.isBitSet(31));
        assertDefaultTime(value.getTimeTag());
    }

    private void assertDefaultTime(Iec104Cp56Time2a timeTag) {
        assertEquals(LocalDateTime.of(2026, 5, 21, 16, 21, 1), timeTag.getDateTime());
        assertEquals(1000, timeTag.getMillisecondsWithinMinute());
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
