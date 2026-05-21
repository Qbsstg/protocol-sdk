package io.github.qbsstg.protocol.iec104;

public final class Iec104InterrogationCommandValue implements Iec104InformationValue {

    private final Iec104AsduType asduType;
    private final int qualifierOfInterrogation;

    public Iec104InterrogationCommandValue(Iec104AsduType asduType, int qualifierOfInterrogation) {
        this.asduType = asduType;
        this.qualifierOfInterrogation = qualifierOfInterrogation & 0xFF;
    }

    public Iec104AsduType getAsduType() {
        return asduType;
    }

    public int getQualifierOfInterrogation() {
        return qualifierOfInterrogation;
    }

    public boolean isStationInterrogation() {
        return qualifierOfInterrogation == 20;
    }

    public Integer getGroupNumber() {
        if (qualifierOfInterrogation >= 21 && qualifierOfInterrogation <= 36) {
            return Integer.valueOf(qualifierOfInterrogation - 20);
        }
        return null;
    }

    public Iec104Cp56Time2a getTimeTag() {
        return null;
    }
}
