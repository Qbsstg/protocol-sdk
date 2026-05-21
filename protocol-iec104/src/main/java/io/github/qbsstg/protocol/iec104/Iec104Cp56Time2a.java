package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.time.DateTimeException;
import java.time.LocalDateTime;

public final class Iec104Cp56Time2a {

    private static final int LENGTH = 7;

    private final byte[] rawBytes;
    private final int millisecondsWithinMinute;
    private final int minute;
    private final int hour;
    private final int dayOfMonth;
    private final int dayOfWeek;
    private final int month;
    private final int year;
    private final boolean invalid;
    private final boolean summerTime;
    private final LocalDateTime dateTime;

    private Iec104Cp56Time2a(byte[] rawBytes, int millisecondsWithinMinute, int minute, int hour,
                             int dayOfMonth, int dayOfWeek, int month, int year,
                             boolean invalid, boolean summerTime, LocalDateTime dateTime) {
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.millisecondsWithinMinute = millisecondsWithinMinute;
        this.minute = minute;
        this.hour = hour;
        this.dayOfMonth = dayOfMonth;
        this.dayOfWeek = dayOfWeek;
        this.month = month;
        this.year = year;
        this.invalid = invalid;
        this.summerTime = summerTime;
        this.dateTime = dateTime;
    }

    public static Iec104Cp56Time2a parse(byte[] bytes, int offset) {
        if (bytes == null || offset < 0 || bytes.length - offset < LENGTH) {
            throw new IllegalArgumentException("CP56Time2a requires 7 bytes");
        }

        byte[] rawBytes = ByteArrayUtil.copyOfRange(bytes, offset, offset + LENGTH);
        int millisecondsWithinMinute = ByteArrayUtil.unsignedByte(rawBytes[0])
                | (ByteArrayUtil.unsignedByte(rawBytes[1]) << 8);
        int minuteRaw = ByteArrayUtil.unsignedByte(rawBytes[2]);
        int hourRaw = ByteArrayUtil.unsignedByte(rawBytes[3]);
        int dayRaw = ByteArrayUtil.unsignedByte(rawBytes[4]);
        int monthRaw = ByteArrayUtil.unsignedByte(rawBytes[5]);
        int yearRaw = ByteArrayUtil.unsignedByte(rawBytes[6]);

        int minute = minuteRaw & 0x3F;
        int hour = hourRaw & 0x1F;
        int dayOfMonth = dayRaw & 0x1F;
        int dayOfWeek = (dayRaw >> 5) & 0x07;
        int month = monthRaw & 0x0F;
        int year = 2000 + (yearRaw & 0x7F);
        boolean invalid = (minuteRaw & 0x80) != 0;
        boolean summerTime = (hourRaw & 0x80) != 0;
        LocalDateTime dateTime = toDateTime(year, month, dayOfMonth, hour, minute, millisecondsWithinMinute);

        return new Iec104Cp56Time2a(rawBytes, millisecondsWithinMinute, minute, hour, dayOfMonth,
                dayOfWeek, month, year, invalid, summerTime, dateTime);
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

    public int getDayOfMonth() {
        return dayOfMonth;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean isSummerTime() {
        return summerTime;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    private static LocalDateTime toDateTime(int year, int month, int dayOfMonth, int hour, int minute,
                                            int millisecondsWithinMinute) {
        try {
            int second = millisecondsWithinMinute / 1000;
            int nanos = (millisecondsWithinMinute % 1000) * 1000000;
            return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second, nanos);
        } catch (DateTimeException ex) {
            return null;
        }
    }
}
