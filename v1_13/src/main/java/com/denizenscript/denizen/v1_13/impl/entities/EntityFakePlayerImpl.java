package com.denizenscript.denizen.v1_13.impl.entities;

import com.mojang.authlib.GameProfile;
import com.denizenscript.denizen.v1_13.impl.network.FakeNetworkManagerImpl;
import com.denizenscript.denizen.v1_13.impl.network.FakePlayerConnectionImpl;
import net.minecraft.server.v1_13_R2.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_13_R2.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayerImpl extends EntityPlayer {

    public EntityFakePlayerImpl(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.bukkitEntity = new CraftFakePlayerImpl((CraftServer) Bukkit.getServer(), this);
        playerinteractmanager.setGameMode(EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManagerImpl(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnectionImpl(minecraftserver, networkManager, this);
        networkManager.setPacketListener(playerConnection);
        datawatcher.set(EntityHuman.bx, (byte) 127);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public CraftFakePlayerImpl getBukkitEntity() {
        return (CraftFakePlayerImpl) bukkitEntity;
    }
}
