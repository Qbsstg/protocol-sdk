package io.github.qbsstg.protocol.iec104;

public final class Iec104QualityDescriptor {

    private final int rawValue;
    private final boolean invalid;
    private final boolean notTopical;
    private final boolean substituted;
    private final boolean blocked;
    private final boolean overflow;

    private Iec104QualityDescriptor(int rawValue, boolean overflow) {
        this.rawValue = rawValue & 0xFF;
        this.invalid = (this.rawValue & 0x80) != 0;
        this.notTopical = (this.rawValue & 0x40) != 0;
        this.substituted = (this.rawValue & 0x20) != 0;
        this.blocked = (this.rawValue & 0x10) != 0;
        this.overflow = overflow;
    }

    public static Iec104QualityDescriptor status(int rawValue) {
        return new Iec104QualityDescriptor(rawValue, false);
    }

    public static Iec104QualityDescriptor measurement(int rawValue) {
        return new Iec104QualityDescriptor(rawValue, (rawValue & 0x01) != 0);
    }

    public int getRawValue() {
        return rawValue;
    }

    public boolean isInvalid() {
        return invalid;
    }

    public boolean isNotTopical() {
        return notTopical;
    }

    public boolean isSubstituted() {
        return substituted;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public boolean isOverflow() {
        return overflow;
    }
}
