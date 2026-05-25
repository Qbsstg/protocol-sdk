package io.github.qbsstg.protocol.iec103;

public final class Iec103ParserConfig {

    private final int linkAddressLength;
    private final int commonAddressLength;
    private final int maxFrameLength;
    private final boolean strictChecksum;
    private final boolean preserveUnknownTypePayload;

    private Iec103ParserConfig(Builder builder) {
        this.linkAddressLength = validateLength("linkAddressLength", builder.linkAddressLength, 1, 2);
        this.commonAddressLength = validateLength("commonAddressLength", builder.commonAddressLength, 1, 2);
        if (builder.maxFrameLength < 6 || builder.maxFrameLength > 261) {
            throw new IllegalArgumentException("maxFrameLength must be between 6 and 261");
        }
        this.maxFrameLength = builder.maxFrameLength;
        this.strictChecksum = builder.strictChecksum;
        this.preserveUnknownTypePayload = builder.preserveUnknownTypePayload;
    }

    public static Iec103ParserConfig defaultUnbalanced() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getLinkAddressLength() {
        return linkAddressLength;
    }

    public int getCommonAddressLength() {
        return commonAddressLength;
    }

    public int getMaxFrameLength() {
        return maxFrameLength;
    }

    public boolean isStrictChecksum() {
        return strictChecksum;
    }

    public boolean isPreserveUnknownTypePayload() {
        return preserveUnknownTypePayload;
    }

    private static int validateLength(String name, int value, int min, int max) {
        if (value < min || value > max) {
            throw new IllegalArgumentException(name + " must be between " + min + " and " + max);
        }
        return value;
    }

    public static final class Builder {

        private int linkAddressLength = 1;
        private int commonAddressLength = 1;
        private int maxFrameLength = 261;
        private boolean strictChecksum = true;
        private boolean preserveUnknownTypePayload = true;

        private Builder() {
        }

        public Builder linkAddressLength(int linkAddressLength) {
            this.linkAddressLength = linkAddressLength;
            return this;
        }

        public Builder commonAddressLength(int commonAddressLength) {
            this.commonAddressLength = commonAddressLength;
            return this;
        }

        public Builder maxFrameLength(int maxFrameLength) {
            this.maxFrameLength = maxFrameLength;
            return this;
        }

        public Builder strictChecksum(boolean strictChecksum) {
            this.strictChecksum = strictChecksum;
            return this;
        }

        public Builder preserveUnknownTypePayload(boolean preserveUnknownTypePayload) {
            this.preserveUnknownTypePayload = preserveUnknownTypePayload;
            return this;
        }

        public Iec103ParserConfig build() {
            return new Iec103ParserConfig(this);
        }
    }
}
