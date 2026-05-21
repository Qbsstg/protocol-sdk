package io.github.qbsstg.protocol.core.util;

import java.util.Arrays;

public final class ByteArrayUtil {

    private ByteArrayUtil() {
    }

    public static byte[] copyOf(byte[] source) {
        if (source == null) {
            return new byte[0];
        }
        return Arrays.copyOf(source, source.length);
    }

    public static byte[] copyOfRange(byte[] source, int from, int to) {
        return Arrays.copyOfRange(source, from, to);
    }

    public static int unsignedByte(byte value) {
        return value & 0xFF;
    }
}
