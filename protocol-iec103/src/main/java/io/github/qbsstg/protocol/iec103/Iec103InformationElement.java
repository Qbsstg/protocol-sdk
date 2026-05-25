package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec103InformationElement {

    private final int index;
    private final int functionType;
    private final int informationNumber;
    private final byte[] payloadBytes;
    private final byte[] rawBytes;
    private final Iec103InformationValue value;

    public Iec103InformationElement(int index, int functionType, int informationNumber,
                                    byte[] payloadBytes, byte[] rawBytes,
                                    Iec103InformationValue value) {
        this.index = index;
        this.functionType = functionType;
        this.informationNumber = informationNumber;
        this.payloadBytes = ByteArrayUtil.copyOf(payloadBytes);
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public int getFunctionType() {
        return functionType;
    }

    public int getInformationNumber() {
        return informationNumber;
    }

    public byte[] getPayloadBytes() {
        return ByteArrayUtil.copyOf(payloadBytes);
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }

    public Iec103InformationValue getValue() {
        return value;
    }
}
