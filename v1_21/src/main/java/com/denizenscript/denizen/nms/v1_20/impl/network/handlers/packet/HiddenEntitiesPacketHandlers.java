package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.denizenscript.denizen.utilities.entity.HideEntitiesHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public class HiddenEntitiesPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundAddEntityPacket.class, HiddenEntitiesPacketHandlers::processHiddenEntitiesForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundAddExperienceOrbPacket.class, HiddenEntitiesPacketHandlers::processHiddenEntitiesForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundMoveEntityPacket.Rot.class, HiddenEntitiesPacketHandlers::processHiddenEntitiesForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundMoveEntityPacket.Pos.class, HiddenEntitiesPacketHandlers::processHiddenEntitiesForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundMoveEntityPacket.PosRot.class, HiddenEntitiesPacketHandlers::processHiddenEntitiesForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSetEntityDataPacket.class, HiddenEntitiesPacketHandlers::processHiddenEntitiesForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSetEntityMotionPacket.class, HiddenEntitiesPacketHandlers::processHiddenEntitiesForPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundTeleportEntityPacket.class, HiddenEntitiesPacketHandlers::processHiddenEntitiesForPacket);
    }

    public static boolean isHidden(ServerPlayer player, Entity entity) {
        return entity != null && HideEntitiesHelper.playerShouldHide(player.getBukkitEntity().getUniqueId(), entity.getBukkitEntity());
    }

    public static Packet<ClientGamePacketListener> processHiddenEntitiesForPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (!HideEntitiesHelper.hasAnyHides()) {
            return packet;
        }
        try {
            int ider = -1;
            Entity e = null;
            if (packet instanceof ClientboundAddEntityPacket) {
                ider = ((ClientboundAddEntityPacket) packet).getId();
            }
            else if (packet instanceof ClientboundAddExperienceOrbPacket) {
                ider = ((ClientboundAddExperienceOrbPacket) packet).getId();
            }
            else if (packet instanceof ClientboundMoveEntityPacket) {
                e = ((ClientboundMoveEntityPacket) packet).getEntity(networkManager.player.level());
            }
            else if (packet instanceof ClientboundSetEntityDataPacket) {
                ider = ((ClientboundSetEntityDataPacket) packet).id();
            }
            else if (packet instanceof ClientboundSetEntityMotionPacket) {
                ider = ((ClientboundSetEntityMotionPacket) packet).getId();
            }
            else if (packet instanceof ClientboundTeleportEntityPacket) {
                ider = ((ClientboundTeleportEntityPacket) packet).getId();
            }
            if (e == null && ider != -1) {
                e = networkManager.player.level().getEntity(ider);
            }
            if (e != null) {
                if (isHidden(networkManager.player, e)) {
                    return null;
                }
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return packet;
    }
}
