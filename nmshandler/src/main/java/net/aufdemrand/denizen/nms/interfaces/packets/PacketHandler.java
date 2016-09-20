package net.aufdemrand.denizen.nms.interfaces.packets;

import org.bukkit.entity.Player;

public interface PacketHandler {

    void receivePacket(Player player, PacketInResourcePackStatus resourcePackStatus);

    boolean receivePacket(Player player, PacketInSteerVehicle steerVehicle);

    boolean sendPacket(Player player, PacketOutChat chat);

    boolean sendPacket(Player player, PacketOutSpawnEntity spawnEntity);

    boolean sendPacket(Player player, PacketOutEntityMetadata entityMetadata);
}
