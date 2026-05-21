package io.github.qbsstg.protocol.iec104;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Iec104Asdu {

    private final int typeId;
    private final Iec104AsduType type;
    private final Iec104VariableStructureQualifier variableStructureQualifier;
    private final int causeCode;
    private final Iec104CauseOfTransmission causeOfTransmission;
    private final boolean test;
    private final boolean negativeConfirm;
    private final int originatorAddress;
    private final int commonAddress;
    private final byte[] rawBytes;
    private final List<Iec104InformationObject> informationObjects;

    public Iec104Asdu(int typeId, Iec104VariableStructureQualifier variableStructureQualifier,
                      int causeCode, boolean test, boolean negativeConfirm, int originatorAddress,
                      int commonAddress, byte[] rawBytes, List<Iec104InformationObject> informationObjects) {
        this.typeId = typeId;
        this.type = Iec104AsduType.fromTypeId(typeId);
        this.variableStructureQualifier = variableStructureQualifier;
        this.causeCode = causeCode;
        this.causeOfTransmission = Iec104CauseOfTransmission.fromCode(causeCode);
        this.test = test;
        this.negativeConfirm = negativeConfirm;
        this.originatorAddress = originatorAddress;
        this.commonAddress = commonAddress;
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.informationObjects = Collections.unmodifiableList(new ArrayList<Iec104InformationObject>(informationObjects));
    }

    public int getTypeId() {
        return typeId;
    }

    public Iec104AsduType getType() {
        return type;
    }

    public Iec104VariableStructureQualifier getVariableStructureQualifier() {
        return variableStructureQualifier;
    }

    public int getCauseCode() {
        return causeCode;
    }

    public Iec104CauseOfTransmission getCauseOfTransmission() {
        return causeOfTransmission;
    }

    public boolean isTest() {
        return test;
    }

    public boolean isNegativeConfirm() {
        return negativeConfirm;
    }

    public int getOriginatorAddress() {
        return originatorAddress;
    }

    public int getCommonAddress() {
        return commonAddress;
    }

    public byte[] getRawBytes() {
        return ByteArrayUtil.copyOf(rawBytes);
    }

    public List<Iec104InformationObject> getInformationObjects() {
        return informationObjects;
    }
}
