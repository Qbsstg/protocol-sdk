package io.github.qbsstg.protocol.iec101;

public final class Iec101ResetProcessCommandValue implements Iec101InformationValue {

    private final Iec101AsduType asduType;
    private final int qualifierOfResetProcess;

    public Iec101ResetProcessCommandValue(Iec101AsduType asduType, int qualifierOfResetProcess) {
        this.asduType = asduType;
        this.qualifierOfResetProcess = qualifierOfResetProcess & 0xFF;
    }

    public Iec101AsduType getAsduType() {
        return asduType;
    }

    public int getQualifierOfResetProcess() {
        return qualifierOfResetProcess;
    }

    public boolean isGeneralResetProcess() {
        return qualifierOfResetProcess == 1;
    }

    public boolean isResetEventBuffer() {
        return qualifierOfResetProcess == 2;
    }
}
