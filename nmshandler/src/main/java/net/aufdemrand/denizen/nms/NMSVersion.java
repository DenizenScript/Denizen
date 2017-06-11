package net.aufdemrand.denizen.nms;

public enum NMSVersion {

    NOT_SUPPORTED,
    v1_8_R3,
    v1_9_R2,
    v1_10_R1,
    v1_11_R1,
    v1_12_R1;

    public boolean isAtLeast(NMSVersion version) {
        return ordinal() >= version.ordinal();
    }
    public boolean isAtMost(NMSVersion version) {
        return ordinal() <= version.ordinal();
    }
}
