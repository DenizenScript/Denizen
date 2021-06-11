package com.denizenscript.denizen.nms;

public enum NMSVersion {

    NOT_SUPPORTED,
    v1_14,
    v1_15,
    v1_16,
    v1_17;

    public boolean isAtLeast(NMSVersion version) {
        return ordinal() >= version.ordinal();
    }

    public boolean isAtMost(NMSVersion version) {
        return ordinal() <= version.ordinal();
    }
}
