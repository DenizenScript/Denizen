package net.aufdemrand.denizen.utilities.entity;

import com.mojang.authlib.GameProfile;
import net.aufdemrand.denizen.utilities.entity.network.FakeNetworkManager;
import net.aufdemrand.denizen.utilities.entity.network.FakePlayerConnection;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayer extends EntityPlayer {

    public EntityFakePlayer(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        playerinteractmanager.setGameMode(WorldSettings.EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManager(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnection(minecraftserver, networkManager, this);
        networkManager.a(playerConnection);
        datawatcher.watch(10, (byte) 127);
        this.bukkitEntity = new CraftFakePlayer((CraftServer) Bukkit.getServer(), this);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public CraftFakePlayer getBukkitEntity() {
        return (CraftFakePlayer) bukkitEntity;
    }
}
