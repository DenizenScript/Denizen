package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.abstracts.BlockLight;
import com.denizenscript.denizen.nms.v1_20.impl.blocks.BlockLightImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;

public class BlockLightPacketHandlers {

    public static void registerHandlers() {

    }

    public void processBlockLightForPacket(Packet<?> packet) {
        if (BlockLight.lightsByChunk.isEmpty()) {
            return;
        }
        if (packet instanceof ClientboundLightUpdatePacket) {
            BlockLightImpl.checkIfLightsBrokenByPacket((ClientboundLightUpdatePacket) packet, player.level());
        }
        else if (packet instanceof ClientboundBlockUpdatePacket) {
            BlockLightImpl.checkIfLightsBrokenByPacket((ClientboundBlockUpdatePacket) packet, player.level());
        }
    }
}
