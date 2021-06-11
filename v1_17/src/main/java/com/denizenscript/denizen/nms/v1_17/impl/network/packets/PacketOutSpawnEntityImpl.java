package com.denizenscript.denizen.nms.v1_17.impl.network.packets;

import com.denizenscript.denizen.nms.interfaces.packets.PacketOutSpawnEntity;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.PacketPlayOutSpawnEntityExperienceOrb;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

public class PacketOutSpawnEntityImpl implements PacketOutSpawnEntity {

    private Packet internal;
    private int entityId;
    private UUID entityUuid;

    public PacketOutSpawnEntityImpl(ServerPlayer player, Packet internal) {
        this.internal = internal;
        Integer integer = ReflectionHelper.getFieldValue(internal.getClass(), "a", internal);
        entityId = integer != null ? integer : -1;
        if (!(internal instanceof PacketPlayOutSpawnEntityExperienceOrb)) {
            entityUuid = ReflectionHelper.getFieldValue(internal.getClass(), "b", internal);
        }
        else {
            Entity entity = player.level.getEntity(entityId);
            entityUuid = entity != null ? entity.getUUID() : null;
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
