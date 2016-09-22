package net.aufdemrand.denizen.nms.impl.packets;

import net.aufdemrand.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import net.aufdemrand.denizen.nms.util.ReflectionHelper;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.WorldServer;

import java.util.UUID;

public class PacketOutSpawnEntity_v1_8_R3 implements PacketOutSpawnEntity {

    private Packet internal;
    private int entityId;
    private UUID entityUuid;

    public PacketOutSpawnEntity_v1_8_R3(EntityPlayer player, Packet internal) {
        this.internal = internal;
        Integer integer = ReflectionHelper.getFieldValue(internal.getClass(), "a", internal);
        entityId = integer != null ? integer : -1;
        if (internal instanceof PacketPlayOutNamedEntitySpawn) {
            entityUuid = ReflectionHelper.getFieldValue(internal.getClass(), "b", internal);
        }
        else {
            EntityTracker tracker = ((WorldServer) player.world).tracker;
            EntityTrackerEntry entry = tracker.trackedEntities.get(entityId);
            entityUuid = entry != null ? entry.tracker.getUniqueID() : null;
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
