package com.denizenscript.denizen.nms.v1_15.impl.network.handlers;

import com.denizenscript.denizen.nms.v1_15.impl.network.packets.PacketInResourcePackStatusImpl;
import com.denizenscript.denizen.nms.v1_15.impl.network.packets.PacketInSteerVehicleImpl;
import com.denizenscript.denizen.utilities.packets.DenizenPacketHandler;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import com.denizenscript.denizen.nms.NMSHandler;
import net.minecraft.server.v1_15_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;

public class DenizenPacketListenerImpl extends AbstractListenerPlayInImpl {

    private static DenizenPacketHandler packetHandler;

    public String brand = "unknown";

    public DenizenPacketListenerImpl(NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(networkManager, entityPlayer, entityPlayer.playerConnection);
    }

    public static void enable(DenizenPacketHandler handler) {
        packetHandler = handler;
        Bukkit.getServer().getPluginManager().registerEvents(new PlayerEventListener(), NMSHandler.getJavaPlugin());
    }

    @Override
    public void a(final PacketPlayInSteerVehicle packet) {
        if (!packetHandler.receivePacket(player.getBukkitEntity(), new PacketInSteerVehicleImpl(packet))) {
            super.a(packet);
        }
    }

    @Override
    public void a(PacketPlayInResourcePackStatus packet) {
        packetHandler.receivePacket(player.getBukkitEntity(), new PacketInResourcePackStatusImpl(packet));
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInBlockPlace packet) {
        packetHandler.receivePlacePacket(player.getBukkitEntity());
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInBlockDig packet) {
        packetHandler.receiveDigPacket(player.getBukkitEntity());
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInCustomPayload packet) {
        if (NMSHandler.debugPackets) {
            Debug.log("Custom packet payload: " + packet.tag.toString() + " sent from " + player.getName());
        }
        if (packet.tag.getNamespace().equals("minecraft") && packet.tag.getKey().equals("brand")) {
            int i = packet.data.i(); // read off the varInt of length to get rid of it
            brand = StandardCharsets.UTF_8.decode(packet.data.nioBuffer()).toString();
        }
        super.a(packet);
    }

    @Override
    public void a(PacketPlayInFlying packet) {
        if (DenizenPacketHandler.forceNoclip.contains(player.getUniqueID())) {
            player.noclip = true;
        }
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
            DenizenNetworkManagerImpl.setNetworkManager(event.getPlayer(), packetHandler);
        }
    }
}
