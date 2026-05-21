package io.github.qbsstg.protocol.iec104;

public final class Iec104PackedOutputCircuitValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int rawOutputCircuitInformation;
    private final Iec104ProtectionQualityDescriptor quality;
    private final int relayOperatingTimeMillis;
    private final Iec104Cp56Time2a timeTag;

    public Iec104PackedOutputCircuitValue(Iec104AsduType asduType, int rawOutputCircuitInformation,
                                          Iec104ProtectionQualityDescriptor quality,
                                          int relayOperatingTimeMillis, Iec104Cp56Time2a timeTag) {
        this.asduType = asduType;
        this.rawOutputCircuitInformation = rawOutputCircuitInformation & 0xFF;
        this.quality = quality;
        this.relayOperatingTimeMillis = relayOperatingTimeMillis;
        this.timeTag = timeTag;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getRawOutputCircuitInformation() {
        return rawOutputCircuitInformation;
    }

    public boolean isOutputCircuitSet(int bitIndex) {
        if (bitIndex < 0 || bitIndex > 7) {
            throw new IllegalArgumentException("bitIndex must be between 0 and 7");
        }
        return (rawOutputCircuitInformation & (1 << bitIndex)) != 0;
    }

    public Iec104ProtectionQualityDescriptor getQuality() {
        return quality;
    }

    public int getRelayOperatingTimeMillis() {
        return relayOperatingTimeMillis;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return timeTag;
    }
}
