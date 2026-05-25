package io.github.qbsstg.protocol.iec104;

public enum Iec104AsduType {
    M_SP_NA_1(1, "single-point information", 1),
    M_DP_NA_1(3, "double-point information", 1),
    M_ST_NA_1(5, "step position information", 2),
    M_BO_NA_1(7, "bitstring of 32 bits", 5),
    M_ME_NA_1(9, "measured value, normalized", 3),
    M_ME_NB_1(11, "measured value, scaled", 3),
    M_ME_NC_1(13, "measured value, short floating point", 5),
    M_IT_NA_1(15, "integrated totals", 5),
    M_PS_NA_1(20, "packed single-point information with status change detection", 5),
    M_ME_ND_1(21, "measured value, normalized without quality descriptor", 2),
    M_SP_TB_1(30, "single-point information with CP56Time2a", 8),
    M_DP_TB_1(31, "double-point information with CP56Time2a", 8),
    M_ST_TB_1(32, "step position information with CP56Time2a", 9),
    M_BO_TB_1(33, "bitstring of 32 bits with CP56Time2a", 12),
    M_ME_TD_1(34, "measured normalized value with CP56Time2a", 10),
    M_ME_TE_1(35, "measured scaled value with CP56Time2a", 10),
    M_ME_TF_1(36, "measured short floating point value with CP56Time2a", 12),
    M_IT_TB_1(37, "integrated totals with CP56Time2a", 12),
    M_EP_TD_1(38, "event of protection equipment with CP56Time2a", 10),
    M_EP_TE_1(39, "packed start events of protection equipment with CP56Time2a", 11),
    M_EP_TF_1(40, "packed output circuit information with CP56Time2a", 11),
    C_SC_NA_1(45, "single command", 1),
    C_DC_NA_1(46, "double command", 1),
    C_RC_NA_1(47, "regulating step command", 1),
    C_SE_NA_1(48, "set point command, normalized", 3),
    C_SE_NB_1(49, "set point command, scaled", 3),
    C_SE_NC_1(50, "set point command, short floating point", 5),
    C_BO_NA_1(51, "bitstring command", 5),
    C_SC_TA_1(58, "single command with CP56Time2a", 8),
    C_DC_TA_1(59, "double command with CP56Time2a", 8),
    C_RC_TA_1(60, "regulating step command with CP56Time2a", 8),
    C_SE_TA_1(61, "set point command, normalized with CP56Time2a", 10),
    C_SE_TB_1(62, "set point command, scaled with CP56Time2a", 10),
    C_SE_TC_1(63, "set point command, short floating point with CP56Time2a", 12),
    C_BO_TA_1(64, "bitstring command with CP56Time2a", 12),
    M_EI_NA_1(70, "end of initialization", -1),
    C_IC_NA_1(100, "interrogation command", 1),
    C_CI_NA_1(101, "counter interrogation command", 1),
    C_RD_NA_1(102, "read command", 0),
    C_CS_NA_1(103, "clock synchronization command", 7),
    C_RP_NA_1(105, "reset process command", 1),
    C_CD_NA_1(106, "delay acquisition command", 2),
    P_ME_NA_1(110, "parameter of measured value, normalized", 3),
    P_ME_NB_1(111, "parameter of measured value, scaled", 3),
    P_ME_NC_1(112, "parameter of measured value, short floating point", 5),
    P_AC_NA_1(113, "parameter activation", 1),
    F_FR_NA_1(120, "file ready", -1),
    F_SR_NA_1(121, "section ready", -1),
    F_SC_NA_1(122, "file call, select, directory command", -1),
    F_LS_NA_1(123, "last section or segment", -1),
    F_AF_NA_1(124, "file or section acknowledgement", -1),
    F_SG_NA_1(125, "file segment", -1),
    F_DR_TA_1(126, "file directory with CP56Time2a", -1),
    UNKNOWN(-1, "unknown", -1);

    private final int typeId;
    private final String description;
    private final int informationElementLength;

    Iec104AsduType(int typeId, String description, int informationElementLength) {
        this.typeId = typeId;
        this.description = description;
        this.informationElementLength = informationElementLength;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getDescription() {
        return description;
    }

    public int getInformationElementLength() {
        return informationElementLength;
    }

    public static Iec104AsduType fromTypeId(int typeId) {
        for (Iec104AsduType type : values()) {
            if (type.typeId == typeId) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
