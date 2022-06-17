package com.denizenscript.denizen.nms.interfaces.packets;

public interface PacketOutChat {

    boolean isSystem();

    boolean isActionbar();

    String getMessage();

    String getRawJson();

    void setRawJson(String rawJson);
}
