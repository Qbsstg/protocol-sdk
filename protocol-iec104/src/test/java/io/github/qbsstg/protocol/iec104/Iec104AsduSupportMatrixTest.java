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
            Iec104AsduType.M_PS_NA_1,
            Iec104AsduType.M_ME_ND_1,
            Iec104AsduType.M_SP_TB_1,
            Iec104AsduType.M_DP_TB_1,
            Iec104AsduType.M_ST_TB_1,
            Iec104AsduType.M_BO_TB_1,
            Iec104AsduType.M_ME_TD_1,
            Iec104AsduType.M_ME_TE_1,
            Iec104AsduType.M_ME_TF_1,
            Iec104AsduType.M_IT_TB_1,
            Iec104AsduType.M_EP_TD_1,
            Iec104AsduType.M_EP_TE_1,
            Iec104AsduType.M_EP_TF_1,
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
            Iec104AsduType.C_CD_NA_1,
            Iec104AsduType.P_ME_NA_1,
            Iec104AsduType.P_ME_NB_1,
            Iec104AsduType.P_ME_NC_1,
            Iec104AsduType.P_AC_NA_1);

    private static final EnumSet<Iec104AsduType> RAW_BYTES_ONLY_TYPES = EnumSet.of(
            Iec104AsduType.M_EI_NA_1,
            Iec104AsduType.F_FR_NA_1,
            Iec104AsduType.F_SR_NA_1,
            Iec104AsduType.F_SC_NA_1,
            Iec104AsduType.F_LS_NA_1,
            Iec104AsduType.F_AF_NA_1,
            Iec104AsduType.F_SG_NA_1,
            Iec104AsduType.F_DR_TA_1);

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
            } else if (RAW_BYTES_ONLY_TYPES.contains(type)) {
                assertTrue(type + " should be raw-only", support.isRawBytesOnly());
            } else {
                throw new AssertionError("Type is not categorized by the support matrix test: " + type);
            }
        }
    }

    @Test
    public void keepsExpectedSupportCountsExplicit() {
        assertEquals(45, TYPED_VALUE_TYPES.size());
        assertEquals(8, RAW_BYTES_ONLY_TYPES.size());
    }

    @Test
    public void mapsTypeIdsToSupportEntries() {
        Iec104AsduSupport singlePoint = Iec104AsduSupport.ofTypeId(1);
        assertTrue(singlePoint.hasTypedValue());
        assertEquals(Iec104SinglePointValue.class, singlePoint.getValueClass());

        Iec104AsduSupport clockSynchronization = Iec104AsduSupport.ofTypeId(103);
        assertTrue(clockSynchronization.hasTypedValue());
        assertEquals(Iec104ClockSynchronizationCommandValue.class, clockSynchronization.getValueClass());

        Iec104AsduSupport protectionEvent = Iec104AsduSupport.ofTypeId(38);
        assertTrue(protectionEvent.hasTypedValue());
        assertEquals(Iec104SingleProtectionEventValue.class, protectionEvent.getValueClass());

        Iec104AsduSupport parameterMeasured = Iec104AsduSupport.ofTypeId(110);
        assertTrue(parameterMeasured.hasTypedValue());
        assertEquals(Iec104ParameterMeasuredValue.class, parameterMeasured.getValueClass());

        Iec104AsduSupport endOfInitialization = Iec104AsduSupport.ofTypeId(70);
        assertTrue(endOfInitialization.isRawBytesOnly());
        assertEquals(Iec104AsduType.M_EI_NA_1, endOfInitialization.getAsduType());

        Iec104AsduSupport fileReady = Iec104AsduSupport.ofTypeId(120);
        assertTrue(fileReady.isRawBytesOnly());
        assertEquals(Iec104AsduType.F_FR_NA_1, fileReady.getAsduType());

        Iec104AsduSupport unknown = Iec104AsduSupport.ofTypeId(200);
        assertTrue(unknown.isUnknownType());
        assertEquals(Iec104AsduType.UNKNOWN, unknown.getAsduType());
    }
}
