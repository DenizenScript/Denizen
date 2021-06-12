package com.denizenscript.denizen.nms.v1_17.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSpawnEntity;

public class PacketOutSpawnEntityImpl implements PacketOutSpawnEntity {

    private int entityId;

    public PacketOutSpawnEntityImpl(int eid) {
        this.entityId = eid;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }
}
