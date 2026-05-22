package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.ProtocolFrame;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec101Frame implements ProtocolFrame {

    private final byte[] rawBytes;
    private final Iec101FrameFormat format;
    private final Iec101LinkControl linkControl;
    private final Integer linkAddress;
    private final Iec101Asdu asdu;

    public Iec101Frame(byte[] rawBytes, Iec101FrameFormat format, Iec101LinkControl linkControl,
                       Integer linkAddress, Iec101Asdu asdu) {
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.format = format;
        this.linkControl = linkControl;
        this.linkAddress = linkAddress;
        this.asdu = asdu;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }

    public String getFrameType() {
        return format.name();
    }

    public Iec101FrameFormat getFormat() {
        return format;
    }

    public Iec101LinkControl getLinkControl() {
        return linkControl;
    }

    public Integer getLinkAddress() {
        return linkAddress;
    }

    public Iec101Asdu getAsdu() {
        return asdu;
    }
}
