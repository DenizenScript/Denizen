package com.denizenscript.denizen.nms;

public enum NMSVersion {

    NOT_SUPPORTED,
    v1_16,
    v1_17,
    v1_18;

    public boolean isAtLeast(NMSVersion version) {
        return ordinal() >= version.ordinal();
    }

    public boolean isAtMost(NMSVersion version) {
        return ordinal() <= version.ordinal();
    }
}
