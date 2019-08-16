package com.denizenscript.denizen.nms.v1_12.impl.entities;

import com.denizenscript.denizen.nms.v1_12.impl.network.FakeNetworkManagerImpl;
import com.denizenscript.denizen.nms.v1_12.impl.network.FakePlayerConnectionImpl;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayerImpl extends EntityPlayer {

    public EntityFakePlayerImpl(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.bukkitEntity = new CraftFakePlayerImpl((CraftServer) Bukkit.getServer(), this);
        playerinteractmanager.setGameMode(EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManagerImpl(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnectionImpl(minecraftserver, networkManager, this);
        networkManager.setPacketListener(playerConnection);
        datawatcher.set(EntityHuman.br, (byte) 127);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public CraftFakePlayerImpl getBukkitEntity() {
        return (CraftFakePlayerImpl) bukkitEntity;
    }
}
