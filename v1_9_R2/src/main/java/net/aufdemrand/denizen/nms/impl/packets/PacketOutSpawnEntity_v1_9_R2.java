package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_9_R2.Packet;

import java.util.UUID;

public class PacketOutSpawnEntity_v1_9_R2 implements PacketOutSpawnEntity {

    private Packet internal;
    private int entityId;
    private UUID entityUuid;

    public PacketOutSpawnEntity_v1_9_R2(Packet internal) {
        this.internal = internal;
        Integer integer = ReflectionHelper.getFieldValue(internal.getClass(), "a", internal);
        entityId = integer != null ? integer : -1;
        entityUuid = ReflectionHelper.getFieldValue(internal.getClass(), "b", internal);
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
