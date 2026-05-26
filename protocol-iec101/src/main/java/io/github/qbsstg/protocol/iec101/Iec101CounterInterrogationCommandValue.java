package io.github.qbsstg.protocol.iec101;

public final class Iec101CounterInterrogationCommandValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final int rawQualifier;
    private final int requestQualifier;
    private final Iec101CounterFreezeResetQualifier freezeResetQualifier;

    public Iec101CounterInterrogationCommandValue(Iec101AsduType asduType, int rawQualifier) {
        this.asduType = asduType;
        this.rawQualifier = rawQualifier & 0xFF;
        this.requestQualifier = this.rawQualifier & 0x3F;
        this.freezeResetQualifier = Iec101CounterFreezeResetQualifier.fromCode(this.rawQualifier >> 6);
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public int getRawQualifier() {
        return rawQualifier;
    }

    public int getRequestQualifier() {
        return requestQualifier;
    }

    public Iec101CounterFreezeResetQualifier getFreezeResetQualifier() {
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
}
