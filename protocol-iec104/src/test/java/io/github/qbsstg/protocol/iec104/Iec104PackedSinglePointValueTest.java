package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class Iec104PackedSinglePointValueTest {

    @Test
    public void parsesPackedSinglePointValue() {
        Iec104PackedSinglePointValue value = (Iec104PackedSinglePointValue) decodeFirstObject(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x14, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x60, 0x00, 0x00,
                0x05, 0x00, 0x02, 0x80, 0xD0)).getValue();

        assertEquals(Iec104AsduType.M_PS_NA_1, value.getAsduType());
        assertEquals(0x0005, value.getStatusBits());
        assertEquals(0x8002, value.getChangeDetectionBits());
        assertTrue(value.isOn(0));
        assertFalse(value.isOn(1));
        assertTrue(value.isOn(2));
        assertTrue(value.hasStatusChanged(1));
        assertFalse(value.hasStatusChanged(2));
        assertTrue(value.hasStatusChanged(15));
        assertEquals(0xD0, value.getQuality().getRawValue());
        assertTrue(value.getQuality().isInvalid());
        assertTrue(value.getQuality().isNotTopical());
        assertTrue(value.getQuality().isBlocked());
        assertFalse(value.getQuality().isOverflow());
    }

    @Test
    public void parsesSequentialPackedSinglePointValues() {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes(
                0x68, 0x17,
                0x00, 0x00, 0x00, 0x00,
                0x14, 0x82, 0x03, 0x00,
                0x01, 0x00, 0x70, 0x00, 0x00,
                0x01, 0x00, 0x01, 0x00, 0x00,
                0x02, 0x00, 0x00, 0x00, 0x10));

        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        List<Iec104InformationObject> objects = results.get(0).getFrame().getAsdu().getInformationObjects();
        assertEquals(2, objects.size());
        assertEquals(0x70, objects.get(0).getAddress());
        assertEquals(0x71, objects.get(1).getAddress());

        Iec104PackedSinglePointValue first = (Iec104PackedSinglePointValue) objects.get(0).getValue();
        Iec104PackedSinglePointValue second = (Iec104PackedSinglePointValue) objects.get(1).getValue();
        assertTrue(first.isOn(0));
        assertTrue(first.hasStatusChanged(0));
        assertFalse(second.isOn(0));
        assertTrue(second.isOn(1));
        assertFalse(second.hasStatusChanged(1));
        assertTrue(second.getQuality().isBlocked());
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsPointIndexOutsidePackedSinglePointRange() {
        Iec104PackedSinglePointValue value = new Iec104PackedSinglePointValue(
                Iec104AsduType.M_PS_NA_1, 0, 0, null);

        value.isOn(16);
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
