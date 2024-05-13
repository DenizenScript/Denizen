package com.denizenscript.denizen.nms;

public enum NMSVersion {

    NOT_SUPPORTED("not_supported"),
    v1_17("1.17"),
    v1_18("1.18"),
    v1_19("1.19"),
    v1_20("1.20");

    final String minecraftVersion;

    NMSVersion(String minecraftVersion) {
        this.minecraftVersion = minecraftVersion;
    }

    public boolean isAtLeast(NMSVersion version) {
        return ordinal() >= version.ordinal();
    }

    public boolean isAtMost(NMSVersion version) {
        return ordinal() <= version.ordinal();
    }
}
