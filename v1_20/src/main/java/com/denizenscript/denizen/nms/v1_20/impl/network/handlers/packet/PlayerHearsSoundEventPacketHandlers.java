package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.events.player.PlayerHearsSoundScriptEvent;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.world.entity.Entity;
import org.bukkit.Location;

public class PlayerHearsSoundEventPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSoundPacket.class, PlayerHearsSoundEventPacketHandlers::processSoundPacket);
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundSoundEntityPacket.class, PlayerHearsSoundEventPacketHandlers::processSoundPacket);
    }

    public static Packet<ClientGamePacketListener> processSoundPacket(DenizenNetworkManagerImpl networkManager, Packet<ClientGamePacketListener> packet) {
        if (!PlayerHearsSoundScriptEvent.instance.eventData.isEnabled) {
            return packet;
        }
        if (packet instanceof ClientboundSoundPacket) {
            ClientboundSoundPacket spacket = (ClientboundSoundPacket) packet;
            return PlayerHearsSoundScriptEvent.instance.run(networkManager.player.getBukkitEntity(), spacket.getSound().value().getLocation().getPath(), spacket.getSource().name(),
                    false, null, new Location(networkManager.player.getBukkitEntity().getWorld(), spacket.getX(), spacket.getY(), spacket.getZ()), spacket.getVolume(), spacket.getPitch()) ? null : packet;
        }
        else if (packet instanceof ClientboundSoundEntityPacket) {
            ClientboundSoundEntityPacket spacket = (ClientboundSoundEntityPacket) packet;
            Entity entity = networkManager.player.level().getEntity(spacket.getId());
            if (entity == null) {
                return packet;
            }
            return PlayerHearsSoundScriptEvent.instance.run(networkManager.player.getBukkitEntity(), spacket.getSound().value().getLocation().getPath(), spacket.getSource().name(),
                    false, entity.getBukkitEntity(), null, spacket.getVolume(), spacket.getPitch()) ? null : packet;
        }
        return packet;
    }
}
