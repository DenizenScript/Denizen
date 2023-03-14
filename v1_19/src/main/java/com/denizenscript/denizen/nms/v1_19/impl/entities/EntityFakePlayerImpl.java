package com.denizenscript.denizen.nms.v1_19.impl.entities;

import com.denizenscript.denizen.nms.v1_19.Handler;
import com.denizenscript.denizen.nms.v1_19.impl.network.fakes.FakeNetworkManagerImpl;
import com.denizenscript.denizen.nms.v1_19.impl.network.fakes.FakePlayerConnectionImpl;
import com.mojang.authlib.GameProfile;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_19_R3.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayerImpl extends ServerPlayer {

    public EntityFakePlayerImpl(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile, boolean doAdd) {
        super(minecraftserver, worldserver, gameprofile);
        try {
            Handler.ENTITY_BUKKITYENTITY.set(this, new CraftFakePlayerImpl((CraftServer) Bukkit.getServer(), this));
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
        Connection networkManager = new FakeNetworkManagerImpl(PacketFlow.CLIENTBOUND);
        connection = new FakePlayerConnectionImpl(minecraftserver, networkManager, this);
        networkManager.setListener(connection);
        getEntityData().set(Player.DATA_PLAYER_MODE_CUSTOMISATION, (byte) 127);
        if (doAdd) {
            worldserver.addFreshEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM);
        }
    }

    @Override
    public CraftFakePlayerImpl getBukkitEntity() {
        return (CraftFakePlayerImpl) super.getBukkitEntity();
    }
}
