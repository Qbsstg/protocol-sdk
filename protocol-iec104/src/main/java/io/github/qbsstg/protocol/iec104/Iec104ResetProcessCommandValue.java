package io.github.qbsstg.protocol.iec104;

public final class Iec104ResetProcessCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int qualifierOfResetProcess;

    public Iec104ResetProcessCommandValue(Iec104AsduType asduType, int qualifierOfResetProcess) {
        this.asduType = asduType;
        this.qualifierOfResetProcess = qualifierOfResetProcess & 0xFF;
    }

    public Iec104AsduType getAsduType() {
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

    public Iec104Cp56Time2a getTimeTag() {
        return null;
    }
}
