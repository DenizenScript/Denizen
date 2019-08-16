package com.denizenscript.denizen.nms;

public enum NMSVersion {

    NOT_SUPPORTED,
    v1_12,
    v1_13,
    v1_14;

    public boolean isAtLeast(NMSVersion version) {
        return ordinal() >= version.ordinal();
    }

    public boolean isAtMost(NMSVersion version) {
        return ordinal() <= version.ordinal();
    }
}
