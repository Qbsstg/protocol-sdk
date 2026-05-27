package io.github.qbsstg.protocol.modbus;

import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

final class ModbusWireParser {

    private static final int MAX_READ_BITS_QUANTITY = 2000;
    private static final int MAX_READ_REGISTERS_QUANTITY = 125;
    private static final int MAX_WRITE_MULTIPLE_COILS_QUANTITY = 1968;
    private static final int MAX_WRITE_MULTIPLE_REGISTERS_QUANTITY = 123;

    private ModbusWireParser() {
    }

    static ParseResult<ModbusTcpAdu> parseAdu(byte[] aduBytes, ModbusParserConfig config, boolean datagram) {
        int transactionId = readUnsignedShort(aduBytes, 0);
        int protocolId = readUnsignedShort(aduBytes, 2);
        int length = readUnsignedShort(aduBytes, 4);
        int unitId = ByteArrayUtil.unsignedByte(aduBytes[6]);

        if (config.isValidateProtocolId() && protocolId != 0) {
            return ParseResult.error("Invalid Modbus protocol id: " + protocolId, aduBytes.length);
        }
        if (length < 2) {
            return ParseResult.error("Invalid Modbus ADU length: " + length, aduBytes.length);
        }
        int expectedLength = 6 + length;
        if (expectedLength > config.getMaxAduLength()) {
            return ParseResult.error("Invalid Modbus ADU length: " + length, aduBytes.length);
        }
        if (aduBytes.length != expectedLength) {
            String transport = datagram ? "datagram" : "frame";
            return ParseResult.error("Invalid Modbus " + transport + " length: expected "
                    + expectedLength + " bytes but received " + aduBytes.length, aduBytes.length);
        }

        byte[] pduBytes = ByteArrayUtil.copyOfRange(aduBytes, 7, aduBytes.length);
        PduParse pduParse = parsePdu(pduBytes);
        if (pduParse.errorMessage != null) {
            return ParseResult.error(pduParse.errorMessage, aduBytes.length);
        }

        ModbusTcpAdu adu = new ModbusTcpAdu(transactionId, protocolId, length, unitId,
                pduParse.pdu, aduBytes);
        return ParseResult.success(adu, aduBytes.length);
    }

    private static PduParse parsePdu(byte[] pduBytes) {
        if (pduBytes.length < 1) {
            return PduParse.error("Invalid Modbus PDU length: " + pduBytes.length);
        }

        int functionCode = ByteArrayUtil.unsignedByte(pduBytes[0]);
        if ((functionCode & 0x80) != 0) {
            return parseException(functionCode, pduBytes);
        }

        switch (functionCode) {
            case 0x01:
            case 0x02:
                return parseBitRead(functionCode, pduBytes);
            case 0x03:
            case 0x04:
                return parseRegisterRead(functionCode, pduBytes);
            case 0x05:
            case 0x06:
                return parseWriteSingle(functionCode, pduBytes);
            case 0x0F:
                return parseWriteMultipleBits(functionCode, pduBytes);
            case 0x10:
                return parseWriteMultipleRegisters(functionCode, pduBytes);
            case 0x17:
                return PduParse.success(new ModbusPdu(functionCode,
                        new ModbusRawValue(ByteArrayUtil.copyOfRange(pduBytes, 1, pduBytes.length)), pduBytes));
            default:
                return PduParse.success(new ModbusPdu(functionCode,
                        new ModbusRawValue(ByteArrayUtil.copyOfRange(pduBytes, 1, pduBytes.length)), pduBytes));
        }
    }

    private static PduParse parseException(int functionCode, byte[] pduBytes) {
        if (pduBytes.length != 2) {
            return PduParse.error("Invalid Modbus exception response length: " + pduBytes.length);
        }
        int exceptionCode = ByteArrayUtil.unsignedByte(pduBytes[1]);
        return PduParse.success(new ModbusPdu(functionCode,
                new ModbusExceptionResponse(functionCode, exceptionCode, pduBytes), pduBytes));
    }

    private static PduParse parseBitRead(int functionCode, byte[] pduBytes) {
        if (pduBytes.length == 5) {
            int quantity = readUnsignedShort(pduBytes, 3);
            PduParse validation = validateQuantity("bit read quantity", quantity, 1, MAX_READ_BITS_QUANTITY);
            if (validation != null) {
                return validation;
            }
            return PduParse.success(new ModbusPdu(functionCode,
                    new ModbusAddressRange(readUnsignedShort(pduBytes, 1), quantity),
                    pduBytes));
        }
        if (pduBytes.length < 2) {
            return PduParse.error("Invalid Modbus bit response length: " + pduBytes.length);
        }
        int byteCount = ByteArrayUtil.unsignedByte(pduBytes[1]);
        if (byteCount < 1 || byteCount > 250) {
            return PduParse.error("Invalid Modbus bit response byte count: " + byteCount);
        }
        if (pduBytes.length != 2 + byteCount) {
            return PduParse.error("Invalid Modbus bit response byte count: " + byteCount);
        }
        byte[] rawData = ByteArrayUtil.copyOfRange(pduBytes, 2, pduBytes.length);
        return PduParse.success(new ModbusPdu(functionCode,
                new ModbusBitValues(byteCount, unpackBits(rawData, byteCount * 8), rawData), pduBytes));
    }

    private static PduParse parseRegisterRead(int functionCode, byte[] pduBytes) {
        if (pduBytes.length == 5) {
            int quantity = readUnsignedShort(pduBytes, 3);
            PduParse validation = validateQuantity("register read quantity", quantity, 1,
                    MAX_READ_REGISTERS_QUANTITY);
            if (validation != null) {
                return validation;
            }
            return PduParse.success(new ModbusPdu(functionCode,
                    new ModbusAddressRange(readUnsignedShort(pduBytes, 1), quantity),
                    pduBytes));
        }
        if (pduBytes.length < 2) {
            return PduParse.error("Invalid Modbus register response length: " + pduBytes.length);
        }
        int byteCount = ByteArrayUtil.unsignedByte(pduBytes[1]);
        if (byteCount < 2 || byteCount > 250) {
            return PduParse.error("Invalid Modbus register response byte count: " + byteCount);
        }
        if (pduBytes.length != 2 + byteCount) {
            return PduParse.error("Invalid Modbus register response byte count: " + byteCount);
        }
        if ((byteCount & 0x01) != 0) {
            return PduParse.error("Invalid Modbus register response byte count: " + byteCount);
        }
        byte[] rawData = ByteArrayUtil.copyOfRange(pduBytes, 2, pduBytes.length);
        return PduParse.success(new ModbusPdu(functionCode,
                new ModbusRegisterValues(byteCount, unpackRegisters(rawData), rawData), pduBytes));
    }

    private static PduParse parseWriteSingle(int functionCode, byte[] pduBytes) {
        if (pduBytes.length != 5) {
            return PduParse.error("Invalid Modbus write single length: " + pduBytes.length);
        }
        int value = readUnsignedShort(pduBytes, 3);
        if (functionCode == 0x05 && value != 0x0000 && value != 0xFF00) {
            return PduParse.error("Invalid Modbus write single coil value: " + value);
        }
        return PduParse.success(new ModbusPdu(functionCode,
                new ModbusWriteSingleValue(readUnsignedShort(pduBytes, 1), value),
                pduBytes));
    }

    private static PduParse parseWriteMultipleBits(int functionCode, byte[] pduBytes) {
        if (pduBytes.length == 5) {
            int quantity = readUnsignedShort(pduBytes, 3);
            PduParse validation = validateQuantity("write multiple coils quantity", quantity, 1,
                    MAX_WRITE_MULTIPLE_COILS_QUANTITY);
            if (validation != null) {
                return validation;
            }
            return PduParse.success(new ModbusPdu(functionCode,
                    new ModbusAddressRange(readUnsignedShort(pduBytes, 1), quantity),
                    pduBytes));
        }
        if (pduBytes.length < 6) {
            return PduParse.error("Invalid Modbus write multiple coils length: " + pduBytes.length);
        }
        int startAddress = readUnsignedShort(pduBytes, 1);
        int quantity = readUnsignedShort(pduBytes, 3);
        PduParse validation = validateQuantity("write multiple coils quantity", quantity, 1,
                MAX_WRITE_MULTIPLE_COILS_QUANTITY);
        if (validation != null) {
            return validation;
        }
        int byteCount = ByteArrayUtil.unsignedByte(pduBytes[5]);
        int expectedByteCount = (quantity + 7) / 8;
        if (byteCount != expectedByteCount) {
            return PduParse.error("Invalid Modbus write multiple coils byte count: " + byteCount);
        }
        if (pduBytes.length != 6 + byteCount) {
            return PduParse.error("Invalid Modbus write multiple coils byte count: " + byteCount);
        }
        byte[] rawData = ByteArrayUtil.copyOfRange(pduBytes, 6, pduBytes.length);
        return PduParse.success(new ModbusPdu(functionCode,
                new ModbusWriteMultipleBitsValue(new ModbusAddressRange(startAddress, quantity),
                        byteCount, unpackBits(rawData, quantity), rawData),
                pduBytes));
    }

    private static PduParse parseWriteMultipleRegisters(int functionCode, byte[] pduBytes) {
        if (pduBytes.length == 5) {
            int quantity = readUnsignedShort(pduBytes, 3);
            PduParse validation = validateQuantity("write multiple registers quantity", quantity, 1,
                    MAX_WRITE_MULTIPLE_REGISTERS_QUANTITY);
            if (validation != null) {
                return validation;
            }
            return PduParse.success(new ModbusPdu(functionCode,
                    new ModbusAddressRange(readUnsignedShort(pduBytes, 1), quantity),
                    pduBytes));
        }
        if (pduBytes.length < 6) {
            return PduParse.error("Invalid Modbus write multiple registers length: " + pduBytes.length);
        }
        int startAddress = readUnsignedShort(pduBytes, 1);
        int quantity = readUnsignedShort(pduBytes, 3);
        PduParse validation = validateQuantity("write multiple registers quantity", quantity, 1,
                MAX_WRITE_MULTIPLE_REGISTERS_QUANTITY);
        if (validation != null) {
            return validation;
        }
        int byteCount = ByteArrayUtil.unsignedByte(pduBytes[5]);
        if (byteCount != quantity * 2) {
            return PduParse.error("Invalid Modbus write multiple registers byte count: " + byteCount);
        }
        if (pduBytes.length != 6 + byteCount) {
            return PduParse.error("Invalid Modbus write multiple registers byte count: " + byteCount);
        }
        if ((byteCount & 0x01) != 0) {
            return PduParse.error("Invalid Modbus write multiple registers byte count: " + byteCount);
        }
        byte[] rawData = ByteArrayUtil.copyOfRange(pduBytes, 6, pduBytes.length);
        return PduParse.success(new ModbusPdu(functionCode,
                new ModbusWriteMultipleRegistersValue(new ModbusAddressRange(startAddress, quantity),
                        byteCount, unpackRegisters(rawData), rawData),
                pduBytes));
    }

    private static boolean[] unpackBits(byte[] rawData, int count) {
        boolean[] values = new boolean[count];
        int index = 0;
        for (int i = 0; i < rawData.length && index < count; i++) {
            int value = ByteArrayUtil.unsignedByte(rawData[i]);
            for (int bit = 0; bit < 8 && index < count; bit++) {
                values[index] = ((value >> bit) & 0x01) == 1;
                index++;
            }
        }
        return values;
    }

    private static int[] unpackRegisters(byte[] rawData) {
        int[] values = new int[rawData.length / 2];
        for (int i = 0; i < values.length; i++) {
            values[i] = readUnsignedShort(rawData, i * 2);
        }
        return values;
    }

    private static PduParse validateQuantity(String field, int quantity, int min, int max) {
        if (quantity < min || quantity > max) {
            return PduParse.error("Invalid Modbus " + field + ": " + quantity);
        }
        return null;
    }

    private static int readUnsignedShort(byte[] bytes, int offset) {
        return (ByteArrayUtil.unsignedByte(bytes[offset]) << 8)
                | ByteArrayUtil.unsignedByte(bytes[offset + 1]);
    }

    private static final class PduParse {
        private final ModbusPdu pdu;
        private final String errorMessage;

        private PduParse(ModbusPdu pdu, String errorMessage) {
            this.pdu = pdu;
            this.errorMessage = errorMessage;
        }

        private static PduParse success(ModbusPdu pdu) {
            return new PduParse(pdu, null);
        }

        private static PduParse error(String message) {
            return new PduParse(null, message);
        }
    }
}
