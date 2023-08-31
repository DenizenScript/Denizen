package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.events.player.PlayerHearsSoundScriptEvent;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;

public class PlayerHearsSoundEventPacketHandlers {

    public static void registerHandlers() {

    }

    public boolean processSoundPacket(Packet<?> packet) {
        if (!PlayerHearsSoundScriptEvent.enabled) {
            return false;
        }
        if (packet instanceof ClientboundSoundPacket) {
            ClientboundSoundPacket spacket = (ClientboundSoundPacket) packet;
            return PlayerHearsSoundScriptEvent.instance.run(player.getBukkitEntity(), spacket.getSound().value().getLocation().getPath(), spacket.getSource().name(),
                    false, null, new Location(player.getBukkitEntity().getWorld(), spacket.getX(), spacket.getY(), spacket.getZ()), spacket.getVolume(), spacket.getPitch());
        }
        else if (packet instanceof ClientboundSoundEntityPacket) {
            ClientboundSoundEntityPacket spacket = (ClientboundSoundEntityPacket) packet;
            Entity entity = player.level().getEntity(spacket.getId());
            if (entity == null) {
                return false;
            }
            return PlayerHearsSoundScriptEvent.instance.run(player.getBukkitEntity(), spacket.getSound().value().getLocation().getPath(), spacket.getSource().name(),
                    false, entity.getBukkitEntity(), null, spacket.getVolume(), spacket.getPitch());
        }
        return false;
    }
}
