package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec103TimeTag {

    private static final int LENGTH = 4;

    private final byte[] rawBytes;
    private final int millisecondsWithinMinute;
    private final int minute;
    private final int hour;
    private final boolean invalid;
    private final boolean summerTime;

    private Iec103TimeTag(byte[] rawBytes, int millisecondsWithinMinute, int minute, int hour,
                          boolean invalid, boolean summerTime) {
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.millisecondsWithinMinute = millisecondsWithinMinute;
        this.minute = minute;
        this.hour = hour;
        this.invalid = invalid;
        this.summerTime = summerTime;
    }

    public static Iec103TimeTag parse(byte[] bytes, int offset) {
        if (bytes == null || offset < 0 || bytes.length - offset < LENGTH) {
            throw new IllegalArgumentException("IEC103 time tag requires 4 bytes");
        }
        byte[] rawBytes = ByteArrayUtil.copyOfRange(bytes, offset, offset + LENGTH);
        int millisecondsWithinMinute = ByteArrayUtil.unsignedByte(rawBytes[0])
                | (ByteArrayUtil.unsignedByte(rawBytes[1]) << 8);
        int minuteRaw = ByteArrayUtil.unsignedByte(rawBytes[2]);
        int hourRaw = ByteArrayUtil.unsignedByte(rawBytes[3]);
        int minute = minuteRaw & 0x3F;
        int hour = hourRaw & 0x1F;
        boolean invalid = (minuteRaw & 0x80) != 0;
        boolean summerTime = (hourRaw & 0x80) != 0;
        return new Iec103TimeTag(rawBytes, millisecondsWithinMinute, minute, hour, invalid, summerTime);
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

    public int getHour() {
        return hour;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean isSummerTime() {
        return summerTime;
    }
}
