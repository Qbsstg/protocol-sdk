package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.ByteStreamDecoder;
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class ModbusTcpStreamDecoder implements ByteStreamDecoder<ModbusTcpAdu> {

    private static final int MBAP_HEADER_LENGTH = 7;

    private final ModbusParserConfig config;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public ModbusTcpStreamDecoder() {
        this(ModbusParserConfig.defaults());
    }

    public ModbusTcpStreamDecoder(ModbusParserConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.config = config;
    }

    public List<ParseResult<ModbusTcpAdu>> decode(byte[] input) {
        if (input != null && input.length > 0) {
            buffer.write(input, 0, input.length);
        }

        List<ParseResult<ModbusTcpAdu>> results = new ArrayList<ParseResult<ModbusTcpAdu>>();
        byte[] bytes = buffer.toByteArray();
        int position = 0;

        while (bytes.length - position >= MBAP_HEADER_LENGTH) {
            int protocolId = readUnsignedShort(bytes, position + 2);
            int length = readUnsignedShort(bytes, position + 4);
            if (config.isValidateProtocolId() && protocolId != 0) {
                results.add(ParseResult.<ModbusTcpAdu>error(
                        "Invalid Modbus protocol id: " + protocolId, 1));
                position += 1;
                continue;
            }
            if (length < 2) {
                results.add(ParseResult.<ModbusTcpAdu>error(
                        "Invalid Modbus ADU length: " + length, MBAP_HEADER_LENGTH));
                position += MBAP_HEADER_LENGTH;
                continue;
            }

            int totalLength = 6 + length;
            if (totalLength > config.getMaxAduLength()) {
                results.add(ParseResult.<ModbusTcpAdu>error(
                        "Invalid Modbus ADU length: " + length, 1));
                position += 1;
                continue;
            }
            if (bytes.length - position < totalLength) {
                break;
            }

            byte[] aduBytes = ByteArrayUtil.copyOfRange(bytes, position, position + totalLength);
            results.add(ModbusWireParser.parseAdu(aduBytes, config, false));
            position += totalLength;
        }

        retain(bytes, position);
        return results;
    }

    public void reset() {
        buffer.reset();
    }

    private void retain(byte[] bytes, int from) {
        buffer.reset();
        if (from < bytes.length) {
            buffer.write(bytes, from, bytes.length - from);
        }
    }

    private int readUnsignedShort(byte[] bytes, int offset) {
        return (ByteArrayUtil.unsignedByte(bytes[offset]) << 8)
                | ByteArrayUtil.unsignedByte(bytes[offset + 1]);
    }
}
