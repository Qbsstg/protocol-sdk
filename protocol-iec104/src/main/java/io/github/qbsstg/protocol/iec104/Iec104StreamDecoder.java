package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.ByteStreamDecoder;
import io.github.qbsstg.protocol.core.ParseResult;
import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public final class Iec104StreamDecoder implements ByteStreamDecoder<Iec104Frame> {

    private static final int START_BYTE = 0x68;
    private static final int CONTROL_FIELD_LENGTH = 4;
    private static final int HEADER_LENGTH = 2;
    private static final int APCI_LENGTH = 6;
    private static final int ASDU_HEADER_LENGTH = 6;
    private static final int INFORMATION_OBJECT_ADDRESS_LENGTH = 3;
    private static final int MAX_APDU_LENGTH = 255;

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();

    public List<ParseResult<Iec104Frame>> decode(byte[] input) {
        if (input != null && input.length > 0) {
            buffer.write(input, 0, input.length);
        }

        List<ParseResult<Iec104Frame>> results = new ArrayList<ParseResult<Iec104Frame>>();
        byte[] bytes = buffer.toByteArray();
        int position = 0;

        while (position < bytes.length) {
            int start = findStart(bytes, position);
            if (start < 0) {
                position = bytes.length;
                break;
            }

            if (bytes.length - start < HEADER_LENGTH) {
                position = start;
                break;
            }

            int length = ByteArrayUtil.unsignedByte(bytes[start + 1]);
            if (length < CONTROL_FIELD_LENGTH || length + HEADER_LENGTH > MAX_APDU_LENGTH) {
                results.add(ParseResult.<Iec104Frame>error("Invalid IEC104 APDU length: " + length, 1));
                position = start + 1;
                continue;
            }

            int totalLength = HEADER_LENGTH + length;
            if (bytes.length - start < totalLength) {
                position = start;
                break;
            }

            byte[] frameBytes = ByteArrayUtil.copyOfRange(bytes, start, start + totalLength);
            results.add(ParseResult.success(parseFrame(frameBytes), totalLength));
            position = start + totalLength;
        }

        retain(bytes, position);
        return results;
    }

    public void reset() {
        buffer.reset();
    }

    private int findStart(byte[] bytes, int from) {
        for (int i = from; i < bytes.length; i++) {
            if (ByteArrayUtil.unsignedByte(bytes[i]) == START_BYTE) {
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

    private Iec104Frame parseFrame(byte[] frameBytes) {
        int c0 = ByteArrayUtil.unsignedByte(frameBytes[2]);
        int c1 = ByteArrayUtil.unsignedByte(frameBytes[3]);
        int c2 = ByteArrayUtil.unsignedByte(frameBytes[4]);
        int c3 = ByteArrayUtil.unsignedByte(frameBytes[5]);

        if ((c0 & 0x01) == 0) {
            Integer sendSequence = Integer.valueOf(((c1 << 8) | c0) >> 1);
            Integer receiveSequence = Integer.valueOf(((c3 << 8) | c2) >> 1);
            return new Iec104Frame(frameBytes, Iec104FrameType.I_FORMAT, sendSequence, receiveSequence, parseAsdu(frameBytes));
        }

        if ((c0 & 0x03) == 0x01) {
            Integer receiveSequence = Integer.valueOf(((c3 << 8) | c2) >> 1);
            return new Iec104Frame(frameBytes, Iec104FrameType.S_FORMAT, null, receiveSequence, null);
        }

        return new Iec104Frame(frameBytes, parseUFormatType(c0), null, null, null);
    }

    private Iec104Asdu parseAsdu(byte[] frameBytes) {
        if (frameBytes.length < APCI_LENGTH + ASDU_HEADER_LENGTH) {
            return null;
        }

        int asduOffset = APCI_LENGTH;
        int typeId = ByteArrayUtil.unsignedByte(frameBytes[asduOffset]);
        int vsq = ByteArrayUtil.unsignedByte(frameBytes[asduOffset + 1]);
        int causeRaw = ByteArrayUtil.unsignedByte(frameBytes[asduOffset + 2]);
        int causeCode = causeRaw & 0x3F;
        boolean negativeConfirm = (causeRaw & 0x40) != 0;
        boolean test = (causeRaw & 0x80) != 0;
        int originatorAddress = ByteArrayUtil.unsignedByte(frameBytes[asduOffset + 3]);
        int commonAddress = readUnsignedLittleEndian(frameBytes, asduOffset + 4, 2);
        byte[] rawAsdu = ByteArrayUtil.copyOfRange(frameBytes, asduOffset, frameBytes.length);
        Iec104VariableStructureQualifier qualifier = new Iec104VariableStructureQualifier(vsq);

        return new Iec104Asdu(typeId, qualifier, causeCode, test, negativeConfirm,
                originatorAddress, commonAddress, rawAsdu, parseInformationObjects(frameBytes, qualifier, typeId));
    }

    private List<Iec104InformationObject> parseInformationObjects(byte[] frameBytes,
                                                                  Iec104VariableStructureQualifier qualifier,
                                                                  int typeId) {
        List<Iec104InformationObject> objects = new ArrayList<Iec104InformationObject>();
        int objectCount = qualifier.getNumberOfObjects();
        if (objectCount == 0) {
            return objects;
        }

        int position = APCI_LENGTH + ASDU_HEADER_LENGTH;
        if (position + INFORMATION_OBJECT_ADDRESS_LENGTH > frameBytes.length) {
            return objects;
        }

        Iec104AsduType asduType = Iec104AsduType.fromTypeId(typeId);
        int elementLength = asduType.getInformationElementLength();
        if (elementLength < 0) {
            int address = readUnsignedLittleEndian(frameBytes, position, INFORMATION_OBJECT_ADDRESS_LENGTH);
            byte[] elementBytes = ByteArrayUtil.copyOfRange(frameBytes, position + INFORMATION_OBJECT_ADDRESS_LENGTH, frameBytes.length);
            objects.add(new Iec104InformationObject(0, address, elementBytes,
                    parseInformationValue(asduType, elementBytes)));
            return objects;
        }

        if (qualifier.isSequence()) {
            int baseAddress = readUnsignedLittleEndian(frameBytes, position, INFORMATION_OBJECT_ADDRESS_LENGTH);
            position += INFORMATION_OBJECT_ADDRESS_LENGTH;
            for (int i = 0; i < objectCount; i++) {
                if (position + elementLength > frameBytes.length) {
                    break;
                }
                byte[] elementBytes = ByteArrayUtil.copyOfRange(frameBytes, position, position + elementLength);
                objects.add(new Iec104InformationObject(i, baseAddress + i, elementBytes,
                        parseInformationValue(asduType, elementBytes)));
                position += elementLength;
            }
            return objects;
        }

        for (int i = 0; i < objectCount; i++) {
            if (position + INFORMATION_OBJECT_ADDRESS_LENGTH + elementLength > frameBytes.length) {
                break;
            }
            int address = readUnsignedLittleEndian(frameBytes, position, INFORMATION_OBJECT_ADDRESS_LENGTH);
            position += INFORMATION_OBJECT_ADDRESS_LENGTH;
            byte[] elementBytes = ByteArrayUtil.copyOfRange(frameBytes, position, position + elementLength);
            objects.add(new Iec104InformationObject(i, address, elementBytes,
                    parseInformationValue(asduType, elementBytes)));
            position += elementLength;
        }

        return objects;
    }

    private Iec104InformationValue parseInformationValue(Iec104AsduType asduType, byte[] elementBytes) {
        switch (asduType) {
            case M_SP_NA_1:
                return parseSinglePointValue(asduType, elementBytes, false);
            case M_SP_TB_1:
                return parseSinglePointValue(asduType, elementBytes, true);
            case M_DP_NA_1:
                return parseDoublePointValue(asduType, elementBytes, false);
            case M_DP_TB_1:
                return parseDoublePointValue(asduType, elementBytes, true);
            case M_ST_NA_1:
                return parseStepPositionValue(asduType, elementBytes, false);
            case M_ST_TB_1:
                return parseStepPositionValue(asduType, elementBytes, true);
            case M_BO_NA_1:
                return parseBitstringValue(asduType, elementBytes, false);
            case M_BO_TB_1:
                return parseBitstringValue(asduType, elementBytes, true);
            case M_ME_NA_1:
                return parseNormalizedMeasuredValue(asduType, elementBytes, false);
            case M_ME_ND_1:
                return parseNormalizedMeasuredValueWithoutQuality(asduType, elementBytes);
            case M_ME_TD_1:
                return parseNormalizedMeasuredValue(asduType, elementBytes, true);
            case M_ME_NB_1:
                return parseScaledMeasuredValue(asduType, elementBytes, false);
            case M_ME_TE_1:
                return parseScaledMeasuredValue(asduType, elementBytes, true);
            case M_ME_NC_1:
                return parseShortFloatMeasuredValue(asduType, elementBytes, false);
            case M_ME_TF_1:
                return parseShortFloatMeasuredValue(asduType, elementBytes, true);
            case M_IT_NA_1:
                return parseIntegratedTotalsValue(asduType, elementBytes, false);
            case M_IT_TB_1:
                return parseIntegratedTotalsValue(asduType, elementBytes, true);
            case M_PS_NA_1:
                return parsePackedSinglePointValue(asduType, elementBytes);
            case C_SC_NA_1:
                return parseSingleCommandValue(asduType, elementBytes, false);
            case C_SC_TA_1:
                return parseSingleCommandValue(asduType, elementBytes, true);
            case C_DC_NA_1:
                return parseDoubleCommandValue(asduType, elementBytes, false);
            case C_DC_TA_1:
                return parseDoubleCommandValue(asduType, elementBytes, true);
            case C_RC_NA_1:
                return parseRegulatingStepCommandValue(asduType, elementBytes, false);
            case C_RC_TA_1:
                return parseRegulatingStepCommandValue(asduType, elementBytes, true);
            case C_SE_NA_1:
                return parseNormalizedSetPointCommandValue(asduType, elementBytes, false);
            case C_SE_TA_1:
                return parseNormalizedSetPointCommandValue(asduType, elementBytes, true);
            case C_SE_NB_1:
                return parseScaledSetPointCommandValue(asduType, elementBytes, false);
            case C_SE_TB_1:
                return parseScaledSetPointCommandValue(asduType, elementBytes, true);
            case C_SE_NC_1:
                return parseShortFloatSetPointCommandValue(asduType, elementBytes, false);
            case C_SE_TC_1:
                return parseShortFloatSetPointCommandValue(asduType, elementBytes, true);
            case C_BO_NA_1:
                return parseBitstringCommandValue(asduType, elementBytes, false);
            case C_BO_TA_1:
                return parseBitstringCommandValue(asduType, elementBytes, true);
            case C_IC_NA_1:
                return parseInterrogationCommandValue(asduType, elementBytes);
            case C_CI_NA_1:
                return parseCounterInterrogationCommandValue(asduType, elementBytes);
            case C_RD_NA_1:
                return parseReadCommandValue(asduType);
            case C_CS_NA_1:
                return parseClockSynchronizationCommandValue(asduType, elementBytes);
            case C_RP_NA_1:
                return parseResetProcessCommandValue(asduType, elementBytes);
            case C_CD_NA_1:
                return parseDelayAcquisitionCommandValue(asduType, elementBytes);
            default:
                return null;
        }
    }

    private Iec104SinglePointValue parseSinglePointValue(Iec104AsduType asduType, byte[] elementBytes,
                                                        boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 8 : 1)) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec104SinglePointValue(asduType, (rawValue & 0x01) != 0,
                Iec104QualityDescriptor.status(rawValue), parseTimeTag(elementBytes, timeTagged, 1));
    }

    private Iec104DoublePointValue parseDoublePointValue(Iec104AsduType asduType, byte[] elementBytes,
                                                        boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 8 : 1)) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec104DoublePointValue(asduType, Iec104DoublePointState.fromCode(rawValue),
                Iec104QualityDescriptor.status(rawValue), parseTimeTag(elementBytes, timeTagged, 1));
    }

    private Iec104StepPositionValue parseStepPositionValue(Iec104AsduType asduType, byte[] elementBytes,
                                                           boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 9 : 2)) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec104StepPositionValue(asduType, readSigned7(rawValue), (rawValue & 0x80) != 0,
                Iec104QualityDescriptor.status(ByteArrayUtil.unsignedByte(elementBytes[1])),
                parseTimeTag(elementBytes, timeTagged, 2));
    }

    private Iec104BitstringValue parseBitstringValue(Iec104AsduType asduType, byte[] elementBytes,
                                                     boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 12 : 5)) {
            return null;
        }
        return new Iec104BitstringValue(asduType, readSignedLittleEndian32(elementBytes, 0),
                Iec104QualityDescriptor.measurement(ByteArrayUtil.unsignedByte(elementBytes[4])),
                parseTimeTag(elementBytes, timeTagged, 5));
    }

    private Iec104MeasuredValue parseNormalizedMeasuredValue(Iec104AsduType asduType, byte[] elementBytes,
                                                            boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 10 : 3)) {
            return null;
        }
        short rawValue = readSignedLittleEndian16(elementBytes, 0);
        return Iec104MeasuredValue.normalized(asduType, rawValue,
                Iec104QualityDescriptor.measurement(ByteArrayUtil.unsignedByte(elementBytes[2])),
                parseTimeTag(elementBytes, timeTagged, 3));
    }

    private Iec104MeasuredValue parseNormalizedMeasuredValueWithoutQuality(Iec104AsduType asduType,
                                                                           byte[] elementBytes) {
        if (elementBytes.length < 2) {
            return null;
        }
        return Iec104MeasuredValue.normalized(asduType, readSignedLittleEndian16(elementBytes, 0), null, null);
    }

    private Iec104MeasuredValue parseScaledMeasuredValue(Iec104AsduType asduType, byte[] elementBytes,
                                                        boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 10 : 3)) {
            return null;
        }
        short rawValue = readSignedLittleEndian16(elementBytes, 0);
        return Iec104MeasuredValue.scaled(asduType, rawValue,
                Iec104QualityDescriptor.measurement(ByteArrayUtil.unsignedByte(elementBytes[2])),
                parseTimeTag(elementBytes, timeTagged, 3));
    }

    private Iec104MeasuredValue parseShortFloatMeasuredValue(Iec104AsduType asduType, byte[] elementBytes,
                                                            boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 12 : 5)) {
            return null;
        }
        int rawBits = readUnsignedLittleEndian(elementBytes, 0, 4);
        return Iec104MeasuredValue.shortFloat(asduType, Float.intBitsToFloat(rawBits), rawBits,
                Iec104QualityDescriptor.measurement(ByteArrayUtil.unsignedByte(elementBytes[4])),
                parseTimeTag(elementBytes, timeTagged, 5));
    }

    private Iec104IntegratedTotalsValue parseIntegratedTotalsValue(Iec104AsduType asduType, byte[] elementBytes,
                                                                   boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 12 : 5)) {
            return null;
        }
        int rawQualifier = ByteArrayUtil.unsignedByte(elementBytes[4]);
        return new Iec104IntegratedTotalsValue(asduType, readSignedLittleEndian32(elementBytes, 0),
                rawQualifier & 0x1F, (rawQualifier & 0x20) != 0, (rawQualifier & 0x40) != 0,
                (rawQualifier & 0x80) != 0, parseTimeTag(elementBytes, timeTagged, 5));
    }

    private Iec104PackedSinglePointValue parsePackedSinglePointValue(Iec104AsduType asduType,
                                                                     byte[] elementBytes) {
        if (elementBytes.length < 5) {
            return null;
        }
        return new Iec104PackedSinglePointValue(asduType, readUnsignedLittleEndian(elementBytes, 0, 2),
                readUnsignedLittleEndian(elementBytes, 2, 2),
                Iec104QualityDescriptor.status(ByteArrayUtil.unsignedByte(elementBytes[4])));
    }

    private Iec104SingleCommandValue parseSingleCommandValue(Iec104AsduType asduType, byte[] elementBytes,
                                                             boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 8 : 1)) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec104SingleCommandValue(asduType, (rawValue & 0x01) != 0,
                Iec104CommandQualifier.command(rawValue), parseTimeTag(elementBytes, timeTagged, 1));
    }

    private Iec104DoubleCommandValue parseDoubleCommandValue(Iec104AsduType asduType, byte[] elementBytes,
                                                             boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 8 : 1)) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec104DoubleCommandValue(asduType, Iec104DoubleCommandState.fromCode(rawValue),
                Iec104CommandQualifier.command(rawValue), parseTimeTag(elementBytes, timeTagged, 1));
    }

    private Iec104RegulatingStepCommandValue parseRegulatingStepCommandValue(Iec104AsduType asduType,
                                                                             byte[] elementBytes,
                                                                             boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 8 : 1)) {
            return null;
        }
        int rawValue = ByteArrayUtil.unsignedByte(elementBytes[0]);
        return new Iec104RegulatingStepCommandValue(asduType,
                Iec104RegulatingStepCommandState.fromCode(rawValue), Iec104CommandQualifier.command(rawValue),
                parseTimeTag(elementBytes, timeTagged, 1));
    }

    private Iec104SetPointCommandValue parseNormalizedSetPointCommandValue(Iec104AsduType asduType,
                                                                           byte[] elementBytes,
                                                                           boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 10 : 3)) {
            return null;
        }
        short rawValue = readSignedLittleEndian16(elementBytes, 0);
        return Iec104SetPointCommandValue.normalized(asduType, rawValue,
                Iec104CommandQualifier.setPoint(ByteArrayUtil.unsignedByte(elementBytes[2])),
                parseTimeTag(elementBytes, timeTagged, 3));
    }

    private Iec104SetPointCommandValue parseScaledSetPointCommandValue(Iec104AsduType asduType,
                                                                       byte[] elementBytes,
                                                                       boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 10 : 3)) {
            return null;
        }
        short rawValue = readSignedLittleEndian16(elementBytes, 0);
        return Iec104SetPointCommandValue.scaled(asduType, rawValue,
                Iec104CommandQualifier.setPoint(ByteArrayUtil.unsignedByte(elementBytes[2])),
                parseTimeTag(elementBytes, timeTagged, 3));
    }

    private Iec104SetPointCommandValue parseShortFloatSetPointCommandValue(Iec104AsduType asduType,
                                                                           byte[] elementBytes,
                                                                           boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 12 : 5)) {
            return null;
        }
        int rawBits = readUnsignedLittleEndian(elementBytes, 0, 4);
        return Iec104SetPointCommandValue.shortFloat(asduType, Float.intBitsToFloat(rawBits), rawBits,
                Iec104CommandQualifier.setPoint(ByteArrayUtil.unsignedByte(elementBytes[4])),
                parseTimeTag(elementBytes, timeTagged, 5));
    }

    private Iec104BitstringCommandValue parseBitstringCommandValue(Iec104AsduType asduType, byte[] elementBytes,
                                                                   boolean timeTagged) {
        if (elementBytes.length < (timeTagged ? 12 : 5)) {
            return null;
        }
        return new Iec104BitstringCommandValue(asduType, readSignedLittleEndian32(elementBytes, 0),
                Iec104CommandQualifier.setPoint(ByteArrayUtil.unsignedByte(elementBytes[4])),
                parseTimeTag(elementBytes, timeTagged, 5));
    }

    private Iec104InterrogationCommandValue parseInterrogationCommandValue(Iec104AsduType asduType,
                                                                           byte[] elementBytes) {
        if (elementBytes.length < 1) {
            return null;
        }
        return new Iec104InterrogationCommandValue(asduType, ByteArrayUtil.unsignedByte(elementBytes[0]));
    }

    private Iec104CounterInterrogationCommandValue parseCounterInterrogationCommandValue(Iec104AsduType asduType,
                                                                                         byte[] elementBytes) {
        if (elementBytes.length < 1) {
            return null;
        }
        return new Iec104CounterInterrogationCommandValue(asduType, ByteArrayUtil.unsignedByte(elementBytes[0]));
    }

    private Iec104ReadCommandValue parseReadCommandValue(Iec104AsduType asduType) {
        return new Iec104ReadCommandValue(asduType);
    }

    private Iec104ClockSynchronizationCommandValue parseClockSynchronizationCommandValue(Iec104AsduType asduType,
                                                                                         byte[] elementBytes) {
        if (elementBytes.length < 7) {
            return null;
        }
        return new Iec104ClockSynchronizationCommandValue(asduType, Iec104Cp56Time2a.parse(elementBytes, 0));
    }

    private Iec104ResetProcessCommandValue parseResetProcessCommandValue(Iec104AsduType asduType,
                                                                         byte[] elementBytes) {
        if (elementBytes.length < 1) {
            return null;
        }
        return new Iec104ResetProcessCommandValue(asduType, ByteArrayUtil.unsignedByte(elementBytes[0]));
    }

    private Iec104DelayAcquisitionCommandValue parseDelayAcquisitionCommandValue(Iec104AsduType asduType,
                                                                                 byte[] elementBytes) {
        if (elementBytes.length < 2) {
            return null;
        }
        return new Iec104DelayAcquisitionCommandValue(asduType, readUnsignedLittleEndian(elementBytes, 0, 2));
    }

    private Iec104Cp56Time2a parseTimeTag(byte[] elementBytes, boolean timeTagged, int offset) {
        if (!timeTagged) {
            return null;
        }
        return Iec104Cp56Time2a.parse(elementBytes, offset);
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

    private int readSignedLittleEndian32(byte[] bytes, int offset) {
        return readUnsignedLittleEndian(bytes, offset, 4);
    }

    private int readSigned7(int rawValue) {
        int value = rawValue & 0x7F;
        if (value >= 64) {
            return value - 128;
        }
        return value;
    }

    private Iec104FrameType parseUFormatType(int control) {
        switch (control) {
            case 0x07:
                return Iec104FrameType.STARTDT_ACT;
            case 0x0B:
                return Iec104FrameType.STARTDT_CON;
            case 0x13:
                return Iec104FrameType.STOPDT_ACT;
            case 0x23:
                return Iec104FrameType.STOPDT_CON;
            case 0x43:
                return Iec104FrameType.TESTFR_ACT;
            case 0x83:
                return Iec104FrameType.TESTFR_CON;
            default:
                return Iec104FrameType.UNKNOWN_U_FORMAT;
        }
    }
}
