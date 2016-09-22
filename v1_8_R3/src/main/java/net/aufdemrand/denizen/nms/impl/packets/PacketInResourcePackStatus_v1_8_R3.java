package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketInResourcePackStatus;
import net.minecraft.server.v1_8_R3.PacketPlayInResourcePackStatus;

public class PacketInResourcePackStatus_v1_8_R3 implements PacketInResourcePackStatus {

    private PacketPlayInResourcePackStatus internal;

    public PacketInResourcePackStatus_v1_8_R3(PacketPlayInResourcePackStatus internal) {
        this.internal = internal;
    }

    @Override
    public String getStatus() {
        return internal.b.name();
    }
}
