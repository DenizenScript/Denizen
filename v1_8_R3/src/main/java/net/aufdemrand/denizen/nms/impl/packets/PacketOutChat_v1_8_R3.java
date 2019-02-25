package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutChat;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.aufdemrand.denizencore.utilities.debugging.dB;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_8_R3.ChatComponentText;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutChat;

import java.lang.reflect.Field;
import java.util.Map;

public class PacketOutChat_v1_8_R3 implements PacketOutChat {

    private PacketPlayOutChat internal;
    private String message;
    private String rawJson;
    private boolean bungee;
    private int position;

    public PacketOutChat_v1_8_R3(PacketPlayOutChat internal) {
        this.internal = internal;
        try {
            IChatBaseComponent baseComponent = (IChatBaseComponent) MESSAGE.get(internal);
            if (baseComponent != null) {
                message = baseComponent.c();
                rawJson = IChatBaseComponent.ChatSerializer.a(baseComponent);
            }
            else {
                message = BaseComponent.toPlainText(internal.components);
                rawJson = ComponentSerializer.toString(internal.components);
                bungee = true;
            }
            position = POSITION.getInt(internal);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getRawJson() {
        return rawJson;
    }

    @Override
    public void setPosition(int position) {
        try {
            POSITION.set(internal, position);
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    @Override
    public void setMessage(String message) {
        try {
            if (!bungee) {
                MESSAGE.set(internal, new ChatComponentText(message));
            }
            else {
                internal.components = new BaseComponent[]{new TextComponent(message)};
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    @Override
    public void setRawJson(String rawJson) {
        try {
            if (!bungee) {
                MESSAGE.set(internal, IChatBaseComponent.ChatSerializer.a(rawJson));
            }
            else {
                internal.components = ComponentSerializer.parse(rawJson);
            }
        }
        catch (Exception e) {
            dB.echoError(e);
        }
    }

    private static final Field MESSAGE, POSITION;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutChat.class);
        MESSAGE = fields.get("a");
        POSITION = fields.get("b");
    }
}
