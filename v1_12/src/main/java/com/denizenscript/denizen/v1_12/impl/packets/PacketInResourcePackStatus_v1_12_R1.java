package com.denizenscript.denizen.v1_12.impl.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketInResourcePackStatus;
import net.minecraft.server.v1_12_R1.PacketPlayInResourcePackStatus;

public class PacketInResourcePackStatus_v1_12_R1 implements PacketInResourcePackStatus {

    private PacketPlayInResourcePackStatus internal;

    public PacketInResourcePackStatus_v1_12_R1(PacketPlayInResourcePackStatus internal) {
        this.internal = internal;
    }

    @Override
    public String getStatus() {
        return internal.status.name();
    }
}
