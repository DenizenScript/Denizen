package com.denizenscript.denizen.v1_14.impl.packets.handlers;

import com.denizenscript.denizen.v1_14.impl.packets.PacketInResourcePackStatus_v1_14_R1;
import com.denizenscript.denizen.v1_14.impl.packets.PacketInSteerVehicle_v1_14_R1;
import com.denizenscript.denizen.nms.interfaces.packets.PacketHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import com.denizenscript.denizen.nms.NMSHandler;
import net.minecraft.server.v1_14_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nullable;

public class DenizenPacketListener_v1_14_R1 extends AbstractListenerPlayIn_v1_14_R1 {

    private static PacketHandler packetHandler;

    public DenizenPacketListener_v1_14_R1(NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(networkManager, entityPlayer, entityPlayer.playerConnection);
    }

    public static void enable(PacketHandler handler) {
        packetHandler = handler;
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerEventListener(), NMSHandler.getJavaPlugin());
    }

    @Override
    public void a(final PacketPlayInSteerVehicle packet) {
        if (!packetHandler.receivePacket(player.getBukkitEntity(), new PacketInSteerVehicle_v1_14_R1(packet))) {
            super.a(packet);
        }
    }

    @Override
    public void a(PacketPlayInResourcePackStatus packet) {
        packetHandler.receivePacket(player.getBukkitEntity(), new PacketInResourcePackStatus_v1_14_R1(packet));
        super.a(packet);
    }

    // For compatibility with other plugins using Reflection weirdly...
    @Override
    public void sendPacket(Packet packet) {
        super.sendPacket(packet);
    }

    @Override
    public void a(Packet<?> packet, @Nullable GenericFutureListener<? extends Future<? super Void>> genericfuturelistener) {
        super.a(packet, genericfuturelistener);
    }

    public static class PlayerEventListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
            DenizenNetworkManager_v1_14_R1.setNetworkManager(event.getPlayer(), packetHandler);
        }
    }
}
