package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec101AsduValueTest {

    @Test
    public void decodesConfigurableAddressLengths() {
        Iec101ParserConfig config = Iec101ParserConfig.builder()
                .causeOfTransmissionLength(1)
                .commonAddressLength(1)
                .informationObjectAddressLength(1)
                .build();
        Iec101Asdu asdu = decodeAsdu(config, Iec101StreamDecoderTest.variableFrame(0x08, 0x01, bytes(
                0x01, 0x01, 0x03,
                0x07, 0x09,
                0x01)));

        assertEquals(Iec101AsduType.M_SP_NA_1, asdu.getType());
        assertEquals(3, asdu.getCauseCode());
        assertEquals(7, asdu.getCommonAddress());
        assertEquals(9, asdu.getInformationObjects().get(0).getAddress());
        assertEquals(0, asdu.getOriginatorAddress());
    }

    @Test
    public void decodesSequenceInformationObjectAddresses() {
        Iec101Asdu asdu = decodeAsdu(Iec101ParserConfig.defaultUnbalanced(),
                Iec101StreamDecoderTest.variableFrame(0x08, 0x01, bytes(
                        0x01, 0x82, 0x03, 0x00,
                        0x01, 0x00, 0x10, 0x00, 0x00,
                        0x01, 0x00)));

        assertTrue(asdu.getVariableStructureQualifier().isSequence());
        assertEquals(2, asdu.getInformationObjects().size());
        assertEquals(0x10, asdu.getInformationObjects().get(0).getAddress());
        assertEquals(0x11, asdu.getInformationObjects().get(1).getAddress());
    }

    @Test
    public void decodesMeasuredValues() {
        Iec101MeasuredValue normalized = (Iec101MeasuredValue) decodeFirstValue(bytes(
                0x09, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x01, 0x40, 0x00,
                0x00, 0x40, 0x00));
        assertEquals(Iec101MeasuredValueKind.NORMALIZED, normalized.getKind());
        assertEquals(0.5d, normalized.getValue(), 0.000001d);

        Iec101MeasuredValue scaled = (Iec101MeasuredValue) decodeFirstValue(bytes(
                0x0B, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x02, 0x40, 0x00,
                0x34, 0x12, 0x00));
        assertEquals(Iec101MeasuredValueKind.SCALED, scaled.getKind());
        assertEquals(4660.0d, scaled.getValue(), 0.000001d);

        Iec101MeasuredValue shortFloat = (Iec101MeasuredValue) decodeFirstValue(bytes(
                0x0D, 0x01, 0x03, 0x00,
                0x01, 0x00, 0x03, 0x40, 0x00,
                0x00, 0x00, 0x20, 0x41, 0x00));
        assertEquals(Iec101MeasuredValueKind.SHORT_FLOAT, shortFloat.getKind());
        assertEquals(10.0d, shortFloat.getValue(), 0.000001d);
    }

    @Test
    public void decodesCommands() {
        Iec101SingleCommandValue single = (Iec101SingleCommandValue) decodeFirstValue(bytes(
                0x2D, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x81));
        assertTrue(single.isOn());
        assertTrue(single.getQualifier().isSelect());

        Iec101DoubleCommandValue doubleCommand = (Iec101DoubleCommandValue) decodeFirstValue(bytes(
                0x2E, 0x01, 0x06, 0x00,
                0x01, 0x00, 0x00, 0x00, 0x00,
                0x02));
        assertEquals(Iec101DoubleCommandState.ON, doubleCommand.getState());

        Iec101InterrogationCommandValue interrogation =
                (Iec101InterrogationCommandValue) decodeFirstValue(bytes(
                        0x64, 0x01, 0x06, 0x00,
                        0x01, 0x00, 0x00, 0x00, 0x00,
                        0x14));
        assertTrue(interrogation.isStationInterrogation());
    }

    @Test
    public void preservesUnknownTypeRawBytes() {
        Iec101Asdu asdu = decodeAsdu(Iec101ParserConfig.defaultUnbalanced(),
                Iec101StreamDecoderTest.variableFrame(0x08, 0x01, bytes(
                        0x7F, 0x01, 0x03, 0x00,
                        0x01, 0x00, 0x03, 0x02, 0x01,
                        0xAA, 0xBB)));

        Iec101InformationObject object = asdu.getInformationObjects().get(0);
        assertEquals(Iec101AsduType.UNKNOWN, asdu.getType());
        assertEquals(0x010203, object.getAddress());
        assertArrayEquals(bytes(0xAA, 0xBB), object.getElementBytes());
        assertNull(object.getValue());
    }

    @Test
    public void classifiesAsduSupport() {
        assertTrue(Iec101AsduSupport.ofTypeId(1).hasTypedValue());
        assertEquals(Iec101SinglePointValue.class, Iec101AsduSupport.ofTypeId(1).getValueClass());
        assertEquals(Iec101SinglePointValue.class, Iec101AsduSupport.ofTypeId(2).getValueClass());
        assertEquals(Iec101CounterInterrogationCommandValue.class,
                Iec101AsduSupport.ofTypeId(101).getValueClass());
        assertEquals(Iec101ClockSynchronizationCommandValue.class,
                Iec101AsduSupport.ofTypeId(103).getValueClass());
        assertTrue(Iec101AsduSupport.ofTypeId(200).isUnknownType());
    }

    private Iec101InformationValue decodeFirstValue(byte[] asduBytes) {
        return decodeAsdu(Iec101ParserConfig.defaultUnbalanced(),
                Iec101StreamDecoderTest.variableFrame(0x08, 0x01, asduBytes))
                .getInformationObjects().get(0).getValue();
    }

    private Iec101Asdu decodeAsdu(Iec101ParserConfig config, byte[] frameBytes) {
        Iec101StreamDecoder decoder = new Iec101StreamDecoder(config);
        List<ParseResult<Iec101Frame>> results = decoder.decode(frameBytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame().getAsdu();
    }

    private byte[] bytes(int... values) {
        return Iec101StreamDecoderTest.bytes(values);
    }
}
