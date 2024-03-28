package com.denizenscript.denizen.nms.v1_20.impl.network.handlers.packet;

/*
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.v1_20.impl.entities.EntityFakePlayerImpl;
import com.denizenscript.denizen.nms.v1_20.impl.network.handlers.DenizenNetworkManagerImpl;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import org.bukkit.Bukkit;

import java.util.List;
*/

public class FakePlayerPacketHandlers {

    public static void registerHandlers() {
        // TODO: 1.20.2: Replace this.
        //DenizenNetworkManagerImpl.registerPacketHandler(ClientboundAddPlayerPacket.class, FakePlayerPacketHandlers::processAddPlayerPacket);
    }

    /*
    public static void processAddPlayerPacket(DenizenNetworkManagerImpl networkManager, ClientboundAddPlayerPacket addPlayerPacket) {
        if (networkManager.player.level().getEntity(addPlayerPacket.getEntityId()) instanceof EntityFakePlayerImpl fakePlayer) {
            networkManager.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, fakePlayer));
            Bukkit.getScheduler().runTaskLater(NMSHandler.getJavaPlugin(),
                    () -> networkManager.send(new ClientboundPlayerInfoRemovePacket(List.of(fakePlayer.getUUID()))), 5);
        }
    }*/
}
