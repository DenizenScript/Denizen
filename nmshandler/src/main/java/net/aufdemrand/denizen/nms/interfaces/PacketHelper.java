package net.aufdemrand.denizen.nms.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface PacketHelper {

    void setSlot(Player player, int slot, ItemStack itemStack, boolean playerOnly);

    void showBlockAction(Player player, Location location, int action, int state);

    void showBlockCrack(Player player, int id, Location location, int progress);
}
