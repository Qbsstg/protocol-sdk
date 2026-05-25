package io.github.qbsstg.protocol.iec103;

public enum Iec103AsduType {
    TIME_TAGGED_MESSAGE(1, "time-tagged message", 5),
    TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME(2, "time-tagged message with relative time", 9),
    MEASURANDS_I(3, "measurands I", 3),
    TIME_TAGGED_MEASURANDS_WITH_RELATIVE_TIME(4, "time-tagged measurands with relative time", -1),
    IDENTIFICATION(5, "identification", -1),
    MEASURANDS_II(9, "measurands II", 3),
    UNKNOWN(-1, "unknown", -1);

    private final int typeId;
    private final String description;
    private final int informationElementPayloadLength;

    Iec103AsduType(int typeId, String description, int informationElementPayloadLength) {
        this.typeId = typeId;
        this.description = description;
        this.informationElementPayloadLength = informationElementPayloadLength;
    }

    public int getTypeId() {
        return typeId;
    }

    public String getDescription() {
        return description;
    }

    public int getInformationElementPayloadLength() {
        return informationElementPayloadLength;
    }

    public static Iec103AsduType fromTypeId(int typeId) {
        for (Iec103AsduType type : values()) {
            if (type.typeId == typeId) {
                return type;
            }
        }
        return UNKNOWN;
    }
}
