package io.github.qbsstg.protocol.iec104;

public interface Iec104InformationValue {

    Iec104AsduType getAsduType();

    Iec104Cp56Time2a getTimeTag();
}
