package com.denizenscript.denizen.nms.v1_19.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutChat;
import com.denizenscript.denizen.utilities.PaperAPITools;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

import java.lang.reflect.Field;

public class PacketOutChatImpl extends PacketOutChat {

    public ClientboundPlayerChatPacket playerPacket;
    public ClientboundSystemChatPacket systemPacket;
    public String message;
    public String rawJson;
    public boolean isOverlayActionbar;

    public static Field paperTextField;

    public PacketOutChatImpl(ClientboundSystemChatPacket internal) {
        systemPacket = internal;
        rawJson = internal.content();
        if (rawJson == null && convertComponentToJsonString != null) {
            try {
                if (paperTextField == null) {
                    paperTextField = ReflectionHelper.getFields(ClientboundSystemChatPacket.class).get("adventure$content");
                }
                if (paperTextField != null) {
                    rawJson = convertComponentToJsonString.apply(paperTextField.get(internal));
                }
            }
            catch (Throwable ex) {
                Debug.echoError(ex);
            }
        }
        message = PaperAPITools.instance.parseJsonToText(rawJson);
        isOverlayActionbar = internal.overlay();
    }

    public PacketOutChatImpl(ClientboundPlayerChatPacket internal) {
        playerPacket = internal;
        rawJson = ComponentSerializer.toString(internal.body().content());
        message = PaperAPITools.instance.parseJsonToText(rawJson);
    }

    @Override
    public boolean isSystem() {
        return systemPacket != null;
    }

    @Override
    public boolean isActionbar() {
        return isOverlayActionbar;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getRawJson() {
        return rawJson;
    }
}
