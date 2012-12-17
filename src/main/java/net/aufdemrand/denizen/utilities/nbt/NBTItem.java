package net.aufdemrand.denizen.utilities.nbt;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.server.v1_4_5.NBTTagCompound;
import net.minecraft.server.v1_4_5.NBTTagList;
import net.minecraft.server.v1_4_5.NBTTagString;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class NBTItem {

	public static String getDisplayName(ItemStack item) {
		if (item == null) return "";
		String displayName = "plain " + item.getType().name().toLowerCase();
		try {
			displayName = ((CraftItemStack) item).getHandle().getTag().getCompound("display").getString("Name");
		} catch (Exception e) { }
		return displayName;
	}

	public static MapOfEnchantments getEnchantments(ItemStack item) {
		return new MapOfEnchantments(item);
	}

	public static void addEnchantment(ItemStack item, Enchantment enchantment, Integer level) {
		item.addUnsafeEnchantment(enchantment, level);
	}

	public static ListOfLore getLore(ItemStack item) {
		return new ListOfLore(item);
	}

	public static void setDisplayName(ItemStack item, String name) {
		((CraftItemStack) item).getHandle().getTag().getCompound("display").setString("Name", name);
	}

	public static boolean hasEngraving(ItemStack item) {
		NBTTagCompound tag = ((CraftItemStack) item).getHandle().getTag();
		boolean hasEngraving = false;
		if (tag != null)
			if (tag.hasKey("display") && tag.getCompound("display").hasKey("Lore")) {
				NBTTagList list = tag.getCompound("display").getList("Lore");
				for (int counter = 0; counter < list.size(); counter++)
					if (((NBTTagString) list.get(counter)).data.contains("protection engraving")) hasEngraving = true;
			}
		return hasEngraving;
	}

	public static List<String> getEngravings(ItemStack item) {
		NBTTagCompound tag = ((CraftItemStack) item).getHandle().getTag();
		List<String> engravings = new ArrayList<String>();
		if (tag != null)
			if (tag.hasKey("display") && tag.getCompound("display").hasKey("Lore")) {
				NBTTagList list = tag.getCompound("display").getList("Lore");
				for (int counter = 0; counter < list.size(); counter++)
					if (((NBTTagString) list.get(counter)).data.contains("A unique engraving"))
						engravings.add(((NBTTagString) list.get(counter)).data.split(" ")[4]);
			}
		return engravings;
	}

	public static void removeEngraving(ItemStack item, String player) {
		NBTTagCompound tag = ((CraftItemStack) item).getHandle().getTag();
		NBTTagList newList =  new NBTTagList();
		if (tag != null)
			if (tag.hasKey("display") && tag.getCompound("display").hasKey("Lore")) {
				NBTTagList list = tag.getCompound("display").getList("Lore");
				for (int counter = 0; counter < list.size(); counter++)
					if (!((NBTTagString) list.get(counter)).data.contains("A unique engraving for " + player))
						newList.add((NBTTagString) list.get(counter));
			}
		tag.getCompound("display").set("Lore", newList);
	}

	public static void addEngraving(ItemStack item, String player) {
		NBTTagCompound tag;
		if (!((CraftItemStack) item).getHandle().hasTag())
			((CraftItemStack) item).getHandle().setTag(new NBTTagCompound());
		tag = ((CraftItemStack) item).getHandle().getTag();
		if (!tag.hasKey("display"))
			tag.setCompound("display", new NBTTagCompound());
		NBTTagCompound display = tag.getCompound("display");
		NBTTagList list =  new NBTTagList();
		if (display.hasKey("Lore"))
			list = display.getList("Lore");
		list.add(new NBTTagString("A unique engraving for " + player));
		((CraftItemStack) item).getHandle().tag = tag;
	}

}
