package io.github.qbsstg.protocol.iec103;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Iec103Asdu {

    private final int typeId;
    private final Iec103AsduType type;
    private final Iec103VariableStructureQualifier variableStructureQualifier;
    private final int causeCode;
    private final Iec103CauseOfTransmission causeOfTransmission;
    private final int commonAddress;
    private final byte[] rawBytes;
    private final List<Iec103InformationElement> informationElements;

    public Iec103Asdu(int typeId, Iec103VariableStructureQualifier variableStructureQualifier,
                      int causeCode, int commonAddress, byte[] rawBytes,
                      List<Iec103InformationElement> informationElements) {
        this.typeId = typeId;
        this.type = Iec103AsduType.fromTypeId(typeId);
        this.variableStructureQualifier = variableStructureQualifier;
        this.causeCode = causeCode;
        this.causeOfTransmission = Iec103CauseOfTransmission.fromCode(causeCode);
        this.commonAddress = commonAddress;
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.informationElements = Collections.unmodifiableList(
                new ArrayList<Iec103InformationElement>(informationElements));
    }

    public int getTypeId() {
        return typeId;
    }

    public Iec103AsduType getType() {
        return type;
    }

    public Iec103VariableStructureQualifier getVariableStructureQualifier() {
        return variableStructureQualifier;
    }

    public int getCauseCode() {
        return causeCode;
    }

    public Iec103CauseOfTransmission getCauseOfTransmission() {
        return causeOfTransmission;
    }

    public int getCommonAddress() {
        return commonAddress;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }

    public List<Iec103InformationElement> getInformationElements() {
        return informationElements;
    }
}
