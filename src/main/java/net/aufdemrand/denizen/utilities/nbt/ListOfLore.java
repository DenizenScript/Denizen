package net.aufdemrand.denizen.utilities.nbt;

import java.util.ArrayList;

import net.minecraft.server.v1_4_5.NBTTagCompound;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ListOfLore extends ArrayList<String> {

	private static final long serialVersionUID = -4078135787654565125L;

	public ListOfLore(ItemStack item) {
		super();
		if (item == null) return;
		net.minecraft.server.v1_4_5.ItemStack cis =  CraftItemStack.asNMSCopy(item);
		NBTTagCompound d = cis.getTag();
		int lores;
		if (d.hasKey("display") && d.getCompound("display").hasKey("Lore")) 
			for (lores = 0; lores < d.getCompound("display").getList("Lore").size(); lores++) {
				add(d.getCompound("display").getList("Lore").get(lores).getName());
			}
	}	

	public String asDScriptList() {
		String dScriptList = "";
		if (this.isEmpty()) return dScriptList;
		for (String lore : this)
			dScriptList = dScriptList + lore + "|";
		return dScriptList.substring(1, dScriptList.length() - 1);

	}

}
