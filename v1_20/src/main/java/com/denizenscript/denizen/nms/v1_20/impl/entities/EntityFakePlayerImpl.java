package com.denizenscript.denizen.nms.v1_20.impl.entities;

import com.denizenscript.denizen.nms.v1_20.Handler;
import com.denizenscript.denizen.nms.v1_20.impl.network.fakes.FakeNetworkManagerImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.fakes.FakePlayerConnectionImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import com.mojang.authlib.GameProfile;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.player.Player;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R4.CraftServer;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class EntityFakePlayerImpl extends ServerPlayer {

    public EntityFakePlayerImpl(MinecraftServer minecraftserver, ServerLevel worldserver, GameProfile gameprofile, ClientInformation clientInfo, boolean doAdd) {
        super(minecraftserver, worldserver, gameprofile, clientInfo);
        try {
            Handler.ENTITY_BUKKITYENTITY.set(this, new CraftFakePlayerImpl((CraftServer) Bukkit.getServer(), this));
            Connection networkManager = new FakeNetworkManagerImpl(PacketFlow.CLIENTBOUND);
            connection = new FakePlayerConnectionImpl(minecraftserver, networkManager, this, new CommonListenerCookie(gameprofile, 0, clientInfo, false));
            DenizenNetworkManagerImpl.Connection_packetListener.set(networkManager, connection);
        }
        catch (Exception ex) {
            Debug.echoError(ex);
        }
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
