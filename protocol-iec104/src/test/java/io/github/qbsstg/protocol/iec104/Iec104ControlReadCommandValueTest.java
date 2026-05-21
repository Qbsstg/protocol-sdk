package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iec104ControlReadCommandValueTest {

    @Test
    public void parsesRegulatingStepCommand() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x2F, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x60, 0x00, 0x00,
                0x8E));

        Iec104RegulatingStepCommandValue value = (Iec104RegulatingStepCommandValue) object.getValue();

        assertEquals(0x60, object.getAddress());
        assertEquals(Iec104AsduType.C_RC_NA_1, value.getAsduType());
        assertEquals(Iec104RegulatingStepCommandState.HIGHER, value.getState());
        assertTrue(value.isHigher());
        assertEquals(3, value.getQualifier().getQualifier());
        assertTrue(value.getQualifier().isSelect());
    }

    @Test
    public void parsesBitstringCommand() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x12,
                0x00, 0x00, 0x00, 0x00,
                0x33, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x61, 0x00, 0x00,
                0x05, 0x00, 0x00, 0x80, 0x84));

        Iec104BitstringCommandValue value = (Iec104BitstringCommandValue) object.getValue();

        assertEquals(Iec104AsduType.C_BO_NA_1, value.getAsduType());
        assertEquals(0x80000005, value.getRawValue());
        assertTrue(value.isBitSet(0));
        assertTrue(value.isBitSet(2));
        assertTrue(value.isBitSet(31));
        assertEquals(4, value.getQualifier().getQualifier());
        assertTrue(value.getQualifier().isSelect());
    }

    @Test
    public void parsesReadCommandWithoutInformationElementBytes() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x0D,
                0x00, 0x00, 0x00, 0x00,
                0x66, 0x01, 0x05, 0x00,
                0x01, 0x00, 0x62, 0x00, 0x00));

        Iec104ReadCommandValue value = (Iec104ReadCommandValue) object.getValue();

        assertEquals(0x62, object.getAddress());
        assertArrayEquals(bytes(), object.getElementBytes());
        assertEquals(Iec104AsduType.C_RD_NA_1, value.getAsduType());
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
