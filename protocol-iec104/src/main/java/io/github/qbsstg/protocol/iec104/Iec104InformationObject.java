package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec104InformationObject {

    private final int index;
    private final int address;
    private final byte[] elementBytes;
    private final Iec104InformationValue value;

    public Iec104InformationObject(int index, int address, byte[] elementBytes) {
        this(index, address, elementBytes, null);
    }

    public Iec104InformationObject(int index, int address, byte[] elementBytes, Iec104InformationValue value) {
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

    public Iec104InformationValue getValue() {
        return value;
    }
}
