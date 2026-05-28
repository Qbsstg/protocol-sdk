package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec104RawOnlyCatalogFixtureTest {

    private static final Iec104AsduType[] RAW_ONLY_TYPES = new Iec104AsduType[]{
            Iec104AsduType.M_EI_NA_1,
            Iec104AsduType.F_FR_NA_1,
            Iec104AsduType.F_SR_NA_1,
            Iec104AsduType.F_SC_NA_1,
            Iec104AsduType.F_LS_NA_1,
            Iec104AsduType.F_AF_NA_1,
            Iec104AsduType.F_SG_NA_1,
            Iec104AsduType.F_DR_TA_1
    };

    @Test
    public void preservesRawBytesForEveryRecognizedRawOnlyCatalogType() {
        for (int i = 0; i < RAW_ONLY_TYPES.length; i++) {
            Iec104AsduType type = RAW_ONLY_TYPES[i];
            int address = 0x010200 + i;
            byte[] payload = payloadFor(type);

            Iec104Frame frame = decodeOne(singleObjectFrame(type, address, payload));

            Iec104Asdu asdu = frame.getAsdu();
            assertEquals(type.getTypeId(), asdu.getTypeId());
            assertEquals(type, asdu.getType());
            assertEquals(Iec104CauseOfTransmission.SPONTANEOUS, asdu.getCauseOfTransmission());
            assertTrue(Iec104AsduSupport.of(type).isRawBytesOnly());
            assertArrayEquals(rawAsdu(type, address, payload), asdu.getRawBytes());

            assertEquals(1, asdu.getInformationObjects().size());
            Iec104InformationObject object = asdu.getInformationObjects().get(0);
            assertEquals(0, object.getIndex());
            assertEquals(address, object.getAddress());
            assertArrayEquals(payload, object.getElementBytes());
            assertNull(object.getValue());
        }
    }

    private Iec104Frame decodeOne(byte[] bytes) {
        Iec104StreamDecoder decoder = new Iec104StreamDecoder();
        List<ParseResult<Iec104Frame>> results = decoder.decode(bytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame();
    }

    private byte[] singleObjectFrame(Iec104AsduType type, int address, byte[] payload) {
        byte[] asdu = rawAsdu(type, address, payload);
        int apduLength = 4 + asdu.length;
        byte[] frame = new byte[2 + apduLength];
        frame[0] = 0x68;
        frame[1] = (byte) apduLength;
        System.arraycopy(asdu, 0, frame, 6, asdu.length);
        return frame;
    }

    private byte[] rawAsdu(Iec104AsduType type, int address, byte[] payload) {
        byte[] asdu = new byte[9 + payload.length];
        asdu[0] = (byte) type.getTypeId();
        asdu[1] = 0x01;
        asdu[2] = 0x03;
        asdu[3] = 0x00;
        asdu[4] = 0x01;
        asdu[5] = 0x00;
        asdu[6] = (byte) (address & 0xFF);
        asdu[7] = (byte) ((address >> 8) & 0xFF);
        asdu[8] = (byte) ((address >> 16) & 0xFF);
        System.arraycopy(payload, 0, asdu, 9, payload.length);
        return asdu;
    }

    private byte[] payloadFor(Iec104AsduType type) {
        switch (type) {
            case M_EI_NA_1:
                return bytes(0x11);
            case F_FR_NA_1:
                return bytes(0x01, 0x02, 0x03);
            case F_SR_NA_1:
                return bytes(0x10, 0x20);
            case F_SC_NA_1:
                return bytes(0xA0, 0xA1, 0xA2, 0xA3);
            case F_LS_NA_1:
                return bytes(0x5A);
            case F_AF_NA_1:
                return bytes(0xC1, 0xC2);
            case F_SG_NA_1:
                return bytes(0x00, 0x01, 0x02, 0x03, 0x04);
            case F_DR_TA_1:
                return bytes(0x07, 0x00, 0xE8, 0x03, 0x15, 0x10, 0x15, 0x05, 0x1A);
            default:
                throw new IllegalArgumentException("Not a raw-only fixture type: " + type);
        }
    }

    private byte[] bytes(int... values) {
        byte[] bytes = new byte[values.length];
        for (int i = 0; i < values.length; i++) {
            bytes[i] = (byte) (values[i] & 0xFF);
        }
        return bytes;
    }
}
