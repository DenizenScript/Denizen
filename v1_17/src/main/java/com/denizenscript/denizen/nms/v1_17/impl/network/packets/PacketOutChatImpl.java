package com.denizenscript.denizen.nms.v1_17.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutChat;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizen.nms.v1_17.Handler;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundChatPacket;

import java.lang.reflect.Field;

public class PacketOutChatImpl implements PacketOutChat {

    private ClientboundChatPacket internal;
    private String message;
    private String rawJson;
    private boolean bungee;
    private ChatType position;

    public PacketOutChatImpl(ClientboundChatPacket internal) {
        this.internal = internal;
        try {
            Component baseComponent = (Component) MESSAGE.get(internal);
            if (baseComponent != null) {
                message = FormattedTextHelper.stringify(Handler.componentToSpigot(baseComponent));
                rawJson = Component.Serializer.toJson(baseComponent);
            }
            else {
                if (internal.components != null) {
                    message = FormattedTextHelper.stringify(internal.components);
                    rawJson = ComponentSerializer.toString(internal.components);
                }
                bungee = true;
            }
            position = (ChatType) POSITION.get(internal);
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    @Override
    public boolean isSystem() {
        return position == ChatType.SYSTEM;
    }

    @Override
    public boolean isActionbar() {
        return position == ChatType.GAME_INFO;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getRawJson() {
        return rawJson;
    }

    public void setRawJson(String rawJson) {
        try {
            if (!bungee) {
                MESSAGE.set(internal, Component.Serializer.fromJson(rawJson));
            }
            else {
                internal.components = ComponentSerializer.parse(rawJson);
            }
        }
        catch (Exception e) {
            Debug.echoError(e);
        }
    }

    private static final Field MESSAGE, POSITION;

    static {
        MESSAGE = ReflectionHelper.getFields(ClientboundChatPacket.class).getFirstOfType(Component.class);
        POSITION = ReflectionHelper.getFields(ClientboundChatPacket.class).getFirstOfType(ChatType.class);
    }
}
