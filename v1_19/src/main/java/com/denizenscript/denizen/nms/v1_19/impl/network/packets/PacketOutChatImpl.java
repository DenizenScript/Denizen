package com.denizenscript.denizen.nms.v1_19.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutChat;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class PacketOutChatImpl implements PacketOutChat {

    public ClientboundPlayerChatPacket playerPacket;
    public ClientboundSystemChatPacket systemPacket;
    public String message;
    public String rawJson;
    public boolean isOverlayActionbar;

    public PacketOutChatImpl(ClientboundSystemChatPacket internal) {
        systemPacket = internal;
        rawJson = internal.content();
        message = FormattedTextHelper.stringify(ComponentSerializer.parse(rawJson));
        isOverlayActionbar = internal.overlay();
    }

    public PacketOutChatImpl(ClientboundPlayerChatPacket internal) {
        playerPacket = internal;
        rawJson = ComponentSerializer.toString(internal.message().signedContent());
        message = FormattedTextHelper.stringify(ComponentSerializer.parse(rawJson));
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
