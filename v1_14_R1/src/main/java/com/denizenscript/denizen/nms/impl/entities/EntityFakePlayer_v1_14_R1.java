package com.denizenscript.denizen.nms.impl.entities;

import com.mojang.authlib.GameProfile;
import com.denizenscript.denizen.nms.Handler_v1_14_R1;
import com.denizenscript.denizen.nms.impl.network.FakeNetworkManager_v1_14_R1;
import com.denizenscript.denizen.nms.impl.network.FakePlayerConnection_v1_14_R1;
import com.denizenscript.denizencore.utilities.debugging.dB;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_14_R1.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayer_v1_14_R1 extends EntityPlayer {

    public EntityFakePlayer_v1_14_R1(MinecraftServer minecraftserver, WorldServer worldserver, GameProfile gameprofile, PlayerInteractManager playerinteractmanager) {
        super(minecraftserver, worldserver, gameprofile, playerinteractmanager);
        try {
            Handler_v1_14_R1.ENTITY_BUKKITYENTITY.set(this, new CraftFakePlayer_v1_14_R1((CraftServer) Bukkit.getServer(), this));
        }
        catch (Exception ex) {
            dB.echoError(ex);
        }
        playerinteractmanager.setGameMode(EnumGamemode.SURVIVAL);
        NetworkManager networkManager = new FakeNetworkManager_v1_14_R1(EnumProtocolDirection.CLIENTBOUND);
        playerConnection = new FakePlayerConnection_v1_14_R1(minecraftserver, networkManager, this);
        networkManager.setPacketListener(playerConnection);
        datawatcher.set(EntityHuman.bt, (byte) 127);
        worldserver.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @Override
    public CraftFakePlayer_v1_14_R1 getBukkitEntity() {
        return (CraftFakePlayer_v1_14_R1) super.getBukkitEntity();
    }
}
