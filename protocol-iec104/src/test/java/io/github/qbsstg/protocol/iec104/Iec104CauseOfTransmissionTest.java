package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iec104CauseOfTransmissionTest {

    @Test
    public void mapsCounterInterrogationReturnCauses() {
        assertEquals(Iec104CauseOfTransmission.REQUESTED_BY_GENERAL_COUNTER,
                Iec104CauseOfTransmission.fromCode(37));
        assertEquals(Iec104CauseOfTransmission.REQUESTED_BY_COUNTER_GROUP_1,
                Iec104CauseOfTransmission.fromCode(38));
        assertEquals(Iec104CauseOfTransmission.REQUESTED_BY_COUNTER_GROUP_2,
                Iec104CauseOfTransmission.fromCode(39));
        assertEquals(Iec104CauseOfTransmission.REQUESTED_BY_COUNTER_GROUP_3,
                Iec104CauseOfTransmission.fromCode(40));
        assertEquals(Iec104CauseOfTransmission.REQUESTED_BY_COUNTER_GROUP_4,
                Iec104CauseOfTransmission.fromCode(41));
    }

    @Test
    public void mapsUnknownDiagnosticCauses() {
        assertEquals(Iec104CauseOfTransmission.UNKNOWN_TYPE_IDENTIFICATION,
                Iec104CauseOfTransmission.fromCode(44));
        assertEquals(Iec104CauseOfTransmission.UNKNOWN_CAUSE_OF_TRANSMISSION,
                Iec104CauseOfTransmission.fromCode(45));
        assertEquals(Iec104CauseOfTransmission.UNKNOWN_COMMON_ADDRESS_OF_ASDU,
                Iec104CauseOfTransmission.fromCode(46));
        assertEquals(Iec104CauseOfTransmission.UNKNOWN_INFORMATION_OBJECT_ADDRESS,
                Iec104CauseOfTransmission.fromCode(47));
    }

    @Test
    public void keepsTestAndNegativeBitsSeparateFromCauseCode() {
        Iec104Asdu asdu = decodeAsdu(bytes(
                0x68, 0x0E,
                0x00, 0x00, 0x00, 0x00,
                0x64, 0x01, 0xEC, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x14));

        assertEquals(44, asdu.getCauseCode());
        assertEquals(Iec104CauseOfTransmission.UNKNOWN_TYPE_IDENTIFICATION, asdu.getCauseOfTransmission());
        assertTrue(asdu.isTest());
        assertTrue(asdu.isNegativeConfirm());
    }

    @Test
    public void keepsUnknownFallbackForUnmodeledCauseCodes() {
        assertEquals(Iec104CauseOfTransmission.UNKNOWN, Iec104CauseOfTransmission.fromCode(42));
    }

    private Iec104Asdu decodeAsdu(byte[] bytes) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame().getAsdu();
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
