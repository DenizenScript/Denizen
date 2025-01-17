package com.denizenscript.denizen.nms.interfaces.packets;

import java.util.function.Function;

public abstract class PacketOutChat {

    public abstract boolean isSystem();

    public abstract boolean isActionbar();

    public abstract String getMessage();

    public abstract String getRawJson();

    // TODO: once 1.20 is the minimum supported version, remove this
    public static Function<Object, String> convertComponentToJsonString;
}
