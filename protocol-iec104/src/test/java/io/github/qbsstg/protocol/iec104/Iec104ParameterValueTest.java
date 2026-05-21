package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec104ParameterValueTest {

    @Test
    public void parsesMeasuredParameterValues() {
        Iec104ParameterMeasuredValue normalized = (Iec104ParameterMeasuredValue) decodeFirstObject(bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x6E, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x90, 0x00, 0x00,
                0x00, 0x40, 0xC2)).getValue();

        assertEquals(Iec104AsduType.P_ME_NA_1, normalized.getAsduType());
        assertEquals(Iec104MeasuredValueKind.NORMALIZED, normalized.getKind());
        assertEquals(0.5d, normalized.getValue(), 0.000001d);
        assertEquals(0x4000, normalized.getRawValue());
        assertEquals(0xC2, normalized.getQualifier().getRawValue());
        assertEquals(2, normalized.getQualifier().getKindOfParameter());
        assertTrue(normalized.getQualifier().isLocalParameterChanged());
        assertTrue(normalized.getQualifier().isParameterNotInOperation());
        assertNull(normalized.getTimeTag());

        Iec104ParameterMeasuredValue scaled = (Iec104ParameterMeasuredValue) decodeFirstObject(bytes(
                0x68, 0x10,
                0x00, 0x00, 0x00, 0x00,
                0x6F, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x91, 0x00, 0x00,
                0xFF, 0xFF, 0x41)).getValue();

        assertEquals(Iec104AsduType.P_ME_NB_1, scaled.getAsduType());
        assertEquals(Iec104MeasuredValueKind.SCALED, scaled.getKind());
        assertEquals(-1.0d, scaled.getValue(), 0.000001d);
        assertEquals(-1, scaled.getRawValue());
        assertEquals(1, scaled.getQualifier().getKindOfParameter());
        assertTrue(scaled.getQualifier().isLocalParameterChanged());
        assertFalse(scaled.getQualifier().isParameterNotInOperation());

        Iec104ParameterMeasuredValue shortFloat = (Iec104ParameterMeasuredValue) decodeFirstObject(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x70, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x92, 0x00, 0x00,
                0x00, 0x00, 0x20, 0x41, 0x83)).getValue();

        assertEquals(Iec104AsduType.P_ME_NC_1, shortFloat.getAsduType());
        assertEquals(Iec104MeasuredValueKind.SHORT_FLOAT, shortFloat.getKind());
        assertEquals(10.0d, shortFloat.getValue(), 0.000001d);
        assertEquals(0x41200000, shortFloat.getRawValue());
        assertEquals(3, shortFloat.getQualifier().getKindOfParameter());
        assertFalse(shortFloat.getQualifier().isLocalParameterChanged());
        assertTrue(shortFloat.getQualifier().isParameterNotInOperation());
    }

    @Test
    public void parsesSequentialMeasuredParameters() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x13,
                0x00, 0x00, 0x00, 0x00,
                0x6E, 0x82, 0x06, 0x00,
                0x01, 0x00, 0xA0, 0x00, 0x00,
                0x00, 0x40, 0x01,
                0x00, 0xC0, 0x02));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        List<Iec104InformationObject> objects = results.get(0).getFrame().getAsdu().getInformationObjects();
        assertEquals(2, objects.size());
        assertEquals(0xA0, objects.get(0).getAddress());
        assertEquals(0xA1, objects.get(1).getAddress());

        Iec104ParameterMeasuredValue first = (Iec104ParameterMeasuredValue) objects.get(0).getValue();
        Iec104ParameterMeasuredValue second = (Iec104ParameterMeasuredValue) objects.get(1).getValue();
        assertEquals(0.5d, first.getValue(), 0.000001d);
        assertEquals(-0.5d, second.getValue(), 0.000001d);
        assertEquals(1, first.getQualifier().getKindOfParameter());
        assertEquals(2, second.getQualifier().getKindOfParameter());
    }

    @Test
    public void parsesParameterActivationQualifier() {
        Iec104ParameterActivationValue value = (Iec104ParameterActivationValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x71, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x03)).getValue();

        assertEquals(Iec104AsduType.P_AC_NA_1, value.getAsduType());
        assertEquals(3, value.getRawQualifier());
        assertTrue(value.isPersistentCyclicOrPeriodicTransmission());
        assertNull(value.getTimeTag());
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
