package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.utilities.entity.EntityAttachmentHelper;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.util.Vector;

import java.lang.reflect.Field;

public class AttachPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundMoveEntityPacket.class, AttachPacketHandlers::processAttachToForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundRotateHeadPacket.class, AttachPacketHandlers::processAttachToForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSetEntityMotionPacket.class, AttachPacketHandlers::processAttachToForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundTeleportEntityPacket.class, AttachPacketHandlers::processAttachToForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundRemoveEntitiesPacket.class, AttachPacketHandlers::processAttachToForPacket);
    }

    public static Field POS_X_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_xa, short.class);
    public static Field POS_Y_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_ya, short.class);
    public static Field POS_Z_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_za, short.class);
    public static Field YAW_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_yRot, byte.class);
    public static Field PITCH_PACKENT = ReflectionHelper.getFields(ClientboundMoveEntityPacket.class).get(ReflectionMappingsInfo.ClientboundMoveEntityPacket_xRot, byte.class);
    public static Field ENTITY_ID_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_id, int.class);
    public static Field POS_X_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_x, double.class);
    public static Field POS_Y_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_y, double.class);
    public static Field POS_Z_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_z, double.class);
    public static Field YAW_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_yRot, byte.class);
    public static Field PITCH_PACKTELENT = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_xRot, byte.class);
    public static Field ENTITY_ID_PACKVELENT = ReflectionHelper.getFields(ClientboundSetEntityMotionPacket.class).get(ReflectionMappingsInfo.ClientboundSetEntityMotionPacket_id, int.class);

    public static Vector VECTOR_ZERO = new Vector(0, 0, 0);

    public static void tryProcessMovePacketForAttach(DenizenNetworkManagerImpl networkManager, ClientboundMoveEntityPacket packet, Entity e) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(networkManager.player.getUUID());
                if (attMap.attached.isValid() && att != null) {
                    ClientboundMoveEntityPacket pNew;
                    int newId = att.attached.getBukkitEntity().getEntityId();
                    if (packet instanceof ClientboundMoveEntityPacket.Pos) {
                        pNew = new ClientboundMoveEntityPacket.Pos(newId, packet.getXa(), packet.getYa(), packet.getZa(), packet.isOnGround());
                    }
                    else if (packet instanceof ClientboundMoveEntityPacket.Rot) {
                        pNew = new ClientboundMoveEntityPacket.Rot(newId, packet.getyRot(), packet.getxRot(), packet.isOnGround());
                    }
                    else if (packet instanceof ClientboundMoveEntityPacket.PosRot) {
                        pNew = new ClientboundMoveEntityPacket.PosRot(newId, packet.getXa(), packet.getYa(), packet.getZa(), packet.getyRot(), packet.getxRot(), packet.isOnGround());
                    }
                    else {
                        if (CoreConfiguration.debugVerbose) {
                            Debug.echoError("Impossible move-entity packet class: " + packet.getClass().getCanonicalName());
                        }
                        return;
                    }
                    if (att.positionalOffset != null) {
                        boolean isRotate = packet instanceof ClientboundMoveEntityPacket.PosRot || packet instanceof ClientboundMoveEntityPacket.Rot;
                        byte yaw, pitch;
                        if (att.noRotate) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            yaw = EntityAttachmentHelper.compressAngle(attachedEntity.getYRot());
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.getXRot());
                        }
                        else if (isRotate) {
                            yaw = packet.getyRot();
                            pitch = packet.getxRot();
                        }
                        else {
                            yaw = EntityAttachmentHelper.compressAngle(e.getYRot());
                            pitch = EntityAttachmentHelper.compressAngle(e.getXRot());
                        }
                        if (att.noPitch) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.getXRot());
                        }
                        byte newYaw = yaw;
                        if (isRotate) {
                            newYaw = EntityAttachmentHelper.adaptedCompressedAngle(newYaw, att.positionalOffset.getYaw());
                            pitch = EntityAttachmentHelper.adaptedCompressedAngle(pitch, att.positionalOffset.getPitch());
                        }
                        Vector goalPosition = att.fixedForOffset(new Vector(e.getX(), e.getY(), e.getZ()), e.getYRot(), e.getXRot());
                        Vector oldPos = att.visiblePositions.get(networkManager.player.getUUID());
                        boolean forceTele = false;
                        if (oldPos == null) {
                            oldPos = att.attached.getLocation().toVector();
                            forceTele = true;
                        }
                        Vector moveNeeded = goalPosition.clone().subtract(oldPos);
                        att.visiblePositions.put(networkManager.player.getUUID(), goalPosition.clone());
                        int offX = (int) (moveNeeded.getX() * (32 * 128));
                        int offY = (int) (moveNeeded.getY() * (32 * 128));
                        int offZ = (int) (moveNeeded.getZ() * (32 * 128));
                        if ((isRotate && att.offsetRelative) || forceTele || offX < Short.MIN_VALUE || offX > Short.MAX_VALUE
                                || offY < Short.MIN_VALUE || offY > Short.MAX_VALUE
                                || offZ < Short.MIN_VALUE || offZ > Short.MAX_VALUE) {
                            ClientboundTeleportEntityPacket newTeleportPacket = new ClientboundTeleportEntityPacket(e);
                            ENTITY_ID_PACKTELENT.setInt(newTeleportPacket, att.attached.getBukkitEntity().getEntityId());
                            POS_X_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getX());
                            POS_Y_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getY());
                            POS_Z_PACKTELENT.setDouble(newTeleportPacket, goalPosition.getZ());
                            YAW_PACKTELENT.setByte(newTeleportPacket, newYaw);
                            PITCH_PACKTELENT.setByte(newTeleportPacket, pitch);
                            if (NMSHandler.debugPackets) {
                                DenizenNetworkManagerImpl.doPacketOutput("Attach Move-Tele Packet: " + newTeleportPacket.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + networkManager.player.getScoreboardName() + " with original yaw " + yaw + " adapted to " + newYaw);
                            }
                            networkManager.oldManager.send(newTeleportPacket);
                        }
                        else {
                            POS_X_PACKENT.setShort(pNew, (short) Mth.clamp(offX, Short.MIN_VALUE, Short.MAX_VALUE));
                            POS_Y_PACKENT.setShort(pNew, (short) Mth.clamp(offY, Short.MIN_VALUE, Short.MAX_VALUE));
                            POS_Z_PACKENT.setShort(pNew, (short) Mth.clamp(offZ, Short.MIN_VALUE, Short.MAX_VALUE));
                            if (isRotate) {
                                YAW_PACKENT.setByte(pNew, yaw);
                                PITCH_PACKENT.setByte(pNew, pitch);
                            }
                            if (NMSHandler.debugPackets) {
                                DenizenNetworkManagerImpl.doPacketOutput("Attach Move Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + networkManager.player.getScoreboardName() + " with original yaw " + yaw + " adapted to " + newYaw);
                            }
                            networkManager.oldManager.send(pNew);
                        }
                    }
                    else {
                        if (NMSHandler.debugPackets) {
                            DenizenNetworkManagerImpl.doPacketOutput("Attach Replica-Move Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + networkManager.player.getScoreboardName());
                        }
                        networkManager.oldManager.send(pNew);
                    }
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessMovePacketForAttach(networkManager, packet, ent);
            }
        }
    }

    public static void tryProcessRotateHeadPacketForAttach(DenizenNetworkManagerImpl networkManager, ClientboundRotateHeadPacket packet, Entity e) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(networkManager.player.getUUID());
                if (attMap.attached.isValid() && att != null) {
                    byte yaw = packet.getYHeadRot();
                    Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                    if (att.positionalOffset != null) {
                        if (att.noRotate) {
                            yaw = EntityAttachmentHelper.compressAngle(attachedEntity.getYRot());
                        }
                        yaw = EntityAttachmentHelper.adaptedCompressedAngle(yaw, att.positionalOffset.getYaw());
                    }
                    ClientboundRotateHeadPacket pNew = new ClientboundRotateHeadPacket(attachedEntity, yaw);
                    if (NMSHandler.debugPackets) {
                        DenizenNetworkManagerImpl.doPacketOutput("Head Rotation Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + networkManager.player.getScoreboardName());
                    }
                    networkManager.oldManager.send(pNew);
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessRotateHeadPacketForAttach(networkManager, packet, ent);
            }
        }
    }

    public static void tryProcessVelocityPacketForAttach(DenizenNetworkManagerImpl networkManager, ClientboundSetEntityMotionPacket packet, Entity e) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(networkManager.player.getUUID());
                if (attMap.attached.isValid() && att != null) {
                    ClientboundSetEntityMotionPacket pNew = ClientboundSetEntityMotionPacket.STREAM_CODEC.decode(DenizenNetworkManagerImpl.copyPacket(packet, ClientboundSetEntityMotionPacket.STREAM_CODEC));
                    ENTITY_ID_PACKVELENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                    if (NMSHandler.debugPackets) {
                        DenizenNetworkManagerImpl.doPacketOutput("Attach Velocity Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID() + " sent to " + networkManager.player.getScoreboardName());
                    }
                    networkManager.oldManager.send(pNew);
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessVelocityPacketForAttach(networkManager, packet, ent);
            }
        }
    }

    public static void tryProcessTeleportPacketForAttach(DenizenNetworkManagerImpl networkManager, ClientboundTeleportEntityPacket packet, Entity e, Vector relative) throws IllegalAccessException {
        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
        if (attList != null) {
            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(networkManager.player.getUUID());
                if (attMap.attached.isValid() && att != null) {
                    ClientboundTeleportEntityPacket pNew = ClientboundTeleportEntityPacket.STREAM_CODEC.decode(DenizenNetworkManagerImpl.copyPacket(packet, ClientboundTeleportEntityPacket.STREAM_CODEC));
                    ENTITY_ID_PACKTELENT.setInt(pNew, att.attached.getBukkitEntity().getEntityId());
                    Vector resultPos = new Vector(POS_X_PACKTELENT.getDouble(pNew), POS_Y_PACKTELENT.getDouble(pNew), POS_Z_PACKTELENT.getDouble(pNew)).add(relative);
                    if (att.positionalOffset != null) {
                        resultPos = att.fixedForOffset(resultPos, e.getYRot(), e.getXRot());
                        byte yaw, pitch;
                        if (att.noRotate) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            yaw = EntityAttachmentHelper.compressAngle(attachedEntity.getYRot());
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.getXRot());
                        }
                        else {
                            yaw = packet.getyRot();
                            pitch = packet.getxRot();
                        }
                        if (att.noPitch) {
                            Entity attachedEntity = ((CraftEntity) att.attached.getBukkitEntity()).getHandle();
                            pitch = EntityAttachmentHelper.compressAngle(attachedEntity.getXRot());
                        }
                        byte newYaw = EntityAttachmentHelper.adaptedCompressedAngle(yaw, att.positionalOffset.getYaw());
                        pitch = EntityAttachmentHelper.adaptedCompressedAngle(pitch, att.positionalOffset.getPitch());
                        POS_X_PACKTELENT.setDouble(pNew, resultPos.getX());
                        POS_Y_PACKTELENT.setDouble(pNew, resultPos.getY());
                        POS_Z_PACKTELENT.setDouble(pNew, resultPos.getZ());
                        YAW_PACKTELENT.setByte(pNew, newYaw);
                        PITCH_PACKTELENT.setByte(pNew, pitch);
                        if (NMSHandler.debugPackets) {
                            DenizenNetworkManagerImpl.doPacketOutput("Attach Teleport Packet: " + pNew.getClass().getCanonicalName() + " for " + att.attached.getUUID()
                                    + " sent to " + networkManager.player.getScoreboardName() + " with raw yaw " + yaw + " adapted to " + newYaw);
                        }
                    }
                    att.visiblePositions.put(networkManager.player.getUUID(), resultPos.clone());
                    networkManager.oldManager.send(pNew);
                }
            }
        }
        if (e.passengers != null && !e.passengers.isEmpty()) {
            for (Entity ent : e.passengers) {
                tryProcessTeleportPacketForAttach(networkManager, packet, ent, new Vector(ent.getX() - e.getX(), ent.getY() - e.getY(), ent.getZ() - e.getZ()));
            }
        }
    }

    public static Packet<ClientGamePacketListener> processAttachToForPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (EntityAttachmentHelper.toEntityToData.isEmpty()) {
            return packet;
        }
        try {
            if (packet instanceof ClientboundMoveEntityPacket moveEntityPacket) {
                Entity e = moveEntityPacket.getEntity(networkManager.player.level());
                if (e == null) {
                    return packet;
                }
                if (!e.isPassenger()) {
                    tryProcessMovePacketForAttach(networkManager, moveEntityPacket, e);
                }
                return EntityAttachmentHelper.denyOriginalPacketSend(networkManager.player.getUUID(), e.getUUID()) ? null : packet;
            }
            else if (packet instanceof ClientboundRotateHeadPacket rotateHeadPacket) {
                Entity e = rotateHeadPacket.getEntity(networkManager.player.level());
                if (e == null) {
                    return packet;
                }
                tryProcessRotateHeadPacketForAttach(networkManager, rotateHeadPacket, e);
                return EntityAttachmentHelper.denyOriginalPacketSend(networkManager.player.getUUID(), e.getUUID()) ? null : packet;
            }
            else if (packet instanceof ClientboundSetEntityMotionPacket setEntityMotionPacket) {
                int ider = setEntityMotionPacket.getId();
                Entity e = networkManager.player.level().getEntity(ider);
                if (e == null) {
                    return packet;
                }
                tryProcessVelocityPacketForAttach(networkManager, setEntityMotionPacket, e);
                return EntityAttachmentHelper.denyOriginalPacketSend(networkManager.player.getUUID(), e.getUUID()) ? null : packet;
            }
            else if (packet instanceof ClientboundTeleportEntityPacket teleportEntityPacket) {
                int ider = teleportEntityPacket.getId();
                Entity e = networkManager.player.level().getEntity(ider);
                if (e == null) {
                    return packet;
                }
                tryProcessTeleportPacketForAttach(networkManager, teleportEntityPacket, e, VECTOR_ZERO);
                return EntityAttachmentHelper.denyOriginalPacketSend(networkManager.player.getUUID(), e.getUUID()) ? null : packet;
            }
            else if (packet instanceof ClientboundRemoveEntitiesPacket removeEntitiesPacket) {
                for (int id : removeEntitiesPacket.getEntityIds()) {
                    Entity e = networkManager.player.level().getEntity(id);
                    if (e != null) {
                        EntityAttachmentHelper.EntityAttachedToMap attList = EntityAttachmentHelper.toEntityToData.get(e.getUUID());
                        if (attList != null) {
                            for (EntityAttachmentHelper.PlayerAttachMap attMap : attList.attachedToMap.values()) {
                                EntityAttachmentHelper.AttachmentData att = attMap.getAttachment(networkManager.player.getUUID());
                                if (attMap.attached.isValid() && att != null) {
                                    att.visiblePositions.remove(networkManager.player.getUUID());
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return packet;
    }
}
