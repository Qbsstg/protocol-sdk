package io.github.qbsstg.protocol.iec104;

public final class Iec104ParameterQualifier {

    private final int rawValue;
    private final int kindOfParameter;
    private final boolean localParameterChanged;
    private final boolean parameterNotInOperation;

    public Iec104ParameterQualifier(int rawValue) {
        this.rawValue = rawValue & 0xFF;
        this.kindOfParameter = this.rawValue & 0x3F;
        this.localParameterChanged = (this.rawValue & 0x40) != 0;
        this.parameterNotInOperation = (this.rawValue & 0x80) != 0;
    }

    public int getRawValue() {
        return rawValue;
    }

    public int getKindOfParameter() {
        return kindOfParameter;
    }

    public boolean isLocalParameterChanged() {
        return localParameterChanged;
    }

    public boolean isParameterNotInOperation() {
        return parameterNotInOperation;
    }
}
