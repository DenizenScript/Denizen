package net.aufdemrand.denizen.utilities.nbt;

import org.bukkit.inventory.ItemStack;

public class NBTItem {

    public static MapOfEnchantments getEnchantments(ItemStack item) {
        return new MapOfEnchantments(item);
    }

    public static ListOfLore getLore(ItemStack item) {
        return new ListOfLore(item);
    }

}
