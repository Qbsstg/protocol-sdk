package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec104CommandValueTest {

    @Test
    public void parsesSingleCommandQualifier() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x2D, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x10, 0x00, 0x00,
                0x85));

        Iec104SingleCommandValue value = (Iec104SingleCommandValue) object.getValue();

        assertEquals(0x10, object.getAddress());
        assertEquals(Iec104AsduType.C_SC_NA_1, value.getAsduType());
        assertTrue(value.isOn());
        assertEquals(0x85, value.getQualifier().getRawValue());
        assertEquals(1, value.getQualifier().getQualifier());
        assertTrue(value.getQualifier().isSelect());
        assertNull(value.getTimeTag());
    }

    @Test
    public void parsesDoubleCommandQualifier() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x2E, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x11, 0x00, 0x00,
                0x8E));

        Iec104DoubleCommandValue value = (Iec104DoubleCommandValue) object.getValue();

        assertEquals(Iec104AsduType.C_DC_NA_1, value.getAsduType());
        assertEquals(Iec104DoubleCommandState.ON, value.getState());
        assertTrue(value.isOn());
        assertEquals(3, value.getQualifier().getQualifier());
        assertTrue(value.getQualifier().isSelect());
    }

    @Test
    public void parsesSetPointCommandValues() {
        Iec104SetPointCommandValue normalized = (Iec104SetPointCommandValue) decodeFirstObject(bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x30, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x20, 0x00, 0x00,
                0x00, 0x40, 0x82)).getValue();

        assertEquals(Iec104AsduType.C_SE_NA_1, normalized.getAsduType());
        assertEquals(Iec104MeasuredValueKind.NORMALIZED, normalized.getKind());
        assertEquals(0.5d, normalized.getValue(), 0.000001d);
        assertEquals(0x4000, normalized.getRawValue());
        assertEquals(2, normalized.getQualifier().getQualifier());
        assertTrue(normalized.getQualifier().isSelect());

        Iec104SetPointCommandValue scaled = (Iec104SetPointCommandValue) decodeFirstObject(bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x31, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x21, 0x00, 0x00,
                0xFF, 0xFF, 0x03)).getValue();

        assertEquals(Iec104AsduType.C_SE_NB_1, scaled.getAsduType());
        assertEquals(Iec104MeasuredValueKind.SCALED, scaled.getKind());
        assertEquals(-1.0d, scaled.getValue(), 0.000001d);
        assertEquals(-1, scaled.getRawValue());
        assertEquals(3, scaled.getQualifier().getQualifier());
        assertTrue(scaled.getQualifier().isExecute());

        Iec104SetPointCommandValue shortFloat = (Iec104SetPointCommandValue) decodeFirstObject(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x32, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x22, 0x00, 0x00,
                0x00, 0x00, 0x20, 0x41, 0x84)).getValue();

        assertEquals(Iec104AsduType.C_SE_NC_1, shortFloat.getAsduType());
        assertEquals(Iec104MeasuredValueKind.SHORT_FLOAT, shortFloat.getKind());
        assertEquals(10.0d, shortFloat.getValue(), 0.000001d);
        assertEquals(4, shortFloat.getQualifier().getQualifier());
        assertTrue(shortFloat.getQualifier().isSelect());
    }

    @Test
    public void parsesInterrogationCommandQualifier() {
        Iec104InterrogationCommandValue station = (Iec104InterrogationCommandValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x64, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x14)).getValue();

        assertEquals(Iec104AsduType.C_IC_NA_1, station.getAsduType());
        assertEquals(20, station.getQualifierOfInterrogation());
        assertTrue(station.isStationInterrogation());
        assertNull(station.getGroupNumber());

        Iec104InterrogationCommandValue groupOne = (Iec104InterrogationCommandValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x64, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x15)).getValue();

        assertEquals(21, groupOne.getQualifierOfInterrogation());
        assertEquals(Integer.valueOf(1), groupOne.getGroupNumber());
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
