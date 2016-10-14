package net.aufdemrand.denizen.nms.interfaces.packets;

import org.bukkit.entity.Player;

public interface PacketHandler {

    void receivePacket(Player player, PacketInResourcePackStatus resourcePackStatus);

    boolean receivePacket(Player player, PacketInSteerVehicle steerVehicle);

    boolean sendPacket(Player player, PacketOutChat chat);

    boolean sendPacket(Player player, PacketOutEntityMetadata entityMetadata);

    boolean sendPacket(Player player, PacketOutSetSlot setSlot);

    boolean sendPacket(Player player, PacketOutWindowItems windowItems);

    boolean sendPacket(Player player, PacketOutTradeList tradeList);
}
