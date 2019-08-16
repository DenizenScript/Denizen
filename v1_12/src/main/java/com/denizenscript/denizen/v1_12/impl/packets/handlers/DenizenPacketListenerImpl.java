package com.denizenscript.denizen.v1_12.impl.packets.handlers;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.v1_12.impl.packets.PacketInResourcePackStatusImpl;
import com.denizenscript.denizen.v1_12.impl.packets.PacketInSteerVehicleImpl;
import com.denizenscript.denizen.nms.interfaces.packets.PacketHandler;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.NetworkManager;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayInResourcePackStatus;
import net.minecraft.server.v1_12_R1.PacketPlayInSteerVehicle;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class DenizenPacketListenerImpl extends AbstractListenerPlayInImpl {

    private static PacketHandler packetHandler;

    public DenizenPacketListenerImpl(NetworkManager networkManager, EntityPlayer entityPlayer) {
        super(networkManager, entityPlayer, entityPlayer.playerConnection);
    }

    public static void enable(PacketHandler handler) {
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

    // For compatibility with other plugins using Reflection weirdly...
    @Override
    public void sendPacket(Packet packet) {
        super.sendPacket(packet);
    }

    public static class PlayerEventListener implements Listener {
        @EventHandler(priority = EventPriority.LOWEST)
        public void onPlayerJoin(PlayerJoinEvent event) {
            DenizenNetworkManagerImpl.setNetworkManager(event.getPlayer(), packetHandler);
        }
    }
}
