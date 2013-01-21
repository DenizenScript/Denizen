package net.aufdemrand.denizen.utilities.nbt;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;

public class ListOfLore extends ArrayList<String> {

	private static final long serialVersionUID = -4078135787654565125L;

	public ListOfLore(ItemStack item) {
		super();
		if (item == null) return;
        this.addAll(item.getItemMeta().getLore());
	}	

	public String asDScriptList() {
		String dScriptList = "";
		if (this.isEmpty()) return dScriptList;
		for (String lore : this)
			dScriptList = dScriptList + lore + "|";
		return dScriptList.substring(1, dScriptList.length() - 1);
	}

	
	
	
}
