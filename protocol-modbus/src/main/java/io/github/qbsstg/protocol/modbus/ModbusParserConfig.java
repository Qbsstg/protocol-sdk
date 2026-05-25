package io.github.qbsstg.protocol.modbus;

public final class ModbusParserConfig {

    public static final int DEFAULT_MAX_ADU_LENGTH = 260;

    private final boolean validateProtocolId;
    private final int maxAduLength;
    private final boolean strictDatagramLength;

    public ModbusParserConfig() {
        this(true, DEFAULT_MAX_ADU_LENGTH, true);
    }

    public ModbusParserConfig(boolean validateProtocolId, int maxAduLength, boolean strictDatagramLength) {
        if (maxAduLength < 8) {
            throw new IllegalArgumentException("maxAduLength must be at least 8");
        }
        this.validateProtocolId = validateProtocolId;
        this.maxAduLength = maxAduLength;
        this.strictDatagramLength = strictDatagramLength;
    }

    public static ModbusParserConfig defaults() {
        return new ModbusParserConfig();
    }

    public boolean isValidateProtocolId() {
        return validateProtocolId;
    }

    public int getMaxAduLength() {
        return maxAduLength;
    }

    public boolean isStrictDatagramLength() {
        return strictDatagramLength;
    }
}
