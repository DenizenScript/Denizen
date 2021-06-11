package com.denizenscript.denizen.nms.v1_17.impl.entities;

import com.denizenscript.denizen.nms.v1_17.Handler;
import com.denizenscript.denizen.nms.v1_17.impl.network.fakes.FakeNetworkManagerImpl;
import com.denizenscript.denizen.nms.v1_17.impl.network.fakes.FakePlayerConnectionImpl;
import com.mojang.authlib.GameProfile;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.protocol.EnumProtocolDirection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.server.level.PlayerInteractManager;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.level.EnumGamemode;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_17_R1.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayerImpl extends EntityPlayer {

    public EntityFakePlayerImpl(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager, boolean doAdd) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        try {
            Handler.ENTITY_BUKKITYENTITY.set(this, new CraftFakePlayerImpl((CraftServer) Bukkit.getServer(), this));
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        playerinteractmanager.setGameMode(EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManagerImpl(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnectionImpl(minecraftserver, networkManager, this);
        networkManager.setPacketListener(playerConnection);
        datawatcher.set(EntityHuman.bi, (byte) 127);
        if (doAdd) {
            worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
    }

    @Override
    public CraftFakePlayerImpl getBukkitEntity() {
        return (CraftFakePlayerImpl) super.getBukkitEntity();
    }
}
