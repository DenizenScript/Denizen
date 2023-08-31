package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.utilities.entity.HideEntitiesHelper;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.entity.Entity;

public class HiddenEntitiesPacketHandlers {

    public static void registerHandlers() {

    }

    public boolean isHidden(Entity entity) {
        return entity != null && HideEntitiesHelper.playerShouldHide(player.getBukkitEntity().getUniqueId(), entity.getBukkitEntity());
    }

    public boolean processHiddenEntitiesForPacket(Packet<?> packet) {
        if (!HideEntitiesHelper.hasAnyHides()) {
            return false;
        }
        try {
            int ider = -1;
            Entity e = null;
            if (packet instanceof ClientboundAddPlayerPacket) {
                ider = ((ClientboundAddPlayerPacket) packet).getEntityId();
            }
            else if (packet instanceof ClientboundAddEntityPacket) {
                ider = ((ClientboundAddEntityPacket) packet).getId();
            }
            else if (packet instanceof ClientboundAddExperienceOrbPacket) {
                ider = ((ClientboundAddExperienceOrbPacket) packet).getId();
            }
            else if (packet instanceof ClientboundMoveEntityPacket) {
                e = ((ClientboundMoveEntityPacket) packet).getEntity(player.level());
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
                e = player.level().getEntity(ider);
            }
            if (e != null) {
                if (isHidden(e)) {
                    return true;
                }
            }
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        return false;
    }
}
