package net.aufdemrand.denizen.nms.interfaces;

import net.aufdemrand.denizen.nms.util.PlayerProfile;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import org.bukkit.inventory.ItemStack;

public interface ItemHelper {

    String getJsonString(ItemStack itemStack);

    PlayerProfile getSkullSkin(ItemStack itemStack);

    ItemStack setSkullSkin(ItemStack itemStack, PlayerProfile playerProfile);

    ItemStack addNbtData(ItemStack itemStack, String key, Tag value);

    CompoundTag getNbtData(ItemStack itemStack);
}
