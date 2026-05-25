package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec103AsduValueTest {

    @Test
    public void decodesProtectionEventWithRelativeTimeAndFaultNumber() {
        Iec103ProtectionEventValue value = (Iec103ProtectionEventValue) decodeFirstValue(bytes(
                0x02, 0x01, 0x01, 0x01,
                0x11, 0x02, 0x01, 0x34, 0x12, 0x02, 0x00, 0xD0, 0x07, 0x05, 0x06));

        assertEquals(Iec103AsduType.TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME, value.getAsduType());
        assertEquals(Iec103ProtectionEventState.OFF, value.getEventState());
        assertEquals(Integer.valueOf(0x1234), value.getRelativeTimeMillis());
        assertEquals(Integer.valueOf(2), value.getFaultNumber());
        assertEquals(2000, value.getTimeTag().getMillisecondsWithinMinute());
        assertEquals(5, value.getTimeTag().getMinute());
        assertEquals(6, value.getTimeTag().getHour());
    }

    @Test
    public void decodesMeasuredValues() {
        Iec103MeasuredValue measurandsI = (Iec103MeasuredValue) decodeFirstValue(bytes(
                0x03, 0x01, 0x01, 0x01,
                0x20, 0x10, 0x00, 0x40, 0x00));
        assertEquals(Iec103MeasuredValueKind.MEASURANDS_I, measurandsI.getKind());
        assertEquals(0x4000, measurandsI.getRawValue());
        assertEquals(0.5d, measurandsI.getValue(), 0.000001d);

        Iec103MeasuredValue measurandsII = (Iec103MeasuredValue) decodeFirstValue(bytes(
                0x09, 0x01, 0x01, 0x01,
                0x21, 0x11, 0x00, 0xC0, 0x80));
        assertEquals(Iec103MeasuredValueKind.MEASURANDS_II, measurandsII.getKind());
        assertEquals(-0x4000, measurandsII.getRawValue());
        assertEquals(-0.5d, measurandsII.getValue(), 0.000001d);
        assertEquals(0x80, measurandsII.getQuality());
    }

    @Test
    public void decodesIdentificationPayloadAsRawBackedAscii() {
        Iec103IdentificationValue value = (Iec103IdentificationValue) decodeFirstValue(bytes(
                0x05, 0x01, 0x01, 0x01,
                0xFE, 0xF1, 'R', 'E', 'L', 'A', 'Y'));

        assertEquals(0xFE, value.getFunctionType());
        assertEquals(0xF1, value.getInformationNumber());
        assertEquals("RELAY", value.getAsciiText());
        assertArrayEquals(bytes('R', 'E', 'L', 'A', 'Y'), value.getRawBytes());
    }

    @Test
    public void preservesUnknownTypeRawBytes() {
        Iec103Asdu asdu = decodeAsdu(Iec103ParserConfig.defaultUnbalanced(),
                Iec103StreamDecoderTest.variableFrame(0x08, 0x01, bytes(
                        0x7F, 0x01, 0x01, 0x01,
                        0x22, 0x33, 0xAA, 0xBB)));

        Iec103InformationElement element = asdu.getInformationElements().get(0);
        assertEquals(Iec103AsduType.UNKNOWN, asdu.getType());
        assertEquals(0x22, element.getFunctionType());
        assertEquals(0x33, element.getInformationNumber());
        assertArrayEquals(bytes(0xAA, 0xBB), element.getPayloadBytes());
        assertArrayEquals(bytes(0x22, 0x33, 0xAA, 0xBB), element.getRawBytes());
        assertNull(element.getValue());
    }

    @Test
    public void classifiesAsduSupport() {
        assertTrue(Iec103Support.ofTypeId(1).hasTypedValue());
        assertEquals(Iec103ProtectionEventValue.class, Iec103Support.ofTypeId(1).getValueClass());
        assertTrue(Iec103Support.ofTypeId(4).isRawBytesOnly());
        assertTrue(Iec103Support.ofTypeId(200).isUnknownType());
    }

    @Test
    public void leavesMalformedInformationElementUnparsed() {
        Iec103Asdu asdu = decodeAsdu(Iec103ParserConfig.defaultUnbalanced(),
                Iec103StreamDecoderTest.variableFrame(0x08, 0x01, bytes(
                        0x01, 0x01, 0x01, 0x01,
                        0x10, 0x01, 0xD2, 0xE8)));

        assertEquals(Iec103AsduType.TIME_TAGGED_MESSAGE, asdu.getType());
        assertTrue(asdu.getInformationElements().isEmpty());
    }

    private Iec103InformationValue decodeFirstValue(byte[] asduBytes) {
        return decodeAsdu(Iec103ParserConfig.defaultUnbalanced(),
                Iec103StreamDecoderTest.variableFrame(0x08, 0x01, asduBytes))
                .getInformationElements().get(0).getValue();
    }

    private Iec103Asdu decodeAsdu(Iec103ParserConfig config, byte[] frameBytes) {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder(config);
        List<ParseResult<Iec103Frame>> results = decoder.decode(frameBytes);
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame().getAsdu();
    }

    private byte[] bytes(int... values) {
        return Iec103StreamDecoderTest.bytes(values);
    }
}
