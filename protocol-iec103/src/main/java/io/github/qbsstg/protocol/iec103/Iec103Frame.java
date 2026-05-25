package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.ProtocolFrame;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec103Frame implements ProtocolFrame {

    private final byte[] rawBytes;
    private final Iec103FrameFormat format;
    private final Iec103LinkControl linkControl;
    private final Integer linkAddress;
    private final Iec103Asdu asdu;

    public Iec103Frame(byte[] rawBytes, Iec103FrameFormat format, Iec103LinkControl linkControl,
                       Integer linkAddress, Iec103Asdu asdu) {
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

    public Iec103FrameFormat getFormat() {
        return format;
    }

    public Iec103LinkControl getLinkControl() {
        return linkControl;
    }

    public Integer getLinkAddress() {
        return linkAddress;
    }

    public Iec103Asdu getAsdu() {
        return asdu;
    }
}
