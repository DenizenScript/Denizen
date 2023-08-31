package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.impl.ProfileEditorImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;

public class ProfileMirrorPacketHandlers {

    public static void registerHandlers() {

    }

    public boolean processMirrorForPacket(Packet<?> packet) {
        if (packet instanceof ClientboundPlayerInfoUpdatePacket playerInfoUpdatePacket) {
            if (!ProfileEditorImpl.handleAlteredProfiles(playerInfoUpdatePacket, this)) {
                return true;
            }
        }
        return false;
    }
}
