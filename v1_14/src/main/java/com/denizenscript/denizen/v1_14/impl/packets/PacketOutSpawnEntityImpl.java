package com.denizenscript.denizen.v1_14.impl.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import com.denizenscript.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_14_R1.*;

import java.util.UUID;

public class PacketOutSpawnEntityImpl implements PacketOutSpawnEntity {

    private Packet internal;
    private int entityId;
    private UUID entityUuid;

    public PacketOutSpawnEntityImpl(EntityPlayer player, Packet internal) {
        this.internal = internal;
        Integer integer = ReflectionHelper.getFieldValue(internal.getClass(), "a", internal);
        entityId = integer != null ? integer : -1;
        if (!(internal instanceof PacketPlayOutSpawnEntityExperienceOrb)) {
            entityUuid = ReflectionHelper.getFieldValue(internal.getClass(), "b", internal);
        }
        else {
            Entity entity = player.world.getEntity(entityId);
            entityUuid = entity != null ? entity.getUniqueID() : null;
        }
    }

    @Override
    public int getEntityId() {
        return entityId;
    }

    @Override
    public UUID getEntityUuid() {
        return entityUuid;
    }
}
