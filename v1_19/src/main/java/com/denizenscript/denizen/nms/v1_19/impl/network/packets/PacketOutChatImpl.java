package com.denizenscript.denizen.nms.v1_19.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutChat;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.chat.ComponentSerializer;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class PacketOutChatImpl implements PacketOutChat {

    public static ChatType ACTIONBAR_TYPE = BuiltinRegistries.CHAT_TYPE.get(ChatType.GAME_INFO);

    public ClientboundPlayerChatPacket playerPacket;
    public ClientboundSystemChatPacket systemPacket;
    public ChatType position;
    public String message;
    public String rawJson;

    public PacketOutChatImpl(ClientboundSystemChatPacket internal) {
        systemPacket = internal;
        rawJson = internal.content();
        message = FormattedTextHelper.stringify(ComponentSerializer.parse(rawJson), ChatColor.BLACK);
        position = BuiltinRegistries.CHAT_TYPE.getHolder(internal.typeId()).get().value();
    }

    public PacketOutChatImpl(ClientboundPlayerChatPacket internal) {
        playerPacket = internal;
        rawJson = ComponentSerializer.toString(internal.signedContent());
        message = FormattedTextHelper.stringify(ComponentSerializer.parse(rawJson), ChatColor.BLACK);
        position = BuiltinRegistries.CHAT_TYPE.getHolder(internal.typeId()).get().value();
    }


    @Override
    public boolean isSystem() {
        return systemPacket != null;
    }

    @Override
    public boolean isActionbar() {
        return position == ACTIONBAR_TYPE;
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
    public void setRawJson(String rawJson) {
        this.rawJson = rawJson;
    }
}
