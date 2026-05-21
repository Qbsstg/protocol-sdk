package io.github.qbsstg.protocol.iec104;

public final class Iec104ParameterActivationValue implements Iec104InformationValue {

    private static final int PERSISTENT_CYCLIC_OR_PERIODIC_TRANSMISSION = 3;

    private final Iec104AsduType asduType;
    private final int rawQualifier;

    public Iec104ParameterActivationValue(Iec104AsduType asduType, int rawQualifier) {
        this.asduType = asduType;
        this.rawQualifier = rawQualifier & 0xFF;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getRawQualifier() {
        return rawQualifier;
    }

    public boolean isPersistentCyclicOrPeriodicTransmission() {
        return rawQualifier == PERSISTENT_CYCLIC_OR_PERIODIC_TRANSMISSION;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return null;
    }
}
