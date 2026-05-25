package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

public final class ModbusDatagramDecoder {

    private static final int MBAP_HEADER_LENGTH = 7;

    private final ModbusParserConfig config;

    public ModbusDatagramDecoder() {
        this(ModbusParserConfig.defaults());
    }

    public ModbusDatagramDecoder(ModbusParserConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.config = config;
    }

    public ParseResult<ModbusTcpAdu> decode(byte[] datagram) {
        if (datagram == null || datagram.length < MBAP_HEADER_LENGTH) {
            return ParseResult.incomplete();
        }

        int protocolId = readUnsignedShort(datagram, 2);
        if (config.isValidateProtocolId() && protocolId != 0) {
            return ParseResult.error("Invalid Modbus protocol id: " + protocolId, datagram.length);
        }

        int length = readUnsignedShort(datagram, 4);
        if (length < 2) {
            return ParseResult.error("Invalid Modbus ADU length: " + length, datagram.length);
        }

        int totalLength = 6 + length;
        if (totalLength > config.getMaxAduLength()) {
            return ParseResult.error("Invalid Modbus ADU length: " + length, datagram.length);
        }
        if (datagram.length < totalLength) {
            return ParseResult.incomplete();
        }
        if (config.isStrictDatagramLength() && datagram.length != totalLength) {
            return ParseResult.error("Invalid Modbus UDP datagram length: declared "
                    + totalLength + " bytes but received " + datagram.length, datagram.length);
        }

        byte[] aduBytes = ByteArrayUtil.copyOfRange(datagram, 0, totalLength);
        return ModbusWireParser.parseAdu(aduBytes, config, true);
    }

    private int readUnsignedShort(byte[] bytes, int offset) {
        return (ByteArrayUtil.unsignedByte(bytes[offset]) << 8)
                | ByteArrayUtil.unsignedByte(bytes[offset + 1]);
    }
}
