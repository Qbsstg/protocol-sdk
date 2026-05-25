package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.ByteStreamDecoder;
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class Iec103StreamDecoder implements ByteStreamDecoder<Iec103Frame> {

    private static final int SINGLE_CHARACTER_ACK = 0xE5;
    private static final int FIXED_START = 0x10;
    private static final int VARIABLE_START = 0x68;
    private static final int END = 0x16;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final Iec103ParserConfig config;

    public Iec103StreamDecoder() {
        this(Iec103ParserConfig.defaultUnbalanced());
    }

    public Iec103StreamDecoder(Iec103ParserConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("config must not be null");
        }
        this.config = config;
    }

    public List<ParseResult<Iec103Frame>> decode(byte[] input) {
        if (input != null && input.length > 0) {
            buffer.write(input, 0, input.length);
        }

        List<ParseResult<Iec103Frame>> results = new ArrayList<ParseResult<Iec103Frame>>();
        byte[] bytes = buffer.toByteArray();
        int position = 0;

        while (position < bytes.length) {
            int start = findStart(bytes, position);
            if (start < 0) {
                if (position < bytes.length) {
                    results.add(ParseResult.<Iec103Frame>error("Noise before IEC103 frame", bytes.length - position));
                }
                position = bytes.length;
                break;
            }

            if (start > position) {
                results.add(ParseResult.<Iec103Frame>error("Noise before IEC103 frame", start - position));
                position = start;
                continue;
            }

            int startByte = ByteArrayUtil.unsignedByte(bytes[start]);
            if (startByte == SINGLE_CHARACTER_ACK) {
                results.add(ParseResult.success(new Iec103Frame(
                        ByteArrayUtil.copyOfRange(bytes, start, start + 1),
                        Iec103FrameFormat.SINGLE_CHARACTER, null, null, null), 1));
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
                results.add(ParseResult.<Iec103Frame>error("Invalid IEC103 variable-length header", 1));
                position = start + 1;
                continue;
            }
            if (length1 < 1 + config.getLinkAddressLength()) {
                results.add(ParseResult.<Iec103Frame>error("Invalid IEC103 variable-length payload size: " + length1, 1));
                position = start + 1;
                continue;
            }

            int totalLength = 6 + length1;
            if (totalLength > config.getMaxFrameLength()) {
                results.add(ParseResult.<Iec103Frame>error("IEC103 frame length exceeds maxFrameLength: " + totalLength, 1));
                position = start + 1;
                continue;
            }
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

    public Iec103ParserConfig getConfig() {
        return config;
    }

    private ParseResult<Iec103Frame> parseFixedFrame(byte[] bytes, int start, int totalLength) {
        int endIndex = start + totalLength - 1;
        if (ByteArrayUtil.unsignedByte(bytes[endIndex]) != END) {
            return ParseResult.error("Invalid IEC103 fixed-length frame end", 1);
        }

        int checksumIndex = endIndex - 1;
        int expectedChecksum = checksum(bytes, start + 1, checksumIndex);
        int actualChecksum = ByteArrayUtil.unsignedByte(bytes[checksumIndex]);
        if (config.isStrictChecksum() && expectedChecksum != actualChecksum) {
            return ParseResult.error("Invalid IEC103 checksum: expected " + expectedChecksum
                    + ", actual " + actualChecksum, totalLength);
        }

        int controlRaw = ByteArrayUtil.unsignedByte(bytes[start + 1]);
        int linkAddress = readUnsignedLittleEndian(bytes, start + 2, config.getLinkAddressLength());
        Iec103LinkControl control = new Iec103LinkControl(controlRaw);
        byte[] rawBytes = ByteArrayUtil.copyOfRange(bytes, start, start + totalLength);
        return ParseResult.success(new Iec103Frame(rawBytes, Iec103FrameFormat.FIXED_LENGTH,
                control, Integer.valueOf(linkAddress), null), totalLength);
    }

    private ParseResult<Iec103Frame> parseVariableFrame(byte[] bytes, int start, int payloadLength, int totalLength) {
        int endIndex = start + totalLength - 1;
        if (ByteArrayUtil.unsignedByte(bytes[endIndex]) != END) {
            return ParseResult.error("Invalid IEC103 variable-length frame end", totalLength);
        }

        int payloadOffset = start + 4;
        int checksumIndex = payloadOffset + payloadLength;
        int expectedChecksum = checksum(bytes, payloadOffset, checksumIndex);
        int actualChecksum = ByteArrayUtil.unsignedByte(bytes[checksumIndex]);
        if (config.isStrictChecksum() && expectedChecksum != actualChecksum) {
            return ParseResult.error("Invalid IEC103 checksum: expected " + expectedChecksum
                    + ", actual " + actualChecksum, totalLength);
        }

        int controlRaw = ByteArrayUtil.unsignedByte(bytes[payloadOffset]);
        int linkAddressOffset = payloadOffset + 1;
        int linkAddress = readUnsignedLittleEndian(bytes, linkAddressOffset, config.getLinkAddressLength());
        int asduOffset = linkAddressOffset + config.getLinkAddressLength();
        int asduLength = checksumIndex - asduOffset;
        byte[] asduBytes = ByteArrayUtil.copyOfRange(bytes, asduOffset, checksumIndex);
        Iec103Asdu asdu = asduLength == 0 ? null : parseAsdu(asduBytes);
        Iec103LinkControl control = new Iec103LinkControl(controlRaw);
        byte[] rawBytes = ByteArrayUtil.copyOfRange(bytes, start, start + totalLength);
        return ParseResult.success(new Iec103Frame(rawBytes, Iec103FrameFormat.VARIABLE_LENGTH,
                control, Integer.valueOf(linkAddress), asdu), totalLength);
    }

    private Iec103Asdu parseAsdu(byte[] asduBytes) {
        int headerLength = 3 + config.getCommonAddressLength();
        if (asduBytes.length < headerLength) {
            return null;
        }

        int typeId = ByteArrayUtil.unsignedByte(asduBytes[0]);
        Iec103VariableStructureQualifier qualifier =
                new Iec103VariableStructureQualifier(ByteArrayUtil.unsignedByte(asduBytes[1]));
        int causeCode = ByteArrayUtil.unsignedByte(asduBytes[2]);
        int commonAddress = readUnsignedLittleEndian(asduBytes, 3, config.getCommonAddressLength());
        List<Iec103InformationElement> elements = parseInformationElements(asduBytes, headerLength, qualifier, typeId);
        return new Iec103Asdu(typeId, qualifier, causeCode, commonAddress, asduBytes, elements);
    }

    private List<Iec103InformationElement> parseInformationElements(byte[] asduBytes, int position,
                                                                    Iec103VariableStructureQualifier qualifier,
                                                                    int typeId) {
        List<Iec103InformationElement> elements = new ArrayList<Iec103InformationElement>();
        int objectCount = qualifier.getNumberOfObjects();
        if (objectCount == 0) {
            return elements;
        }

        Iec103AsduType asduType = Iec103AsduType.fromTypeId(typeId);
        int payloadLength = asduType.getInformationElementPayloadLength();
        if (payloadLength < 0) {
            addVariableLengthElement(elements, asduType, asduBytes, position);
            return elements;
        }

        for (int i = 0; i < objectCount; i++) {
            int rawElementLength = 2 + payloadLength;
            if (position + rawElementLength > asduBytes.length) {
                break;
            }
            int functionType = ByteArrayUtil.unsignedByte(asduBytes[position]);
            int informationNumber = ByteArrayUtil.unsignedByte(asduBytes[position + 1]);
            byte[] payloadBytes = ByteArrayUtil.copyOfRange(asduBytes, position + 2, position + rawElementLength);
            byte[] rawElementBytes = ByteArrayUtil.copyOfRange(asduBytes, position, position + rawElementLength);
            elements.add(new Iec103InformationElement(i, functionType, informationNumber, payloadBytes, rawElementBytes,
                    parseInformationValue(asduType, functionType, informationNumber, payloadBytes, rawElementBytes)));
            position += rawElementLength;
        }

        return elements;
    }

    private void addVariableLengthElement(List<Iec103InformationElement> elements, Iec103AsduType asduType,
                                          byte[] asduBytes, int position) {
        if (position + 2 > asduBytes.length) {
            return;
        }
        int functionType = ByteArrayUtil.unsignedByte(asduBytes[position]);
        int informationNumber = ByteArrayUtil.unsignedByte(asduBytes[position + 1]);
        boolean preservePayload = !Iec103AsduType.UNKNOWN.equals(asduType) || config.isPreserveUnknownTypePayload();
        byte[] payloadBytes = preservePayload
                ? ByteArrayUtil.copyOfRange(asduBytes, position + 2, asduBytes.length)
                : new byte[0];
        byte[] rawElementBytes = preservePayload
                ? ByteArrayUtil.copyOfRange(asduBytes, position, asduBytes.length)
                : ByteArrayUtil.copyOfRange(asduBytes, position, position + 2);
        elements.add(new Iec103InformationElement(0, functionType, informationNumber, payloadBytes, rawElementBytes,
                parseInformationValue(asduType, functionType, informationNumber, payloadBytes, rawElementBytes)));
    }

    private Iec103InformationValue parseInformationValue(Iec103AsduType asduType, int functionType,
                                                         int informationNumber, byte[] payloadBytes,
                                                         byte[] rawElementBytes) {
        switch (asduType) {
            case TIME_TAGGED_MESSAGE:
                return parseTimeTaggedMessage(asduType, functionType, informationNumber, payloadBytes, rawElementBytes);
            case TIME_TAGGED_MESSAGE_WITH_RELATIVE_TIME:
                return parseTimeTaggedMessageWithRelativeTime(asduType, functionType, informationNumber,
                        payloadBytes, rawElementBytes);
            case MEASURANDS_I:
                return parseMeasuredValue(asduType, functionType, informationNumber, Iec103MeasuredValueKind.MEASURANDS_I,
                        payloadBytes, rawElementBytes);
            case MEASURANDS_II:
                return parseMeasuredValue(asduType, functionType, informationNumber, Iec103MeasuredValueKind.MEASURANDS_II,
                        payloadBytes, rawElementBytes);
            case IDENTIFICATION:
                return new Iec103IdentificationValue(functionType, informationNumber, payloadBytes);
            default:
                return null;
        }
    }

    private Iec103ProtectionEventValue parseTimeTaggedMessage(Iec103AsduType asduType, int functionType,
                                                              int informationNumber, byte[] payloadBytes,
                                                              byte[] rawElementBytes) {
        if (payloadBytes.length < 5) {
            return null;
        }
        int rawEvent = ByteArrayUtil.unsignedByte(payloadBytes[0]);
        Iec103TimeTag timeTag = Iec103TimeTag.parse(payloadBytes, 1);
        return new Iec103ProtectionEventValue(asduType, functionType, informationNumber, rawEvent,
                null, null, timeTag, rawElementBytes);
    }

    private Iec103ProtectionEventValue parseTimeTaggedMessageWithRelativeTime(Iec103AsduType asduType,
                                                                              int functionType,
                                                                              int informationNumber,
                                                                              byte[] payloadBytes,
                                                                              byte[] rawElementBytes) {
        if (payloadBytes.length < 9) {
            return null;
        }
        int rawEvent = ByteArrayUtil.unsignedByte(payloadBytes[0]);
        int relativeTimeMillis = readUnsignedLittleEndian(payloadBytes, 1, 2);
        int faultNumber = readUnsignedLittleEndian(payloadBytes, 3, 2);
        Iec103TimeTag timeTag = Iec103TimeTag.parse(payloadBytes, 5);
        return new Iec103ProtectionEventValue(asduType, functionType, informationNumber, rawEvent,
                Integer.valueOf(relativeTimeMillis), Integer.valueOf(faultNumber), timeTag, rawElementBytes);
    }

    private Iec103MeasuredValue parseMeasuredValue(Iec103AsduType asduType, int functionType, int informationNumber,
                                                   Iec103MeasuredValueKind kind, byte[] payloadBytes,
                                                   byte[] rawElementBytes) {
        if (payloadBytes.length < 3) {
            return null;
        }
        short rawValue = readSignedLittleEndian16(payloadBytes, 0);
        double value = rawValue / 32768.0d;
        int quality = ByteArrayUtil.unsignedByte(payloadBytes[2]);
        return new Iec103MeasuredValue(asduType, functionType, informationNumber, kind, rawValue,
                value, quality, rawElementBytes);
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
