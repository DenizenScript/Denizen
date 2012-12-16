package net.aufdemrand.denizen.utilities.nbt;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBT {

	ItemStack item;
	
	public NBT(ItemStack item) {
		this.item = item;
	}
	
	public String getDisplayName() {
		String displayName = "Plain " + item.getType().name().toLowerCase();
		try {
			displayName = ((CraftItemStack) item).getHandle().getTag().getCompound("display").getString("Name");
		} catch (Exception e) { }
		return displayName;
	}
	
	public MapOfEnchantments getEnchantments() {
		return new MapOfEnchantments(item);
	}
	
	public ListOfLore getLore() {
		return new ListOfLore(item);
	}
	
}
