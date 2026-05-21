package io.github.qbsstg.protocol.iec104;

public final class Iec104VariableStructureQualifier {

    private final int rawValue;
    private final int numberOfObjects;
    private final boolean sequence;

    public Iec104VariableStructureQualifier(int rawValue) {
        this.rawValue = rawValue & 0xFF;
        this.numberOfObjects = rawValue & 0x7F;
        this.sequence = (rawValue & 0x80) != 0;
    }

    public int getRawValue() {
        return rawValue;
    }

    public int getNumberOfObjects() {
        return numberOfObjects;
    }

    public boolean isSequence() {
        return sequence;
    }
}
