package net.aufdemrand.denizen.utilities.nbt;

import net.minecraft.server.v1_4_5.NBTTagCompound;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class NBT {

	ItemStack item;

	public NBT(ItemStack item) {
		this.item = item;
	}

	public String getDisplayName() {
		if (item == null) return "";
		String displayName = "plain " + item.getType().name().toLowerCase();
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

	public void setDisplayName(String name) {
		((CraftItemStack) item).getHandle().getTag().getCompound("display").setString("Name", name);
	}
	
	public boolean hasOwner() {
		return ((CraftItemStack) item).getHandle().getTag().hasKey("owner");
	}

	public String getOwner() {
		if (((CraftItemStack) item).getHandle().getTag().hasKey("owner"))
			return ((CraftItemStack) item).getHandle().getTag().getString("owner");
		else return "*";
	}
	
	public void setOwner(Player owner) {
		((CraftItemStack) item).getHandle().getTag().setString("owner", owner.getName()); 
	}

}
