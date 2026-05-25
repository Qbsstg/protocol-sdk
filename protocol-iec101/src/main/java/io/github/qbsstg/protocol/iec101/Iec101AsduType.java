package io.github.qbsstg.protocol.iec101;

public enum Iec101AsduType {
    M_SP_NA_1(1, "single-point information", 1),
    M_DP_NA_1(3, "double-point information", 1),
    M_ME_NA_1(9, "measured value, normalized", 3),
    M_ME_NB_1(11, "measured value, scaled", 3),
    M_ME_NC_1(13, "measured value, short floating point", 5),
    C_SC_NA_1(45, "single command", 1),
    C_DC_NA_1(46, "double command", 1),
    C_IC_NA_1(100, "interrogation command", 1),
    C_CS_NA_1(103, "clock synchronization command", -1),
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
