package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.ParseResult;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iec103SdkUsageExampleTest {

    @Test
    public void parsesProtectionEventFromStreamBytes() {
        Iec103StreamDecoder decoder = new Iec103StreamDecoder();

        List<ParseResult<Iec103Frame>> results = decoder.decode(Iec103StreamDecoderTest.variableFrame(0x08, 0x01,
                Iec103StreamDecoderTest.bytes(
                        0x01, 0x01, 0x01, 0x01,
                        0x10, 0x01, 0xD2, 0xE8, 0x03, 0x15, 0x10)));

        ParseResult<Iec103Frame> result = results.get(0);
        assertTrue(result.isSuccess());

        Iec103InformationElement element = result.getFrame().getAsdu().getInformationElements().get(0);
        Iec103ProtectionEventValue value = (Iec103ProtectionEventValue) element.getValue();

        assertEquals(Iec103AsduType.TIME_TAGGED_MESSAGE, result.getFrame().getAsdu().getType());
        assertEquals(0x10, element.getFunctionType());
        assertEquals(0x01, element.getInformationNumber());
        assertTrue(value.isOn());
    }
}
