package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iec104ResetDelayCommandValueTest {

    @Test
    public void parsesResetProcessCommand() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x69, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x01));

        Iec104ResetProcessCommandValue value = (Iec104ResetProcessCommandValue) object.getValue();

        assertEquals(Iec104AsduType.C_RP_NA_1, value.getAsduType());
        assertEquals(1, value.getQualifierOfResetProcess());
        assertTrue(value.isGeneralResetProcess());
    }

    @Test
    public void parsesResetEventBufferCommand() {
        Iec104ResetProcessCommandValue value = (Iec104ResetProcessCommandValue) decodeFirstObject(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x69, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x02)).getValue();

        assertEquals(2, value.getQualifierOfResetProcess());
        assertTrue(value.isResetEventBuffer());
    }

    @Test
    public void parsesDelayAcquisitionCommand() {
        Iec104InformationObject object = decodeFirstObject(bytes(
                0x68, 0x0F,
                0x00, 0x00, 0x00, 0x00,
                0x6A, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0xE8, 0x03));

        Iec104DelayAcquisitionCommandValue value = (Iec104DelayAcquisitionCommandValue) object.getValue();

        assertEquals(Iec104AsduType.C_CD_NA_1, value.getAsduType());
        assertEquals(1000, value.getDelayMilliseconds());
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
