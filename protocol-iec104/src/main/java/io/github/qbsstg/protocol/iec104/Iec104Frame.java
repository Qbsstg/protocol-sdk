package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ProtocolFrame;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class Iec104Frame implements ProtocolFrame {

    private final byte[] rawBytes;
    private final Iec104FrameType type;
    private final Integer sendSequence;
    private final Integer receiveSequence;
    private final Iec104Asdu asdu;

    public Iec104Frame(byte[] rawBytes, Iec104FrameType type, Integer sendSequence,
                       Integer receiveSequence, Iec104Asdu asdu) {
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.type = type;
        this.sendSequence = sendSequence;
        this.receiveSequence = receiveSequence;
        this.asdu = asdu;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }

    public String getFrameType() {
        return type.name();
    }

    public Iec104FrameType getType() {
        return type;
    }

    public Integer getSendSequence() {
        return sendSequence;
    }

    public Integer getReceiveSequence() {
        return receiveSequence;
    }

    public Integer getAsduType() {
        return asdu == null ? null : Integer.valueOf(asdu.getTypeId());
    }

    public Integer getCauseOfTransmission() {
        return asdu == null ? null : Integer.valueOf(asdu.getCauseCode());
    }

    public Iec104Asdu getAsdu() {
        return asdu;
    }
}
