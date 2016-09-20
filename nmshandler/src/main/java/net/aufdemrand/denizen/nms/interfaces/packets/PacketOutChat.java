package net.aufdemrand.denizen.nms.interfaces.packets;

public interface PacketOutChat {

    int getPosition();

    String getMessage();

    String getRawJson();

    void setPosition(int position);

    void setMessage(String message);

    void setRawJson(String rawJson);
}
