package com.denizenscript.denizen.nms.v1_20.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketInResourcePackStatus;
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket;

public class PacketInResourcePackStatusImpl implements PacketInResourcePackStatus {

    private ServerboundResourcePackPacket internal;

    public PacketInResourcePackStatusImpl(ServerboundResourcePackPacket internal) {
        this.internal = internal;
    }

    @Override
    public String getStatus() {
        return internal.action().name();
    }
}
