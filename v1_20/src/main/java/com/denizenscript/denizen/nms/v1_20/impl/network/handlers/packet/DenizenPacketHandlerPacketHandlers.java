package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.events.player.PlayerReceivesMessageScriptEvent;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.packets.PacketOutChatImpl;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;

public class DenizenPacketHandlerPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSystemChatPacket.class, DenizenPacketHandlerPacketHandlers::processPacketHandlerForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundPlayerChatPacket.class, DenizenPacketHandlerPacketHandlers::processPacketHandlerForPacket);
    }

    public static Packet<ClientGamePacketListener> processPacketHandlerForPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (DenizenPacketHandler.instance.shouldInterceptChatPacket()) {
            PacketOutChatImpl packetHelper = null;
            boolean isActionbar = false;
            if (packet instanceof ClientboundSystemChatPacket chatPacket) {
                isActionbar = chatPacket.overlay();
                packetHelper = new PacketOutChatImpl(chatPacket);
                if (packetHelper.rawJson == null) { // Makes no sense but this can be null in weird edge cases
                    return packet;
                }
            }
            else if (packet instanceof ClientboundPlayerChatPacket playerChatPacket) {
                packetHelper = new PacketOutChatImpl(playerChatPacket);
            }
            if (packetHelper != null) {
                PlayerReceivesMessageScriptEvent result = DenizenPacketHandler.instance.sendPacket(networkManager.player.getBukkitEntity(), packetHelper);
                if (result != null) {
                    if (result.cancelled) {
                        return null;
                    }
                    if (result.modified) {
                        return new ClientboundSystemChatPacket(result.altMessageDetermination, isActionbar);
                    }
                }
            }
        }
        return packet;
    }
}
