package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.events.player.PlayerReceivesMessageScriptEvent;
import com.denizenscript.denizen.nms.v1_20.impl.network.packets.PacketOutChatImpl;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class DenizenPacketHandlerPacketHandlers {

    public static void registerHandlers() {

    }

    public boolean processPacketHandlerForPacket(Packet<?> packet, PacketSendListener genericfuturelistener) {
        if (DenizenPacketHandler.instance.shouldInterceptChatPacket()) {
            PacketOutChatImpl packetHelper = null;
            boolean isActionbar = false;
            if (packet instanceof ClientboundSystemChatPacket chatPacket) {
                isActionbar = chatPacket.overlay();
                packetHelper = new PacketOutChatImpl(chatPacket);
                if (packetHelper.rawJson == null) { // Makes no sense but this can be null in weird edge cases
                    return false;
                }
            }
            else if (packet instanceof ClientboundPlayerChatPacket playerChatPacket) {
                packetHelper = new PacketOutChatImpl(playerChatPacket);
            }
            if (packetHelper != null) {
                PlayerReceivesMessageScriptEvent result = DenizenPacketHandler.instance.sendPacket(player.getBukkitEntity(), packetHelper);
                if (result != null) {
                    if (result.cancelled) {
                        return true;
                    }
                    if (result.modified) {
                        oldManager.send(new ClientboundSystemChatPacket(result.altMessageDetermination, isActionbar), genericfuturelistener);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
