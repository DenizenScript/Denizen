package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_20.ReflectionMappingsInfo;
import com.denizenscript.denizen.nms.v1_20.helpers.PacketHelperImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.scripts.commands.player.DisguiseCommand;
import com.denizenscript.denizen.utilities.entity.EntityAttachmentHelper;
import com.denizenscript.denizen.utilities.entity.FakeEntity;
import com.denizenscript.denizencore.utilities.ReflectionHelper;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.bukkit.craftbukkit.v1_20_R4.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.ToIntFunction;

public class DisguisePacketHandlers {

    public static void registerHandlers() {
        registerPacketHandler(ClientboundSetEntityDataPacket.class, ClientboundSetEntityDataPacket::id, DisguisePacketHandlers::processEntityDataPacket);
        registerPacketHandler(ClientboundUpdateAttributesPacket.class, ClientboundUpdateAttributesPacket::getEntityId, DisguisePacketHandlers::processAttributesPacket);
        registerPacketHandler(ClientboundAddEntityPacket.class, ClientboundAddEntityPacket::getId, DisguisePacketHandlers::sendDisguiseForPacket);
        registerPacketHandler(ClientboundTeleportEntityPacket.class, ClientboundTeleportEntityPacket::getId, DisguisePacketHandlers::processTeleportPacket);
        registerPacketHandler(ClientboundMoveEntityPacket.Rot.class, ClientboundMoveEntityPacket::getEntity, DisguisePacketHandlers::processMoveEntityRotPacket);
        registerPacketHandler(ClientboundMoveEntityPacket.PosRot.class, ClientboundMoveEntityPacket::getEntity, DisguisePacketHandlers::processMoveEntityPosRotPacket);
    }

    public static final Field TELEPORT_PACKET_YAW = ReflectionHelper.getFields(ClientboundTeleportEntityPacket.class).get(ReflectionMappingsInfo.ClientboundTeleportEntityPacket_yRot, byte.class);

    private static boolean antiDuplicate = false;

    public static <T extends Packet<ClientGamePacketListener>> void registerPacketHandler(Class<T> packetType, ToIntFunction<T> idGetter, DisguisePacketHandler<T> handler) {
        registerPacketHandler(packetType, (packet, level) -> level.getEntity(idGetter.applyAsInt(packet)), handler);
    }

    public static <T extends Packet<ClientGamePacketListener>> void registerPacketHandler(Class<T> packetType, BiFunction<T, Level, Entity> entityGetter, DisguisePacketHandler<T> handler) {
        DenizenNetworkManagerImpl.registerPacketHandler(packetType, (networkManager, packet) -> {
            if (DisguiseCommand.disguises.isEmpty() || antiDuplicate) {
                return packet;
            }
            Entity entity = entityGetter.apply(packet, networkManager.player.level());
            if (entity == null) {
                return packet;
            }
            Map<UUID, DisguiseCommand.TrackedDisguise> playerMap = DisguiseCommand.disguises.get(entity.getUUID());
            if (playerMap == null) {
                return packet;
            }
            DisguiseCommand.TrackedDisguise disguise = playerMap.get(networkManager.player.getUUID());
            if (disguise == null) {
                disguise = playerMap.get(null);
            }
            if (disguise == null || !disguise.isActive) {
                return packet;
            }
            if (NMSHandler.debugPackets) {
                DenizenNetworkManagerImpl.doPacketOutput("DISGUISED packet " + packet.getClass().getName() + " for entity " + entity.getId() + " to player " + networkManager.player.getScoreboardName());
            }
            try {
                return handler.handle(networkManager, packet, disguise);
            }
            catch (Exception e) {
                antiDuplicate = false;
                throw e; // "pass it" to the generic exception handling
            }
        });
    }

    @FunctionalInterface
    public interface DisguisePacketHandler<T extends Packet<ClientGamePacketListener>> {

        T handle(DenizenNetworkManagerImpl networkManager, T packet, DisguiseCommand.TrackedDisguise disguise) throws Exception;
    }

    public static ClientboundSetEntityDataPacket processEntityDataPacket(DenizenNetworkManagerImpl networkManager, ClientboundSetEntityDataPacket entityDataPacket, DisguiseCommand.TrackedDisguise disguise) {
        if (entityDataPacket.id() == networkManager.player.getId()) {
            if (!disguise.shouldFake) {
                return entityDataPacket;
            }
            for (SynchedEntityData.DataValue<?> dataValue : entityDataPacket.packedItems()) {
                if (dataValue.id() == 0) { // Entity flags
                    List<SynchedEntityData.DataValue<?>> newData = new ArrayList<>(entityDataPacket.packedItems());
                    newData.remove(dataValue);
                    byte flags = (byte) dataValue.value();
                    flags |= 0x20; // Invisible flag
                    newData.add(PacketHelperImpl.createEntityData(PacketHelperImpl.ENTITY_DATA_ACCESSOR_FLAGS, flags));
                    return new ClientboundSetEntityDataPacket(entityDataPacket.id(), newData);
                }
            }
        }
        else {
            List<SynchedEntityData.DataValue<?>> data = ((CraftEntity) disguise.toOthers.entity.entity).getHandle().getEntityData().getNonDefaultValues();
            return data != null ? new ClientboundSetEntityDataPacket(entityDataPacket.id(), data) : null;
        }
        return entityDataPacket;
    }

    public static ClientboundUpdateAttributesPacket processAttributesPacket(DenizenNetworkManagerImpl networkManager, ClientboundUpdateAttributesPacket attributesPacket, DisguiseCommand.TrackedDisguise disguise) {
        FakeEntity fake = attributesPacket.getEntityId() == networkManager.player.getId() ? disguise.fakeToSelf : disguise.toOthers;
        return fake == null || fake.entity.entity instanceof LivingEntity ? attributesPacket : null; // Non-living entities don't have attributes
    }

    public static ClientboundTeleportEntityPacket processTeleportPacket(DenizenNetworkManagerImpl networkManager, ClientboundTeleportEntityPacket teleportEntityPacket, DisguiseCommand.TrackedDisguise disguise) throws IllegalAccessException {
        if (disguise.as.getBukkitEntityType() == EntityType.ENDER_DRAGON) {
            ClientboundTeleportEntityPacket pNew = ClientboundTeleportEntityPacket.STREAM_CODEC.decode(DenizenNetworkManagerImpl.copyPacket(teleportEntityPacket, ClientboundTeleportEntityPacket.STREAM_CODEC));
            TELEPORT_PACKET_YAW.setByte(pNew, EntityAttachmentHelper.adaptedCompressedAngle(teleportEntityPacket.getyRot(), 180));
            return pNew;
        }
        return sendDisguiseForPacket(networkManager, teleportEntityPacket, disguise);
    }


    public static ClientboundMoveEntityPacket.Rot processMoveEntityRotPacket(DenizenNetworkManagerImpl networkManager, ClientboundMoveEntityPacket.Rot rotPacket, DisguiseCommand.TrackedDisguise disguise) {
        if (disguise.as.getBukkitEntityType() == EntityType.ENDER_DRAGON) {
            return new ClientboundMoveEntityPacket.Rot(disguise.entity.getBukkitEntity().getEntityId(), EntityAttachmentHelper.adaptedCompressedAngle(rotPacket.getyRot(), 180), rotPacket.getxRot(), rotPacket.isOnGround());
        }
        return sendDisguiseForPacket(networkManager, rotPacket, disguise);
    }


    public static ClientboundMoveEntityPacket.PosRot processMoveEntityPosRotPacket(DenizenNetworkManagerImpl networkManager, ClientboundMoveEntityPacket.PosRot posRotPacket, DisguiseCommand.TrackedDisguise disguise) {
        if (disguise.as.getBukkitEntityType() == EntityType.ENDER_DRAGON) {
            return new ClientboundMoveEntityPacket.PosRot(disguise.entity.getBukkitEntity().getEntityId(), posRotPacket.getXa(), posRotPacket.getYa(), posRotPacket.getZa(), EntityAttachmentHelper.adaptedCompressedAngle(posRotPacket.getyRot(), 180), posRotPacket.getxRot(), posRotPacket.isOnGround());
        }
        return sendDisguiseForPacket(networkManager, posRotPacket, disguise);
    }

    public static <T extends Packet<ClientGamePacketListener>> T sendDisguiseForPacket(DenizenNetworkManagerImpl networkManager, T packet, DisguiseCommand.TrackedDisguise disguise) {
        antiDuplicate = true;
        disguise.sendTo(List.of(new PlayerTag(networkManager.player.getUUID())));
        antiDuplicate = false;
        return null;
    }
}
