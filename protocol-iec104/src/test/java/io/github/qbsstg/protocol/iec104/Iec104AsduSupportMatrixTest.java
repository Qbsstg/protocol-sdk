package io.github.qbsstg.protocol.iec104;

import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class Iec104AsduSupportMatrixTest {

    private static final EnumSet<Iec104AsduType> TYPED_VALUE_TYPES = EnumSet.of(
            Iec104AsduType.M_SP_NA_1,
            Iec104AsduType.M_DP_NA_1,
            Iec104AsduType.M_ST_NA_1,
            Iec104AsduType.M_BO_NA_1,
            Iec104AsduType.M_ME_NA_1,
            Iec104AsduType.M_ME_NB_1,
            Iec104AsduType.M_ME_NC_1,
            Iec104AsduType.M_IT_NA_1,
            Iec104AsduType.M_ME_ND_1,
            Iec104AsduType.M_SP_TB_1,
            Iec104AsduType.M_DP_TB_1,
            Iec104AsduType.M_ST_TB_1,
            Iec104AsduType.M_BO_TB_1,
            Iec104AsduType.M_ME_TD_1,
            Iec104AsduType.M_ME_TE_1,
            Iec104AsduType.M_ME_TF_1,
            Iec104AsduType.M_IT_TB_1,
            Iec104AsduType.C_SC_NA_1,
            Iec104AsduType.C_DC_NA_1,
            Iec104AsduType.C_RC_NA_1,
            Iec104AsduType.C_SE_NA_1,
            Iec104AsduType.C_SE_NB_1,
            Iec104AsduType.C_SE_NC_1,
            Iec104AsduType.C_BO_NA_1,
            Iec104AsduType.C_SC_TA_1,
            Iec104AsduType.C_DC_TA_1,
            Iec104AsduType.C_RC_TA_1,
            Iec104AsduType.C_SE_TA_1,
            Iec104AsduType.C_SE_TB_1,
            Iec104AsduType.C_SE_TC_1,
            Iec104AsduType.C_BO_TA_1,
            Iec104AsduType.C_IC_NA_1,
            Iec104AsduType.C_CI_NA_1,
            Iec104AsduType.C_RD_NA_1,
            Iec104AsduType.C_CS_NA_1,
            Iec104AsduType.C_RP_NA_1,
            Iec104AsduType.C_CD_NA_1);

    @Test
    public void classifiesEveryKnownAsduType() {
        for (Iec104AsduType type : Iec104AsduType.values()) {
            if (Iec104AsduType.UNKNOWN.equals(type)) {
                continue;
            }

            Iec104AsduSupport support = Iec104AsduSupport.of(type);
            assertEquals(type, support.getAsduType());
            assertFalse("Missing support matrix entry for " + type, support.isUnknownType());

            if (TYPED_VALUE_TYPES.contains(type)) {
                assertTrue(type + " should be typed", support.hasTypedValue());
                assertNotNull(type + " should declare a value class", support.getValueClass());
            } else {
                throw new AssertionError("Type is not categorized by the support matrix test: " + type);
            }
        }
    }

    @Test
    public void keepsExpectedSupportCountsExplicit() {
        assertEquals(37, TYPED_VALUE_TYPES.size());
    }

    @Test
    public void mapsTypeIdsToSupportEntries() {
        Iec104AsduSupport singlePoint = Iec104AsduSupport.ofTypeId(1);
        assertTrue(singlePoint.hasTypedValue());
        assertEquals(Iec104SinglePointValue.class, singlePoint.getValueClass());

        Iec104AsduSupport clockSynchronization = Iec104AsduSupport.ofTypeId(103);
        assertTrue(clockSynchronization.hasTypedValue());
        assertEquals(Iec104ClockSynchronizationCommandValue.class, clockSynchronization.getValueClass());

        Iec104AsduSupport unknown = Iec104AsduSupport.ofTypeId(200);
        assertTrue(unknown.isUnknownType());
        assertEquals(Iec104AsduType.UNKNOWN, unknown.getAsduType());
    }
}
