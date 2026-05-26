package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec101CommandStationServiceTest {

    @Test
    public void parsesSingleAndDoubleCommandQualifiers() {
        Iec101InformationObject singleObject = decodeFirstObject(bytes(
                0x2D, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x21, 0x00, 0x00,
                0x85));
        Iec101SingleCommandValue single = (Iec101SingleCommandValue) singleObject.getValue();
        assertEquals(Iec101AsduType.C_SC_NA_1, single.getAsduType());
        assertTrue(single.isOn());
        assertTrue(single.getQualifier().isSelect());
        assertEquals(1, single.getQualifier().getQualifier());
        assertEquals(0x85, single.getQualifier().getRawValue());

        Iec101InformationObject doubleObject = decodeFirstObject(bytes(
                0x2E, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x22, 0x00, 0x00,
                0x06));
        Iec101DoubleCommandValue doubleCommand = (Iec101DoubleCommandValue) doubleObject.getValue();
        assertEquals(Iec101AsduType.C_DC_NA_1, doubleCommand.getAsduType());
        assertEquals(Iec101DoubleCommandState.ON, doubleCommand.getState());
        assertEquals(1, doubleCommand.getQualifier().getQualifier());
    }

    @Test
    public void parsesCounterInterrogationCommand() {
        Iec101CounterInterrogationCommandValue value =
                (Iec101CounterInterrogationCommandValue) decodeFirstObject(bytes(
                        0x65, 0x01, 0x06, 0x00,
                        0x01, 0x00, 0x00, 0x00, 0x00,
                        0x82)).getValue();

        assertEquals(Iec101AsduType.C_CI_NA_1, value.getAsduType());
        assertEquals(0x82, value.getRawQualifier());
        assertEquals(2, value.getRequestQualifier());
        assertEquals(Integer.valueOf(2), value.getCounterGroupNumber());
        assertEquals(Iec101CounterFreezeResetQualifier.FREEZE_WITH_RESET, value.getFreezeResetQualifier());
    }

    @Test
    public void parsesReadCommandWithTargetAddressOnly() {
        Iec101InformationObject object = decodeFirstObject(bytes(
                0x66, 0x01, 0x05, 0x00,
                0x01, 0x00, 0x03, 0x02, 0x01));

        assertEquals(0x010203, object.getAddress());
        assertArrayEquals(new byte[0], object.getElementBytes());
        Iec101ReadCommandValue value = (Iec101ReadCommandValue) object.getValue();
        assertEquals(Iec101AsduType.C_RD_NA_1, value.getAsduType());
    }

    @Test
    public void parsesResetProcessAndDelayAcquisitionCommands() {
        Iec101ResetProcessCommandValue reset =
                (Iec101ResetProcessCommandValue) decodeFirstObject(bytes(
                        0x69, 0x01, 0x06, 0x00,
                        0x01, 0x00, 0x00, 0x00, 0x00,
                        0x02)).getValue();
        assertEquals(Iec101AsduType.C_RP_NA_1, reset.getAsduType());
        assertEquals(2, reset.getQualifierOfResetProcess());
        assertTrue(reset.isResetEventBuffer());

        Iec101DelayAcquisitionCommandValue delay =
                (Iec101DelayAcquisitionCommandValue) decodeFirstObject(bytes(
                        0x6A, 0x01, 0x06, 0x00,
                        0x01, 0x00, 0x00, 0x00, 0x00,
                        0x34, 0x12)).getValue();
        assertEquals(Iec101AsduType.C_CD_NA_1, delay.getAsduType());
        assertEquals(4660, delay.getDelayMilliseconds());
    }

    @Test
    public void preservesRawAsduWhenSupportedCommandPayloadIsTruncated() {
        byte[] asduBytes = bytes(
                0x65, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00);
        Iec101Asdu asdu = decodeAsdu(asduBytes);

        assertEquals(Iec101AsduType.C_CI_NA_1, asdu.getType());
        assertArrayEquals(asduBytes, asdu.getRawBytes());
        assertTrue(asdu.getInformationObjects().isEmpty());
    }

    @Test
    public void classifiesStationServiceSupport() {
        assertEquals(Iec101CounterInterrogationCommandValue.class,
                Iec101AsduSupport.ofTypeId(101).getValueClass());
        assertEquals(Iec101ReadCommandValue.class, Iec101AsduSupport.ofTypeId(102).getValueClass());
        assertEquals(Iec101ResetProcessCommandValue.class, Iec101AsduSupport.ofTypeId(105).getValueClass());
        assertEquals(Iec101DelayAcquisitionCommandValue.class, Iec101AsduSupport.ofTypeId(106).getValueClass());
        assertNull(Iec101AsduSupport.ofTypeId(58).getValueClass());
        assertTrue(Iec101AsduSupport.ofTypeId(58).isUnknownType());
    }

    private Iec101InformationObject decodeFirstObject(byte[] asduBytes) {
        Iec101Asdu asdu = decodeAsdu(asduBytes);
        assertEquals(1, asdu.getInformationObjects().size());
        return asdu.getInformationObjects().get(0);
    }

    private Iec101Asdu decodeAsdu(byte[] asduBytes) {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder();
        List<ParseResult<Iec101Frame>> results = decoder.decode(
                Iec101StreamDecoderTest.variableFrame(0x08, 0x01, asduBytes));
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame().getAsdu();
    }

    private byte[] bytes(int... values) {
        return Iec101StreamDecoderTest.bytes(values);
    }
}
