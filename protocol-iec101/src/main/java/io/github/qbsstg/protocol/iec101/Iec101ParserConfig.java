package io.github.qbsstg.protocol.iec101;

public final class Iec101ParserConfig {

    private final Iec101TransmissionMode transmissionMode;
    private final int linkAddressLength;
    private final int causeOfTransmissionLength;
    private final int commonAddressLength;
    private final int informationObjectAddressLength;

    private Iec101ParserConfig(Builder builder) {
        this.transmissionMode = builder.transmissionMode;
        this.linkAddressLength = validateLength("linkAddressLength", builder.linkAddressLength, 1, 2);
        this.causeOfTransmissionLength = validateLength("causeOfTransmissionLength",
                builder.causeOfTransmissionLength, 1, 2);
        this.commonAddressLength = validateLength("commonAddressLength", builder.commonAddressLength, 1, 2);
        this.informationObjectAddressLength = validateLength("informationObjectAddressLength",
                builder.informationObjectAddressLength, 1, 3);
    }

    public static Iec101ParserConfig defaultUnbalanced() {
        return builder().transmissionMode(Iec101TransmissionMode.UNBALANCED).build();
    }

    public static Iec101ParserConfig defaultBalanced() {
        return builder().transmissionMode(Iec101TransmissionMode.BALANCED).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public Iec101TransmissionMode getTransmissionMode() {
        return transmissionMode;
    }

    public int getLinkAddressLength() {
        return linkAddressLength;
    }

    public int getCauseOfTransmissionLength() {
        return causeOfTransmissionLength;
    }

    public int getCommonAddressLength() {
        return commonAddressLength;
    }

    public int getInformationObjectAddressLength() {
        return informationObjectAddressLength;
    }

    private static int validateLength(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
        return value;
    }

    public static final class Builder {

        private Iec101TransmissionMode transmissionMode = Iec101TransmissionMode.UNBALANCED;
        private int linkAddressLength = 1;
        private int causeOfTransmissionLength = 2;
        private int commonAddressLength = 2;
        private int informationObjectAddressLength = 3;

        private Builder() {
        }

        public Builder transmissionMode(Iec101TransmissionMode transmissionMode) {
            if (transmissionMode == null) {
                throw new IllegalArgumentException("transmissionMode must not be null");
            }
            this.transmissionMode = transmissionMode;
            return this;
        }

        public Builder linkAddressLength(int linkAddressLength) {
            this.linkAddressLength = linkAddressLength;
            return this;
        }

        public Builder causeOfTransmissionLength(int causeOfTransmissionLength) {
            this.causeOfTransmissionLength = causeOfTransmissionLength;
            return this;
        }

        public Builder commonAddressLength(int commonAddressLength) {
            this.commonAddressLength = commonAddressLength;
            return this;
        }

        public Builder informationObjectAddressLength(int informationObjectAddressLength) {
            this.informationObjectAddressLength = informationObjectAddressLength;
            return this;
        }

        public Iec101ParserConfig build() {
            return new Iec101ParserConfig(this);
        }
    }
}
