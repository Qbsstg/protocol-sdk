package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.ByteStreamDecoder;
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class Iec101StreamDecoder implements ByteStreamDecoder<Iec101Frame> {

    private static final int SINGLE_CHARACTER_ACK = 0xE5;
    private static final int FIXED_START = 0x10;
    private static final int VARIABLE_START = 0x68;
    private static final int END = 0x16;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final Iec101ParserConfig config;

    public Iec101StreamDecoder() {
        this(Iec101ParserConfig.defaultUnbalanced());
    }

    public Iec101StreamDecoder(Iec101ParserConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.config = config;
    }

    public List<ParseResult<Iec101Frame>> decode(byte[] input) {
        if (input != null && input.length > 0) {
            buffer.write(input, 0, input.length);
        }

        List<ParseResult<Iec101Frame>> results = new ArrayList<ParseResult<Iec101Frame>>();
        byte[] bytes = buffer.toByteArray();
        int position = 0;

        while (position < bytes.length) {
            int start = findStart(bytes, position);
            if (start < 0) {
                if (position < bytes.length) {
                    results.add(ParseResult.<Iec101Frame>error("Noise before IEC101 frame", bytes.length - position));
                }
                position = bytes.length;
                break;
            }

            if (start > position) {
                results.add(ParseResult.<Iec101Frame>error("Noise before IEC101 frame", start - position));
                position = start;
                continue;
            }

            int startByte = ByteArrayUtil.unsignedByte(bytes[start]);
            if (startByte == SINGLE_CHARACTER_ACK) {
                results.add(ParseResult.success(new Iec101Frame(
                        ByteArrayUtil.copyOfRange(bytes, start, start + 1),
                        Iec101FrameFormat.SINGLE_CHARACTER, null, null, null), 1));
                position = start + 1;
                continue;
            }

            if (startByte == FIXED_START) {
                int totalLength = 4 + config.getLinkAddressLength();
                if (bytes.length - start < totalLength) {
                    position = start;
                    break;
                }
                results.add(parseFixedFrame(bytes, start, totalLength));
                position = start + results.get(results.size() - 1).getConsumedBytes();
                continue;
            }

            if (bytes.length - start < 4) {
                position = start;
                break;
            }

            int length1 = ByteArrayUtil.unsignedByte(bytes[start + 1]);
            int length2 = ByteArrayUtil.unsignedByte(bytes[start + 2]);
            if (length1 != length2 || ByteArrayUtil.unsignedByte(bytes[start + 3]) != VARIABLE_START) {
                results.add(ParseResult.<Iec101Frame>error("Invalid IEC101 variable-length header", 1));
                position = start + 1;
                continue;
            }
            if (length1 < 1 + config.getLinkAddressLength()) {
                results.add(ParseResult.<Iec101Frame>error("Invalid IEC101 variable-length payload size: " + length1, 1));
                position = start + 1;
                continue;
            }

            int totalLength = 6 + length1;
            if (bytes.length - start < totalLength) {
                position = start;
                break;
            }
            results.add(parseVariableFrame(bytes, start, length1, totalLength));
            position = start + results.get(results.size() - 1).getConsumedBytes();
        }

        retain(bytes, position);
        return results;
    }

    public void reset() {
        buffer.reset();
    }

    public Iec101ParserConfig getConfig() {
        return config;
    }

    private ParseResult<Iec101Frame> parseFixedFrame(byte[] bytes, int start, int totalLength) {
        int endIndex = start + totalLength - 1;
        if (ByteArrayUtil.unsignedByte(bytes[endIndex]) != END) {
            return ParseResult.error("Invalid IEC101 fixed-length frame end", 1);
        }

        int checksumIndex = endIndex - 1;
        int expectedChecksum = checksum(bytes, start + 1, checksumIndex);
        int actualChecksum = ByteArrayUtil.unsignedByte(bytes[checksumIndex]);
        if (expectedChecksum != actualChecksum) {
            return ParseResult.error("Invalid IEC101 checksum: expected " + expectedChecksum
                    + ", actual " + actualChecksum, totalLength);
        }

        int controlRaw = ByteArrayUtil.unsignedByte(bytes[start + 1]);
        int linkAddress = readUnsignedLittleEndian(bytes, start + 2, config.getLinkAddressLength());
        Iec101LinkControl control = new Iec101LinkControl(controlRaw, config.getTransmissionMode());
        byte[] rawBytes = ByteArrayUtil.copyOfRange(bytes, start, start + totalLength);
        return ParseResult.success(new Iec101Frame(rawBytes, Iec101FrameFormat.FIXED_LENGTH,
                control, Integer.valueOf(linkAddress), null), totalLength);
    }

    private ParseResult<Iec101Frame> parseVariableFrame(byte[] bytes, int start, int payloadLength, int totalLength) {
        int endIndex = start + totalLength - 1;
        if (ByteArrayUtil.unsignedByte(bytes[endIndex]) != END) {
            return ParseResult.error("Invalid IEC101 variable-length frame end", totalLength);
        }

        int payloadOffset = start + 4;
        int checksumIndex = payloadOffset + payloadLength;
        int expectedChecksum = checksum(bytes, payloadOffset, checksumIndex);
        int actualChecksum = ByteArrayUtil.unsignedByte(bytes[checksumIndex]);
        if (expectedChecksum != actualChecksum) {
            return ParseResult.error("Invalid IEC101 checksum: expected " + expectedChecksum
                    + ", actual " + actualChecksum, totalLength);
        }

        int controlRaw = ByteArrayUtil.unsignedByte(bytes[payloadOffset]);
        int linkAddressOffset = payloadOffset + 1;
        int linkAddress = readUnsignedLittleEndian(bytes, linkAddressOffset, config.getLinkAddressLength());
        int asduOffset = linkAddressOffset + config.getLinkAddressLength();
        int asduLength = checksumIndex - asduOffset;
        byte[] asduBytes = ByteArrayUtil.copyOfRange(bytes, asduOffset, checksumIndex);
        Iec101Asdu asdu = asduLength == 0 ? null : parseAsdu(asduBytes);
        Iec101LinkControl control = new Iec101LinkControl(controlRaw, config.getTransmissionMode());
        byte[] rawBytes = ByteArrayUtil.copyOfRange(bytes, start, start + totalLength);
        return ParseResult.success(new Iec101Frame(rawBytes, Iec101FrameFormat.VARIABLE_LENGTH,
                control, Integer.valueOf(linkAddress), asdu), totalLength);
    }

    private Iec101Asdu parseAsdu(byte[] asduBytes) {
        int headerLength = 2 + config.getCauseOfTransmissionLength() + config.getCommonAddressLength();
        if (asduBytes.length < headerLength) {
            return null;
        }

        int typeId = ByteArrayUtil.unsignedByte(asduBytes[0]);
        Iec101VariableStructureQualifier qualifier =
                new Iec101VariableStructureQualifier(ByteArrayUtil.unsignedByte(asduBytes[1]));
        int causeRaw = ByteArrayUtil.unsignedByte(asduBytes[2]);
        int causeCode = causeRaw & 0x3F;
        boolean negativeConfirm = (causeRaw & 0x40) != 0;
        boolean test = (causeRaw & 0x80) != 0;
        int originatorAddress = config.getCauseOfTransmissionLength() == 2
                ? ByteArrayUtil.unsignedByte(asduBytes[3]) : 0;
        int commonAddressOffset = 2 + config.getCauseOfTransmissionLength();
        int commonAddress = readUnsignedLittleEndian(asduBytes, commonAddressOffset,
                config.getCommonAddressLength());
        List<Iec101InformationObject> objects = parseInformationObjects(asduBytes, headerLength, qualifier, typeId);
        return new Iec101Asdu(typeId, qualifier, causeCode, test, negativeConfirm, originatorAddress,
                commonAddress, asduBytes, objects);
    }

    private List<Iec101InformationObject> parseInformationObjects(byte[] asduBytes, int position,
                                                                  Iec101VariableStructureQualifier qualifier,
                                                                  int typeId) {
        List<Iec101InformationObject> objects = new ArrayList<Iec101InformationObject>();
        int objectCount = qualifier.getNumberOfObjects();
        if (objectCount == 0) {
            return objects;
        }

        int addressLength = config.getInformationObjectAddressLength();
        if (position + addressLength > asduBytes.length) {
            return objects;
        }

        Iec101AsduType asduType = Iec101AsduType.fromTypeId(typeId);
        int elementLength = asduType.getInformationElementLength();
        if (elementLength < 0) {
            int address = readUnsignedLittleEndian(asduBytes, position, addressLength);
            byte[] elementBytes = ByteArrayUtil.copyOfRange(asduBytes, position + addressLength, asduBytes.length);
            objects.add(new Iec101InformationObject(0, address, elementBytes,
                    parseInformationValue(asduType, elementBytes)));
            return objects;
        }

        if (qualifier.isSequence()) {
            int baseAddress = readUnsignedLittleEndian(asduBytes, position, addressLength);
            position += addressLength;
            for (int i = 0; i < objectCount; i++) {
                if (position + elementLength > asduBytes.length) {
                    break;
                }
                byte[] elementBytes = ByteArrayUtil.copyOfRange(asduBytes, position, position + elementLength);
                objects.add(new Iec101InformationObject(i, baseAddress + i, elementBytes,
                        parseInformationValue(asduType, elementBytes)));
                position += elementLength;
            }
            return objects;
        }

        for (int i = 0; i < objectCount; i++) {
            if (position + addressLength + elementLength > asduBytes.length) {
                break;
            }
            int address = readUnsignedLittleEndian(asduBytes, position, addressLength);
            position += addressLength;
            byte[] elementBytes = ByteArrayUtil.copyOfRange(asduBytes, position, position + elementLength);
            objects.add(new Iec101InformationObject(i, address, elementBytes,
                    parseInformationValue(asduType, elementBytes)));
            position += elementLength;
        }

        return objects;
    }

    private Iec101InformationValue parseInformationValue(Iec101AsduType asduType, byte[] elementBytes) {
        switch (asduType) {
            case M_SP_NA_1:
                return parseSinglePointValue(asduType, elementBytes);
            case M_DP_NA_1:
                return parseDoublePointValue(asduType, elementBytes);
            case M_ME_NA_1:
                return parseNormalizedMeasuredValue(asduType, elementBytes);
            case M_ME_NB_1:
                return parseScaledMeasuredValue(asduType, elementBytes);
            case M_ME_NC_1:
                return parseShortFloatMeasuredValue(asduType, elementBytes);
            case C_SC_NA_1:
                return parseSingleCommandValue(asduType, elementBytes);
            case C_DC_NA_1:
                return parseDoubleCommandValue(asduType, elementBytes);
            case C_IC_NA_1:
                return parseInterrogationCommandValue(asduType, elementBytes);
            default:
                return null;
        }
    }

    private Iec101SinglePointValue parseSinglePointValue(Iec101AsduType asduType, byte[] elementBytes) {
        if (elementBytes.length < 1) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec101SinglePointValue(asduType, (rawValue & 0x01) != 0,
                Iec101QualityDescriptor.status(rawValue));
    }

    private Iec101DoublePointValue parseDoublePointValue(Iec101AsduType asduType, byte[] elementBytes) {
        if (elementBytes.length < 1) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec101DoublePointValue(asduType, Iec101DoublePointState.fromCode(rawValue),
                Iec101QualityDescriptor.status(rawValue));
    }

    private Iec101MeasuredValue parseNormalizedMeasuredValue(Iec101AsduType asduType, byte[] elementBytes) {
        if (elementBytes.length < 3) {
            return null;
        }
        return Iec101MeasuredValue.normalized(asduType, readSignedLittleEndian16(elementBytes, 0),
                Iec101QualityDescriptor.measurement(ByteArrayUtil.unsignedByte(elementBytes[2])));
    }

    private Iec101MeasuredValue parseScaledMeasuredValue(Iec101AsduType asduType, byte[] elementBytes) {
        if (elementBytes.length < 3) {
            return null;
        }
        return Iec101MeasuredValue.scaled(asduType, readSignedLittleEndian16(elementBytes, 0),
                Iec101QualityDescriptor.measurement(ByteArrayUtil.unsignedByte(elementBytes[2])));
    }

    private Iec101MeasuredValue parseShortFloatMeasuredValue(Iec101AsduType asduType, byte[] elementBytes) {
        if (elementBytes.length < 5) {
            return null;
        }
        int rawBits = readUnsignedLittleEndian(elementBytes, 0, 4);
        return Iec101MeasuredValue.shortFloat(asduType, Float.intBitsToFloat(rawBits), rawBits,
                Iec101QualityDescriptor.measurement(ByteArrayUtil.unsignedByte(elementBytes[4])));
    }

    private Iec101SingleCommandValue parseSingleCommandValue(Iec101AsduType asduType, byte[] elementBytes) {
        if (elementBytes.length < 1) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec101SingleCommandValue(asduType, (rawValue & 0x01) != 0,
                new Iec101CommandQualifier(rawValue));
    }

    private Iec101DoubleCommandValue parseDoubleCommandValue(Iec101AsduType asduType, byte[] elementBytes) {
        if (elementBytes.length < 1) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec101DoubleCommandValue(asduType, Iec101DoubleCommandState.fromCode(rawValue),
                new Iec101CommandQualifier(rawValue));
    }

    private Iec101InterrogationCommandValue parseInterrogationCommandValue(Iec101AsduType asduType,
                                                                           byte[] elementBytes) {
        if (elementBytes.length < 1) {
            return null;
        }
        return new Iec101InterrogationCommandValue(asduType, ByteArrayUtil.unsignedByte(elementBytes[0]));
    }

    private int findStart(byte[] bytes, int from) {
        for (int i = from; i < bytes.length; i++) {
            int value = ByteArrayUtil.unsignedByte(bytes[i]);
            if (value == SINGLE_CHARACTER_ACK || value == FIXED_START || value == VARIABLE_START) {
                return i;
            }
        }
        return -1;
    }

    private void retain(byte[] bytes, int from) {
        buffer.reset();
        if (from < bytes.length) {
            buffer.write(bytes, from, bytes.length - from);
        }
    }

    private int checksum(byte[] bytes, int from, int toExclusive) {
        int value = 0;
        for (int i = from; i < toExclusive; i++) {
            value = (value + ByteArrayUtil.unsignedByte(bytes[i])) & 0xFF;
        }
        return value;
    }

    private int readUnsignedLittleEndian(byte[] bytes, int offset, int length) {
        int value = 0;
        for (int i = 0; i < length; i++) {
            value |= ByteArrayUtil.unsignedByte(bytes[offset + i]) << (8 * i);
        }
        return value;
    }

    private short readSignedLittleEndian16(byte[] bytes, int offset) {
        return (short) readUnsignedLittleEndian(bytes, offset, 2);
    }
}
