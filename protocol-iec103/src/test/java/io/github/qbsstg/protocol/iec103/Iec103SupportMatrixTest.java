package io.github.qbsstg.protocol.iec103;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Iec103SupportMatrixTest {

    private static final EnumSet<Iec103AsduType> TYPED = EnumSet.of(
            Iec103AsduType.TIME_TAGGED_MESSAGE,
            Iec103AsduType.TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME,
            Iec103AsduType.MEASURANDS_I,
            Iec103AsduType.IDENTIFICATION,
            Iec103AsduType.MEASURANDS_II);

    private static final EnumSet<Iec103AsduType> RAW_ONLY = EnumSet.of(
            Iec103AsduType.TIME_TAGGED_MEASURANDS_WITH_RELATIVE_TIME);

    @Test
    public void everyKnownAsduTypeHasExplicitSupportStatus() {
        for (Iec103AsduType type : Iec103AsduType.values()) {
            Iec103Support support = Iec103Support.of(type);
            if (Iec103AsduType.UNKNOWN.equals(type)) {
                assertTrue(support.isUnknownType());
            } else if (TYPED.contains(type)) {
                assertTrue("Expected typed value support for " + type, support.hasTypedValue());
            } else if (RAW_ONLY.contains(type)) {
                assertTrue("Expected raw-only support for " + type, support.isRawBytesOnly());
            } else {
                throw new AssertionError("Update Iec103SupportMatrixTest for " + type);
            }
        }
    }

    @Test
    public void mapsTypedAsduTypesToExpectedValueClasses() {
        assertEquals(Iec103ProtectionEventValue.class,
                Iec103Support.of(Iec103AsduType.TIME_TAGGED_MESSAGE).getValueClass());
        assertEquals(Iec103ProtectionEventValue.class,
                Iec103Support.of(Iec103AsduType.TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME).getValueClass());
        assertEquals(Iec103MeasuredValue.class,
                Iec103Support.of(Iec103AsduType.MEASURANDS_I).getValueClass());
        assertEquals(Iec103MeasuredValue.class,
                Iec103Support.of(Iec103AsduType.MEASURANDS_II).getValueClass());
        assertEquals(Iec103IdentificationValue.class,
                Iec103Support.of(Iec103AsduType.IDENTIFICATION).getValueClass());
    }
}
