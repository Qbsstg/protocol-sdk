package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.nio.charset.StandardCharsets;

public final class Iec103IdentificationValue implements Iec103InformationValue {

    private final int functionType;
    private final int informationNumber;
    private final byte[] rawBytes;
    private final String asciiText;

    public Iec103IdentificationValue(int functionType, int informationNumber, byte[] rawBytes) {
        this.functionType = functionType & 0xFF;
        this.informationNumber = informationNumber & 0xFF;
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.asciiText = new String(this.rawBytes, StandardCharsets.US_ASCII);
    }

    public int getFunctionType() {
        return functionType;
    }

    public int getInformationNumber() {
        return informationNumber;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }

    public String getAsciiText() {
        return asciiText;
    }
}
