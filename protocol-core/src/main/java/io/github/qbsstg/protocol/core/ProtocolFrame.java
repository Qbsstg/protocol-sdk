package io.github.qbsstg.protocol.core;

public interface ProtocolFrame {

    byte[] getRawBytes();

    String getFrameType();
}
