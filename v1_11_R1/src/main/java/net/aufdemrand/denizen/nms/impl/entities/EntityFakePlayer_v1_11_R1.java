package net.aufdemrand.denizen.nms.impl.entities;

import com.mojang.authlib.GameProfile;
import net.aufdemrand.denizen.nms.impl.network.FakeNetworkManager_v1_11_R1;
import net.aufdemrand.denizen.nms.impl.network.FakePlayerConnection_v1_11_R1;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_11_R1.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayer_v1_11_R1 extends EntityPlayer {

    public EntityFakePlayer_v1_11_R1(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        this.bukkitEntity = new CraftFakePlayer_v1_11_R1((CraftServer) Bukkit.getServer(), this);
        playerinteractmanager.setGameMode(EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManager_v1_11_R1(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnection_v1_11_R1(minecraftserver, networkManager, this);
        networkManager.setPacketListener(playerConnection);
        datawatcher.set(EntityHuman.br, (byte) 127);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public CraftFakePlayer_v1_11_R1 getBukkitEntity() {
        return (CraftFakePlayer_v1_11_R1) bukkitEntity;
    }
}
