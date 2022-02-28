package com.denizenscript.denizen.nms.interfaces.packets;

public interface PacketOutChat {

    boolean isSystem();

    boolean isActionbar();

    int getPosition();

    String getMessage();

    String getRawJson();

    void setPosition(int position);

    void setMessage(String message);

    void setRawJson(String rawJson);
}
