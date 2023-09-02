package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.v1_20.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;

public class BlockLightPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundLightUpdatePacket.class, BlockLightPacketHandlers::processBlockLightForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundBlockUpdatePacket.class, BlockLightPacketHandlers::processBlockLightForPacket);
    }

    public static void processBlockLightForPacket(DenizenNetworkManagerImpl networkManager, Packet<?> packet) {
        if (BlockLight.lightsByChunk.isEmpty()) {
            return;
        }
        if (packet instanceof ClientboundLightUpdatePacket) {
            BlockLightImpl.checkIfLightsBrokenByPacket((ClientboundLightUpdatePacket) packet, networkManager.player.level());
        }
        else if (packet instanceof ClientboundBlockUpdatePacket) {
            BlockLightImpl.checkIfLightsBrokenByPacket((ClientboundBlockUpdatePacket) packet, networkManager.player.level());
        }
    }
}
