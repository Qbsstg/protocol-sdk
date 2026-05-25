package io.github.qbsstg.protocol.iec103;

public final class Iec103VariableStructureQualifier {

    private final int rawValue;
    private final int numberOfObjects;
    private final boolean sequence;

    public Iec103VariableStructureQualifier(int rawValue) {
        this.rawValue = rawValue & 0xFF;
        this.numberOfObjects = this.rawValue & 0x7F;
        this.sequence = (this.rawValue & 0x80) != 0;
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
