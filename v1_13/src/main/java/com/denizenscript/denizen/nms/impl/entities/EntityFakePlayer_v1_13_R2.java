package com.denizenscript.denizen.nms.impl.entities;

import com.mojang.authlib.GameProfile;
import com.denizenscript.denizen.nms.impl.network.FakeNetworkManager_v1_13_R2;
import com.denizenscript.denizen.nms.impl.network.FakePlayerConnection_v1_13_R2;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayer_v1_13_R2 extends EntityPlayer {

    public EntityFakePlayer_v1_13_R2(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.bukkitEntity = new CraftFakePlayer_v1_13_R2((CraftServer) Bukkit.getServer(), this);
        playerinteractmanager.setGameMode(EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManager_v1_13_R2(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnection_v1_13_R2(minecraftserver, networkManager, this);
        networkManager.setPacketListener(playerConnection);
        datawatcher.set(EntityHuman.bx, (byte) 127);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public CraftFakePlayer_v1_13_R2 getBukkitEntity() {
        return (CraftFakePlayer_v1_13_R2) bukkitEntity;
    }
}
