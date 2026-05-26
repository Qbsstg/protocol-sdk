package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class Iec103ProtectionEventValueTest {

    @Test
    public void parsesTimeTaggedMessageQualityFlagsAndRawBytes() {
        Iec103InformationElement element = decodeFirstElement(bytes(
                0x01, 0x01, 0x01, 0x01,
                0x10, 0x01, 0xFA, 0xE8, 0x03, 0x95, 0x90));

        assertEquals(0x10, element.getFunctionType());
        assertEquals(0x01, element.getInformationNumber());
        assertArrayEquals(bytes(0xFA, 0xE8, 0x03, 0x95, 0x90), element.getPayloadBytes());
        assertArrayEquals(bytes(0x10, 0x01, 0xFA, 0xE8, 0x03, 0x95, 0x90), element.getRawBytes());

        Iec103ProtectionEventValue value = (Iec103ProtectionEventValue) element.getValue();
        assertEquals(Iec103AsduType.TIME_TAGGED_MESSAGE, value.getAsduType());
        assertEquals(0x10, value.getFunctionType());
        assertEquals(0x01, value.getInformationNumber());
        assertEquals(0xFA, value.getRawEvent());
        assertEquals(Iec103ProtectionEventState.ON, value.getEventState());
        assertTrue(value.isOn());
        assertNull(value.getRelativeTimeMillis());
        assertNull(value.getFaultNumber());
        assertArrayEquals(element.getRawBytes(), value.getRawBytes());

        Iec103ProtectionQualityDescriptor quality = value.getQuality();
        assertEquals(0xFA, quality.getRawValue());
        assertTrue(quality.isInvalid());
        assertTrue(quality.isNotTopical());
        assertTrue(quality.isSubstituted());
        assertTrue(quality.isBlocked());
        assertTrue(quality.isElapsedTimeInvalid());

        Iec103TimeTag timeTag = value.getTimeTag();
        assertEquals(1000, timeTag.getMillisecondsWithinMinute());
        assertEquals(1, timeTag.getSecond());
        assertEquals(0, timeTag.getMillisecond());
        assertEquals(21, timeTag.getMinute());
        assertEquals(16, timeTag.getHour());
        assertTrue(timeTag.isInvalid());
        assertTrue(timeTag.isSummerTime());
        assertArrayEquals(bytes(0xE8, 0x03, 0x95, 0x90), timeTag.getRawBytes());
    }

    @Test
    public void parsesAllProtectionEventStates() {
        assertState(0x00, Iec103ProtectionEventState.INDETERMINATE_ZERO, false);
        assertState(0x01, Iec103ProtectionEventState.OFF, false);
        assertState(0x02, Iec103ProtectionEventState.ON, true);
        assertState(0x03, Iec103ProtectionEventState.INDETERMINATE_THREE, false);
    }

    @Test
    public void parsesRelativeTimeEventMetadataAndQualityFlags() {
        Iec103InformationElement element = decodeFirstElement(bytes(
                0x02, 0x01, 0x01, 0x01,
                0x11, 0x02, 0x29, 0x34, 0x12, 0x78, 0x56, 0xD0, 0x07, 0x05, 0x86));

        assertEquals(0x11, element.getFunctionType());
        assertEquals(0x02, element.getInformationNumber());
        assertArrayEquals(bytes(0x29, 0x34, 0x12, 0x78, 0x56, 0xD0, 0x07, 0x05, 0x86),
                element.getPayloadBytes());

        Iec103ProtectionEventValue value = (Iec103ProtectionEventValue) element.getValue();
        assertEquals(Iec103AsduType.TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME, value.getAsduType());
        assertEquals(0x29, value.getRawEvent());
        assertEquals(Iec103ProtectionEventState.OFF, value.getEventState());
        assertFalse(value.isOn());
        assertEquals(Integer.valueOf(0x1234), value.getRelativeTimeMillis());
        assertEquals(Integer.valueOf(0x5678), value.getFaultNumber());
        assertArrayEquals(element.getRawBytes(), value.getRawBytes());

        Iec103ProtectionQualityDescriptor quality = value.getQuality();
        assertFalse(quality.isInvalid());
        assertFalse(quality.isNotTopical());
        assertTrue(quality.isSubstituted());
        assertFalse(quality.isBlocked());
        assertTrue(quality.isElapsedTimeInvalid());

        Iec103TimeTag timeTag = value.getTimeTag();
        assertEquals(2000, timeTag.getMillisecondsWithinMinute());
        assertEquals(5, timeTag.getMinute());
        assertEquals(6, timeTag.getHour());
        assertFalse(timeTag.isInvalid());
        assertTrue(timeTag.isSummerTime());
    }

    @Test
    public void preservesRawAsduWhenRelativeTimeEventIsTruncated() {
        byte[] asduBytes = bytes(
                0x02, 0x01, 0x01, 0x01,
                0x11, 0x02, 0x29, 0x34, 0x12, 0x78, 0x56, 0xD0, 0x07, 0x05);
        Iec103Asdu asdu = decodeAsdu(asduBytes);

        assertEquals(Iec103AsduType.TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME, asdu.getType());
        assertArrayEquals(asduBytes, asdu.getRawBytes());
        assertTrue(asdu.getInformationElements().isEmpty());
    }

    private void assertState(int rawEvent, Iec103ProtectionEventState expectedState, boolean on) {
        Iec103ProtectionEventValue value = (Iec103ProtectionEventValue) decodeFirstElement(bytes(
                0x01, 0x01, 0x01, 0x01,
                0x10, 0x01, rawEvent, 0xE8, 0x03, 0x15, 0x10)).getValue();
        assertEquals(expectedState, value.getEventState());
        assertEquals(on, value.isOn());
        assertEquals(rawEvent, value.getRawEvent());
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
