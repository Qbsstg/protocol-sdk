package io.github.qbsstg.protocol.iec101;

public interface Iec101TimeTag {

    byte[] getRawBytes();

    int getMillisecondsWithinMinute();

    int getSecond();

    int getMillisecond();

    int getMinute();

    boolean isInvalid();
}
