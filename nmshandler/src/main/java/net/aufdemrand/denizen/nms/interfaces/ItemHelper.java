package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.util.PlayerProfile;
import org.bukkit.inventory.ItemStack;

public interface ItemHelper {

    PlayerProfile getSkullSkin(ItemStack itemStack);

    ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);
}
