package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec103MeasuredValue implements Iec103InformationValue {

    private final Iec103AsduType asduType;
    private final int functionType;
    private final int informationNumber;
    private final Iec103MeasuredValueKind kind;
    private final int rawValue;
    private final double value;
    private final int quality;
    private final byte[] rawBytes;

    public Iec103MeasuredValue(Iec103AsduType asduType, int functionType, int informationNumber,
                               Iec103MeasuredValueKind kind, int rawValue, double value,
                               int quality, byte[] rawBytes) {
        this.asduType = asduType;
        this.functionType = functionType & 0xFF;
        this.informationNumber = informationNumber & 0xFF;
        this.kind = kind;
        this.rawValue = rawValue;
        this.value = value;
        this.quality = quality & 0xFF;
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
    }

    public Iec103AsduType getAsduType() {
        return asduType;
    }

    public int getFunctionType() {
        return functionType;
    }

    public int getInformationNumber() {
        return informationNumber;
    }

    public Iec103MeasuredValueKind getKind() {
        return kind;
    }

    public int getRawValue() {
        return rawValue;
    }

    public double getValue() {
        return value;
    }

    public int getQuality() {
        return quality;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }
}
