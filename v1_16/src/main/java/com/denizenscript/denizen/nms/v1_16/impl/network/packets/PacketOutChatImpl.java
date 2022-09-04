package com.denizenscript.denizen.nms.v1_16.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutChat;
import com.denizenscript.denizen.nms.v1_16.Handler;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.server.v1_16_R3.ChatMessageType;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutChat;

import java.lang.reflect.Field;
import java.util.Map;

public class PacketOutChatImpl implements PacketOutChat {

    private String message;
    private String rawJson;
    private ChatMessageType position;

    public PacketOutChatImpl(PacketPlayOutChat internal) {
        try {
            IChatBaseComponent baseComponent = (IChatBaseComponent) MESSAGE.get(internal);
            if (baseComponent != null) {
                message = FormattedTextHelper.stringify(Handler.componentToSpigot(baseComponent), ChatColor.WHITE);
                rawJson = IChatBaseComponent.ChatSerializer.a(baseComponent);
            }
            else {
                if (internal.components != null) {
                    message = FormattedTextHelper.stringify(internal.components, ChatColor.WHITE);
                    rawJson = ComponentSerializer.toString(internal.components);
                }
            }
            position = (ChatMessageType) POSITION.get(internal);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    @Override
    public boolean isSystem() {
        return position == ChatMessageType.SYSTEM;
    }

    @Override
    public boolean isActionbar() {
        return position == ChatMessageType.GAME_INFO;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getRawJson() {
        return rawJson;
    }

    private static final Field MESSAGE, POSITION;

    static {
        Map<String, Field> fields = ReflectionHelper.getFields(PacketPlayOutChat.class);
        MESSAGE = fields.get("a");
        POSITION = fields.get("b");
    }
}
