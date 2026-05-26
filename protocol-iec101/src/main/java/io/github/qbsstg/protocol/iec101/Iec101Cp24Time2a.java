package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec101Cp24Time2a implements Iec101TimeTag {

    public static final int LENGTH = 3;

    private final byte[] rawBytes;
    private final int millisecondsWithinMinute;
    private final int minute;
    private final boolean invalid;

    private Iec101Cp24Time2a(byte[] rawBytes, int millisecondsWithinMinute, int minute, boolean invalid) {
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.millisecondsWithinMinute = millisecondsWithinMinute;
        this.minute = minute;
        this.invalid = invalid;
    }

    public static Iec101Cp24Time2a parse(byte[] bytes, int offset) {
        if (bytes == null || offset < 0 || bytes.length - offset < LENGTH) {
            throw new IllegalArgumentException("CP24Time2a requires 3 bytes");
        }

        byte[] rawBytes = ByteArrayUtil.copyOfRange(bytes, offset, offset + LENGTH);
        int millisecondsWithinMinute = ByteArrayUtil.unsignedByte(rawBytes[0])
                | (ByteArrayUtil.unsignedByte(rawBytes[1]) << 8);
        int minuteRaw = ByteArrayUtil.unsignedByte(rawBytes[2]);
        int minute = minuteRaw & 0x3F;
        boolean invalid = (minuteRaw & 0x80) != 0;
        return new Iec101Cp24Time2a(rawBytes, millisecondsWithinMinute, minute, invalid);
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }

    public int getMillisecondsWithinMinute() {
        return millisecondsWithinMinute;
    }

    public int getSecond() {
        return millisecondsWithinMinute / 1000;
    }

    public int getMillisecond() {
        return millisecondsWithinMinute % 1000;
    }

    public int getMinute() {
        return minute;
    }

    public boolean isInvalid() {
        return invalid;
    }
}
