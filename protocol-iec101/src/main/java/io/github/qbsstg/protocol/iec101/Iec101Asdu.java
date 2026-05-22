package io.github.qbsstg.protocol.iec101;

import io.github.qbsstg.protocol.core.util.ByteArrayUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Iec101Asdu {

    private final int typeId;
    private final Iec101AsduType type;
    private final Iec101VariableStructureQualifier variableStructureQualifier;
    private final int causeCode;
    private final boolean test;
    private final boolean negativeConfirm;
    private final int originatorAddress;
    private final int commonAddress;
    private final byte[] rawBytes;
    private final List<Iec101InformationObject> informationObjects;

    public Iec101Asdu(int typeId, Iec101VariableStructureQualifier variableStructureQualifier,
                      int causeCode, boolean test, boolean negativeConfirm, int originatorAddress,
                      int commonAddress, byte[] rawBytes, List<Iec101InformationObject> informationObjects) {
        this.typeId = typeId;
        this.type = Iec101AsduType.fromTypeId(typeId);
        this.variableStructureQualifier = variableStructureQualifier;
        this.causeCode = causeCode;
        this.test = test;
        this.negativeConfirm = negativeConfirm;
        this.originatorAddress = originatorAddress;
        this.commonAddress = commonAddress;
        this.rawBytes = ByteArrayUtil.copyOf(rawBytes);
        this.informationObjects = Collections.unmodifiableList(
                new ArrayList<Iec101InformationObject>(informationObjects));
    }

    public int getTypeId() {
        return typeId;
    }

    public Iec101AsduType getType() {
        return type;
    }

    public Iec101VariableStructureQualifier getVariableStructureQualifier() {
        return variableStructureQualifier;
    }

    public int getCauseCode() {
        return causeCode;
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

    public List<Iec101InformationObject> getInformationObjects() {
        return informationObjects;
    }
}
