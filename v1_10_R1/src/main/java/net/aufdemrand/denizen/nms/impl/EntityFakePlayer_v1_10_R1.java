package net.aufdemrand.denizen.nms.impl;

import com.mojang.authlib.GameProfile;
import net.aufdemrand.denizen.nms.impl.network.FakeNetworkManager_v1_10_R1;
import net.aufdemrand.denizen.nms.impl.network.FakePlayerConnection_v1_10_R1;
import net.minecraft.server.v1_10_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_10_R1.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class EntityFakePlayer_v1_10_R1 extends EntityPlayer {

    public EntityFakePlayer_v1_10_R1(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager, JavaPlugin plugin) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.bukkitEntity = new CraftFakePlayer_v1_10_R1((CraftServer) Bukkit.getServer(), this, plugin);
        playerinteractmanager.setGameMode(EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManager_v1_10_R1(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnection_v1_10_R1(minecraftserver, networkManager, this);
        networkManager.setPacketListener(playerConnection);
        datawatcher.set(EntityHuman.br, (byte) 127);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public CraftFakePlayer_v1_10_R1 getBukkitEntity() {
        return (CraftFakePlayer_v1_10_R1) bukkitEntity;
    }
}
