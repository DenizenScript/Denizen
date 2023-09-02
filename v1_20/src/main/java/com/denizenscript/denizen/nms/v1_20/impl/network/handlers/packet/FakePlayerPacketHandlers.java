package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_20.impl.entities.EntityFakePlayerImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.world.entity.Entity;
import org.bukkit.Bukkit;

import java.util.Collections;

public class FakePlayerPacketHandlers {

    public static void registerHandlers() {
        DenizenNetworkManagerImpl.registerPacketHandler(ClientboundAddPlayerPacket.class, FakePlayerPacketHandlers::processFakePlayerSpawnForPacket);
    }

    public static void processFakePlayerSpawnForPacket(DenizenNetworkManagerImpl networkManager, Packet<?> packet) {
        if (packet instanceof ClientboundAddPlayerPacket) {
            int id = ((ClientboundAddPlayerPacket) packet).getEntityId();
            if (id != -1) {
                Entity e = networkManager.player.level().getEntity(id);
                processFakePlayerSpawn(networkManager, e);
            }
        }
    }

    public static void processFakePlayerSpawn(DenizenNetworkManagerImpl networkManager, Entity entity) {
        if (entity instanceof EntityFakePlayerImpl) {
            final EntityFakePlayerImpl fakePlayer = (EntityFakePlayerImpl) entity;
            networkManager.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(),
                    () -> networkManager.send(new ClientboundPlayerInfoRemovePacket(Collections.singletonList(fakePlayer.getUUID()))), 5);
        }
    }
}
