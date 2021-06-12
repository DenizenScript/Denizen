package com.denizenscript.denizen.nms.v1_16.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.server.v1_16_R3.*;

public class PacketOutSpawnEntityImpl implements PacketOutSpawnEntity {

    private int entityId;

    public PacketOutSpawnEntityImpl(EntityPlayer player, Packet internal) {
        Integer integer = ReflectionHelper.getFieldValue(internal.getClass(), "a", internal);
        entityId = integer != null ? integer : -1;
    }

    @Override
    public int getEntityId() {
        return entityId;
    }
}
