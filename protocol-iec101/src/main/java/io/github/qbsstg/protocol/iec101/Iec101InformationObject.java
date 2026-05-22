package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec101InformationObject {

    private final int index;
    private final int address;
    private final byte[] elementBytes;
    private final Iec101InformationValue value;

    public Iec101InformationObject(int index, int address, byte[] elementBytes,
                                   Iec101InformationValue value) {
        this.index = index;
        this.address = address;
        this.elementBytes = ByteArrayUtil.copyOf(elementBytes);
        this.value = value;
    }

    public int getIndex() {
        return index;
    }

    public int getAddress() {
        return address;
    }

    public byte[] getElementBytes() {
        return ByteArrayUtil.copyOf(elementBytes);
    }

    public Iec101InformationValue getValue() {
        return value;
    }
}
