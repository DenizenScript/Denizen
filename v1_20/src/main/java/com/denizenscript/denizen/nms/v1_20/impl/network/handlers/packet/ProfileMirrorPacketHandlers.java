package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.impl.ProfileEditorImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

public class ProfileMirrorPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundPlayerInfoUpdatePacket.class, ProfileMirrorPacketHandlers::processMirrorForPacket);
    }

    public static Packet<ClientGamePacketListener> processMirrorForPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (packet instanceof ClientboundPlayerInfoUpdatePacket playerInfoUpdatePacket) {
            if (!ProfileEditorImpl.handleAlteredProfiles(playerInfoUpdatePacket, networkManager)) {
                return null;
            }
        }
        return packet;
    }
}
