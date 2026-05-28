package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec104VsqSqBoundaryTest {

    @Test
    public void parsesTypedSequenceUsingBaseAddressAndElementCount() {
        Iec104Asdu asdu = decodeOne(bytes(
                0x68, 0x16,
                0x00, 0x00, 0x00, 0x00,
                0x09, 0x83, 0x03, 0x00,
                0x01, 0x00, 0x34, 0x12, 0x00,
                0x00, 0x40, 0x00,
                0x00, 0xC0, 0x01,
                0x34, 0x12, 0xF1)).getAsdu();

        assertTrue(asdu.getVariableStructureQualifier().isSequence());
        assertEquals(3, asdu.getVariableStructureQualifier().getNumberOfObjects());

        List<Iec104InformationObject> objects = asdu.getInformationObjects();
        assertEquals(3, objects.size());
        assertMeasuredObject(objects.get(0), 0, 0x1234, bytes(0x00, 0x40, 0x00), 0x4000);
        assertMeasuredObject(objects.get(1), 1, 0x1235, bytes(0x00, 0xC0, 0x01), -0x4000);
        assertMeasuredObject(objects.get(2), 2, 0x1236, bytes(0x34, 0x12, 0xF1), 0x1234);
    }

    @Test
    public void parsesTypedNonSequenceUsingEachInformationObjectAddress() {
        Iec104Asdu asdu = decodeOne(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x01, 0x02, 0x03, 0x00,
                0x01, 0x00,
                0x10, 0x00, 0x00, 0x01,
                0x20, 0x00, 0x00, 0x00)).getAsdu();

        assertFalse(asdu.getVariableStructureQualifier().isSequence());
        assertEquals(2, asdu.getVariableStructureQualifier().getNumberOfObjects());

        List<Iec104InformationObject> objects = asdu.getInformationObjects();
        assertEquals(2, objects.size());
        assertSinglePointObject(objects.get(0), 0, 0x10, bytes(0x01), true);
        assertSinglePointObject(objects.get(1), 1, 0x20, bytes(0x00), false);
    }

    @Test
    public void preservesRawOnlySequenceAsOneRawInformationObject() {
        Iec104Asdu asdu = decodeOne(bytes(
                0x68, 0x13,
                0x00, 0x00, 0x00, 0x00,
                0x78, 0x82, 0x03, 0x00,
                0x01, 0x00, 0x50, 0x00, 0x00,
                0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF)).getAsdu();

        assertEquals(Iec104AsduType.F_FR_NA_1, asdu.getType());
        assertTrue(asdu.getVariableStructureQualifier().isSequence());
        assertEquals(2, asdu.getVariableStructureQualifier().getNumberOfObjects());

        List<Iec104InformationObject> objects = asdu.getInformationObjects();
        assertEquals(1, objects.size());
        assertEquals(0, objects.get(0).getIndex());
        assertEquals(0x50, objects.get(0).getAddress());
        assertArrayEquals(bytes(0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF), objects.get(0).getElementBytes());
        assertNull(objects.get(0).getValue());
    }

    private Iec104Frame decodeOne(byte[] bytes) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame();
    }

    private void assertMeasuredObject(Iec104InformationObject object, int index, int address,
                                      byte[] elementBytes, int rawValue) {
        assertEquals(index, object.getIndex());
        assertEquals(address, object.getAddress());
        assertArrayEquals(elementBytes, object.getElementBytes());

        Iec104MeasuredValue value = (Iec104MeasuredValue) object.getValue();
        assertEquals(Iec104AsduType.M_ME_NA_1, value.getAsduType());
        assertEquals(Iec104MeasuredValueKind.NORMALIZED, value.getKind());
        assertEquals(rawValue, value.getRawValue());
    }

    private void assertSinglePointObject(Iec104InformationObject object, int index, int address,
                                         byte[] elementBytes, boolean on) {
        assertEquals(index, object.getIndex());
        assertEquals(address, object.getAddress());
        assertArrayEquals(elementBytes, object.getElementBytes());

        Iec104SinglePointValue value = (Iec104SinglePointValue) object.getValue();
        assertEquals(Iec104AsduType.M_SP_NA_1, value.getAsduType());
        assertEquals(on, value.isOn());
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
