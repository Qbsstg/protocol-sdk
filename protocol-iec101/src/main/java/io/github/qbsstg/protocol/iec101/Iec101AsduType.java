package io.github.qbsstg.protocol.iec101;

public enum Iec101AsduType {
    M_SP_NA_1(1, "single-point information", 1),
    M_SP_TA_1(2, "single-point information with CP24Time2a", 4),
    M_DP_NA_1(3, "double-point information", 1),
    M_DP_TA_1(4, "double-point information with CP24Time2a", 4),
    M_ME_NA_1(9, "measured value, normalized", 3),
    M_ME_TA_1(10, "measured value, normalized with CP24Time2a", 6),
    M_ME_NB_1(11, "measured value, scaled", 3),
    M_ME_TB_1(12, "measured value, scaled with CP24Time2a", 6),
    M_ME_NC_1(13, "measured value, short floating point", 5),
    M_ME_TC_1(14, "measured value, short floating point with CP24Time2a", 8),
    M_SP_TB_1(30, "single-point information with CP56Time2a", 8),
    M_DP_TB_1(31, "double-point information with CP56Time2a", 8),
    M_ME_TD_1(34, "measured value, normalized with CP56Time2a", 10),
    M_ME_TE_1(35, "measured value, scaled with CP56Time2a", 10),
    M_ME_TF_1(36, "measured value, short floating point with CP56Time2a", 12),
    C_SC_NA_1(45, "single command", 1),
    C_DC_NA_1(46, "double command", 1),
    C_IC_NA_1(100, "interrogation command", 1),
    C_CS_NA_1(103, "clock synchronization command", 7),
    UNKNOWN(-1, "unknown", -1);

    private final int typeId;
    private final String description;
    private final int informationElementLength;

    Iec101AsduType(int typeId, String description, int informationElementLength) {
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

    public static Iec101AsduType fromTypeId(int typeId) {
        for (Iec101AsduType type : values()) {
            if (type.typeId == typeId) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
