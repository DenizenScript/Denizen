package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_20.impl.entities.EntityFakePlayerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;

import java.util.Collections;

public class FakePlayerPacketHandlers {

    public static void registerHandlers() {

    }

    public void processFakePlayerSpawnForPacket(Packet<?> packet) {
        if (packet instanceof ClientboundAddPlayerPacket) {
            int id = ((ClientboundAddPlayerPacket) packet).getEntityId();
            if (id != -1) {
                Entity e = player.level().getEntity(id);
                processFakePlayerSpawn(e);
            }
        }
    }

    public void processFakePlayerSpawn(Entity entity) {
        if (entity instanceof EntityFakePlayerImpl) {
            final EntityFakePlayerImpl fakePlayer = (EntityFakePlayerImpl) entity;
            send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(),
                    () -> send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(fakePlayer.getUUID()))), 5);
        }
    }
}
