package com.denizenscript.denizen.nms.v1_14.impl.entities;

import com.denizenscript.denizen.nms.v1_14.Handler;
import com.denizenscript.denizen.nms.v1_14.impl.network.FakeNetworkManagerImpl;
import com.denizenscript.denizen.nms.v1_14.impl.network.FakePlayerConnectionImpl;
import com.mojang.authlib.GameProfile;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayerImpl extends EntityPlayer {

    public EntityFakePlayerImpl(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
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
        datawatcher.set(EntityHuman.bt, (byte) 127);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public CraftFakePlayerImpl getBukkitEntity() {
        return (CraftFakePlayerImpl) super.getBukkitEntity();
    }
}
