package io.github.qbsstg.protocol.core;

import java.util.List;

public interface ByteStreamDecoder<T extends ProtocolFrame> {

    List<ParseResult<T>> decode(byte[] input);

    void reset();
}
