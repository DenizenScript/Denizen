package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.v1_20.impl.blocks.BlockLightImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;

public class BlockLightPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundLightUpdatePacket.class, BlockLightPacketHandlers::processLightUpdatePacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundBlockUpdatePacket.class, BlockLightPacketHandlers::processBlockUpdatePacket);
    }

    public static void processLightUpdatePacket(DenizenNetworkManagerImpl networkManager, ClientboundLightUpdatePacket lightUpdatePacket) {
        if (!BlockLight.lightsByChunk.isEmpty()) {
            BlockLightImpl.checkIfLightsBrokenByPacket(lightUpdatePacket, networkManager.player.level());
        }
    }

    public static void processBlockUpdatePacket(DenizenNetworkManagerImpl networkManager, ClientboundBlockUpdatePacket blockUpdatePacket) {
        if (!BlockLight.lightsByChunk.isEmpty()) {
            BlockLightImpl.checkIfLightsBrokenByPacket(blockUpdatePacket, networkManager.player.level());
        }
    }
}
