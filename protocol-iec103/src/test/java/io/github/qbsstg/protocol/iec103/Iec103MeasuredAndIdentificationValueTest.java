package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec103MeasuredAndIdentificationValueTest {

    @Test
    public void parsesMeasurandsISequenceWithSignedNormalizedValuesAndRawBytes() {
        Iec103Asdu asdu = decodeAsdu(bytes(
                0x03, 0x02, 0x01, 0x01,
                0x20, 0x10, 0x00, 0x00, 0x00,
                0x20, 0x11, 0xFF, 0x7F, 0x3F));

        assertEquals(Iec103AsduType.MEASURANDS_I, asdu.getType());
        assertEquals(2, asdu.getInformationElements().size());

        Iec103InformationElement zeroElement = asdu.getInformationElements().get(0);
        assertEquals(0, zeroElement.getIndex());
        assertArrayEquals(bytes(0x00, 0x00, 0x00), zeroElement.getPayloadBytes());
        assertArrayEquals(bytes(0x20, 0x10, 0x00, 0x00, 0x00), zeroElement.getRawBytes());
        assertMeasuredValue(zeroElement, Iec103AsduType.MEASURANDS_I, Iec103MeasuredValueKind.MEASURANDS_I,
                0x20, 0x10, 0, 0.0d, 0x00);

        Iec103InformationElement positiveElement = asdu.getInformationElements().get(1);
        assertEquals(1, positiveElement.getIndex());
        assertArrayEquals(bytes(0xFF, 0x7F, 0x3F), positiveElement.getPayloadBytes());
        assertArrayEquals(bytes(0x20, 0x11, 0xFF, 0x7F, 0x3F), positiveElement.getRawBytes());
        assertMeasuredValue(positiveElement, Iec103AsduType.MEASURANDS_I, Iec103MeasuredValueKind.MEASURANDS_I,
                0x20, 0x11, 32767, 32767.0d / 32768.0d, 0x3F);
    }

    @Test
    public void parsesMeasurandsIINegativeFullScaleQualityAndDefensiveRawCopy() {
        Iec103InformationElement element = decodeFirstElement(bytes(
                0x09, 0x01, 0x01, 0x01,
                0x21, 0x12, 0x00, 0x80, 0xC5));

        Iec103MeasuredValue value = assertMeasuredValue(element, Iec103AsduType.MEASURANDS_II,
                Iec103MeasuredValueKind.MEASURANDS_II, 0x21, 0x12, -32768, -1.0d, 0xC5);
        assertArrayEquals(bytes(0x21, 0x12, 0x00, 0x80, 0xC5), value.getRawBytes());

        byte[] mutated = value.getRawBytes();
        mutated[2] = 0x7F;
        assertArrayEquals(bytes(0x21, 0x12, 0x00, 0x80, 0xC5), value.getRawBytes());
    }

    @Test
    public void parsesIdentificationPayloadAsRawBackedAsciiAndDefensiveCopy() {
        Iec103InformationElement element = decodeFirstElement(bytes(
                0x05, 0x01, 0x01, 0x01,
                0xFE, 0xF1, 'R', 'E', 'L', '1', '0', '3', '-', 'A'));

        assertEquals(0xFE, element.getFunctionType());
        assertEquals(0xF1, element.getInformationNumber());
        assertArrayEquals(bytes('R', 'E', 'L', '1', '0', '3', '-', 'A'), element.getPayloadBytes());
        assertArrayEquals(bytes(0xFE, 0xF1, 'R', 'E', 'L', '1', '0', '3', '-', 'A'), element.getRawBytes());

        Iec103IdentificationValue value = (Iec103IdentificationValue) element.getValue();
        assertEquals(0xFE, value.getFunctionType());
        assertEquals(0xF1, value.getInformationNumber());
        assertEquals("REL103-A", value.getAsciiText());
        assertArrayEquals(bytes('R', 'E', 'L', '1', '0', '3', '-', 'A'), value.getRawBytes());

        byte[] mutated = value.getRawBytes();
        mutated[0] = 'X';
        assertArrayEquals(bytes('R', 'E', 'L', '1', '0', '3', '-', 'A'), value.getRawBytes());
        assertEquals("REL103-A", value.getAsciiText());
    }

    @Test
    public void preservesRawAsduWhenIdentificationElementIsMissingFunInf() {
        byte[] asduBytes = bytes(0x05, 0x01, 0x01, 0x01);
        Iec103Asdu asdu = decodeAsdu(asduBytes);

        assertEquals(Iec103AsduType.IDENTIFICATION, asdu.getType());
        assertArrayEquals(asduBytes, asdu.getRawBytes());
        assertTrue(asdu.getInformationElements().isEmpty());
    }

    @Test
    public void keepsTimeTaggedMeasurandsRawOnly() {
        Iec103InformationElement element = decodeFirstElement(bytes(
                0x04, 0x01, 0x01, 0x01,
                0x30, 0x20, 0x34, 0x12, 0x10, 0x78, 0x56, 0xD0, 0x07, 0x05, 0x06));

        assertEquals(Iec103AsduSupportStatus.RAW_BYTES_ONLY,
                Iec103Support.of(Iec103AsduType.TIME_TAGGED_MEASURANDS_WITH_RELATIVE_TIME).getStatus());
        assertEquals(0x30, element.getFunctionType());
        assertEquals(0x20, element.getInformationNumber());
        assertArrayEquals(bytes(0x34, 0x12, 0x10, 0x78, 0x56, 0xD0, 0x07, 0x05, 0x06),
                element.getPayloadBytes());
        assertArrayEquals(bytes(0x30, 0x20, 0x34, 0x12, 0x10, 0x78, 0x56, 0xD0, 0x07, 0x05, 0x06),
                element.getRawBytes());
        assertNull(element.getValue());
    }

    private Iec103MeasuredValue assertMeasuredValue(Iec103InformationElement element, Iec103AsduType expectedAsduType,
                                                    Iec103MeasuredValueKind expectedKind, int expectedFunctionType,
                                                    int expectedInformationNumber, int expectedRawValue,
                                                    double expectedValue, int expectedQuality) {
        Iec103MeasuredValue value = (Iec103MeasuredValue) element.getValue();
        assertEquals(expectedAsduType, value.getAsduType());
        assertEquals(expectedKind, value.getKind());
        assertEquals(expectedFunctionType, value.getFunctionType());
        assertEquals(expectedInformationNumber, value.getInformationNumber());
        assertEquals(expectedRawValue, value.getRawValue());
        assertEquals(expectedValue, value.getValue(), 0.000001d);
        assertEquals(expectedQuality, value.getQuality());
        assertArrayEquals(element.getRawBytes(), value.getRawBytes());
        return value;
    }

    private Iec103InformationElement decodeFirstElement(byte[] asduBytes) {
        Iec103Asdu asdu = decodeAsdu(asduBytes);
        assertEquals(1, asdu.getInformationElements().size());
        return asdu.getInformationElements().get(0);
    }

    private Iec103Asdu decodeAsdu(byte[] asduBytes) {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();
        List<ParseResult<Iec103Frame>> results = decoder.decode(
                Iec103StreamDecoderTest.variableFrame(0x08, 0x01, asduBytes));
        assertEquals(1, results.size());
        assertTrue(results.get(0).isSuccess());
        return results.get(0).getFrame().getAsdu();
    }

    private byte[] bytes(int... values) {
        return Iec103StreamDecoderTest.bytes(values);
    }
}
