package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.player.DisguiseCommand;
import com.denizenscript.denizen.utilities.entity.EntityAttachmentHelper;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class DisguisePacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSetEntityDataPacket.class, DisguisePacketHandlers::processDisguiseForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundUpdateAttributesPacket.class, DisguisePacketHandlers::processDisguiseForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundAddPlayerPacket.class, DisguisePacketHandlers::processDisguiseForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundAddEntityPacket.class, DisguisePacketHandlers::processDisguiseForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundTeleportEntityPacket.class, DisguisePacketHandlers::processDisguiseForPacket);
        DenizenNetworkManagerImpl.registerPacketHandlerForChildren(ClientboundMoveEntityPacket.class, DisguisePacketHandlers::processDisguiseForPacket);
    }

    public static Field ENTITY_ID_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_id, int.class);
    public static Field POS_X_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_x, double.class);
    public static Field POS_Y_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_y, double.class);
    public static Field POS_Z_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_z, double.class);
    public static Field YAW_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_yRot, byte.class);
    public static Field PITCH_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_xRot, byte.class);

    private static boolean antiDuplicate = false;

    public static Packet<ClientGamePacketListener> processDisguiseForPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (DisguiseCommand.disguises.isEmpty() || antiDuplicate) {
            return packet;
        }
        try {
            int entityID = -1;
            if (packet instanceof ClientboundSetEntityDataPacket entityDataPacket) {
                entityID = entityDataPacket.id();
            }
            if (packet instanceof ClientboundUpdateAttributesPacket updateAttributesPacket) {
                entityID = updateAttributesPacket.getEntityId();
            }
            if (packet instanceof ClientboundAddPlayerPacket addPlayerPacket) {
                entityID = addPlayerPacket.getEntityId();
            }
            else if (packet instanceof ClientboundAddEntityPacket addEntityPacket) {
                entityID = addEntityPacket.getId();
            }
            else if (packet instanceof ClientboundTeleportEntityPacket teleportEntityPacket) {
                entityID = teleportEntityPacket.getId();
            }
            else if (packet instanceof ClientboundMoveEntityPacket moveEntityPacket) {
                Entity e = moveEntityPacket.getEntity(networkManager.player.level());
                if (e != null) {
                    entityID = e.getId();
                }
            }
            if (entityID == -1) {
                return packet;
            }
            Entity entity = networkManager.player.level().getEntity(entityID);
            if (entity == null) {
                return packet;
            }
            HashMap<UUID, DisguiseCommand.TrackedDisguise> playerMap = DisguiseCommand.disguises.get(entity.getUUID());
            if (playerMap == null) {
                return packet;
            }
            DisguiseCommand.TrackedDisguise disguise = playerMap.get(networkManager.player.getUUID());
            if (disguise == null) {
                disguise = playerMap.get(null);
                if (disguise == null) {
                    return packet;
                }
            }
            if (!disguise.isActive) {
                return packet;
            }
            if (NMSHandler.debugPackets) {
                DenizenNetworkManagerImpl.doPacketOutput("DISGUISED packet " + packet.getClass().getName() + " for entity " + entityID + " to player " + networkManager.player.getScoreboardName());
            }
            if (packet instanceof ClientboundSetEntityDataPacket metadataPacket) {
                if (entityID == networkManager.player.getId()) {
                    if (!disguise.shouldFake) {
                        return packet;
                    }
                    List<SynchedEntityData.DataValue<?>> data = metadataPacket.packedItems();
                    for (SynchedEntityData.DataValue<?> dataValue : data) {
                        if (dataValue.id() == 0) { // Entity flags
                            data = new ArrayList<>(data);
                            data.remove(dataValue);
                            byte flags = (byte) dataValue.value();
                            flags |= 0x20; // Invisible flag
                            data.add(new SynchedEntityData.DataValue(dataValue.id(), dataValue.serializer(), flags));
                            ClientboundSetEntityDataPacket altPacket = new ClientboundSetEntityDataPacket(metadataPacket.id(), data);
                            ClientboundSetEntityDataPacket updatedPacket = EntityMetadataPacketHandlers.getModifiedMetadataFor(networkManager, altPacket);
                            return updatedPacket == null ? altPacket : updatedPacket;
                        }
                    }
                }
                else {
                    List<SynchedEntityData.DataValue<?>> data = ((CraftEntity) disguise.toOthers.entity.entity).getHandle().getEntityData().getNonDefaultValues();
                    return data != null ? new ClientboundSetEntityDataPacket(entityID, data) : null;
                }
                return packet;
            }
            else if (packet instanceof ClientboundUpdateAttributesPacket) {
                FakeEntity fake = entityID == networkManager.player.getId() ? disguise.fakeToSelf : disguise.toOthers;
                if (fake == null) {
                    return packet;
                }
                if (fake.entity.entity instanceof LivingEntity) {
                    return packet;
                }
                return null; // Non-living don't have attributes
            }
            else if (packet instanceof ClientboundTeleportEntityPacket) {
                if (disguise.as.getBukkitEntityType() == EntityType.ENDER_DRAGON) {
                    ClientboundTeleportEntityPacket pOld = (ClientboundTeleportEntityPacket) packet;
                    ClientboundTeleportEntityPacket pNew = new ClientboundTeleportEntityPacket(entity);
                    ENTITY_ID_PACKTELENT.setInt(pNew, pOld.getId());
                    POS_X_PACKTELENT.setDouble(pNew, pOld.getX());
                    POS_Y_PACKTELENT.setDouble(pNew, pOld.getY());
                    POS_Z_PACKTELENT.setDouble(pNew, pOld.getZ());
                    YAW_PACKTELENT.setByte(pNew, EntityAttachmentHelper.adaptedCompressedAngle(pOld.getyRot(), 180));
                    PITCH_PACKTELENT.setByte(pNew, pOld.getxRot());
                    return pNew;
                }
            }
            else if (packet instanceof ClientboundMoveEntityPacket) {
                if (disguise.as.getBukkitEntityType() == EntityType.ENDER_DRAGON) {
                    ClientboundMoveEntityPacket pOld = (ClientboundMoveEntityPacket) packet;
                    ClientboundMoveEntityPacket pNew = null;
                    if (packet instanceof ClientboundMoveEntityPacket.Rot) {
                        pNew = new ClientboundMoveEntityPacket.Rot(entityID, EntityAttachmentHelper.adaptedCompressedAngle(pOld.getyRot(), 180), pOld.getxRot(), pOld.isOnGround());
                    }
                    else if (packet instanceof ClientboundMoveEntityPacket.PosRot) {
                        pNew = new ClientboundMoveEntityPacket.PosRot(entityID, pOld.getXa(), pOld.getYa(), pOld.getZa(), EntityAttachmentHelper.adaptedCompressedAngle(pOld.getyRot(), 180), pOld.getxRot(), pOld.isOnGround());
                    }
                    if (pNew != null) {
                        return pNew;
                    }
                    return packet;
                }
            }
            antiDuplicate = true;
            disguise.sendTo(List.of(new PlayerTag(networkManager.player.getUUID())));
            antiDuplicate = false;
            return null;
        }
        catch (Throwable ex) {
            antiDuplicate = false;
            Debug.echoError(ex);
        }
        return packet;
    }
}
