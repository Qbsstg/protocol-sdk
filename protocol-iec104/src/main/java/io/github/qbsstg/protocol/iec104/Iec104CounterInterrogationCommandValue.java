package io.github.qbsstg.protocol.iec104;

public final class Iec104CounterInterrogationCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int rawQualifier;
    private final int requestQualifier;
    private final Iec104CounterFreezeResetQualifier freezeResetQualifier;

    public Iec104CounterInterrogationCommandValue(Iec104AsduType asduType, int rawQualifier) {
        this.asduType = asduType;
        this.rawQualifier = rawQualifier & 0xFF;
        this.requestQualifier = this.rawQualifier & 0x3F;
        this.freezeResetQualifier = Iec104CounterFreezeResetQualifier.fromCode(this.rawQualifier >> 6);
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getRawQualifier() {
        return rawQualifier;
    }

    public int getRequestQualifier() {
        return requestQualifier;
    }

    public Iec104CounterFreezeResetQualifier getFreezeResetQualifier() {
        return freezeResetQualifier;
    }

    public boolean isGeneralCounterInterrogation() {
        return requestQualifier == 5;
    }

    public Integer getCounterGroupNumber() {
        if (requestQualifier >= 1 && requestQualifier <= 4) {
            return Integer.valueOf(requestQualifier);
        }
        return null;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return null;
    }
}
