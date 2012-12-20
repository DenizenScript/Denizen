package net.aufdemrand.denizen.utilities.nbt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_4_6.NBTBase;
import net.minecraft.server.v1_4_6.NBTTagCompound;
import net.minecraft.server.v1_4_6.NBTTagList;
import net.minecraft.server.v1_4_6.NBTTagString;

import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

public class NBTItem {

	public static MapOfEnchantments getEnchantments(ItemStack item) {
		return new MapOfEnchantments(item);
	}

	public static void addEnchantment(ItemStack item, Enchantment enchantment, Integer level) {
		item.addUnsafeEnchantment(enchantment, level);
	}

	public static ListOfLore getLore(ItemStack item) {
		return new ListOfLore(item);
	}

	
	 public static ItemStack setName(ItemStack is, String name) {
	        net.minecraft.server.v1_4_6.ItemStack cis =  CraftItemStack.asNMSCopy(is);
	 
	        NBTTagCompound tag = cis.getTag();
	 
	        if (tag == null) {
	            cis.setTag(new NBTTagCompound());
	            tag = cis.getTag();
	        }
	 
	        if (tag.getCompound("display") == null) {
	            tag.setCompound("display", new NBTTagCompound());
	        }
	 
	        NBTTagCompound display = tag.getCompound("display");
	 
	        display.setString("Name", name);
	 
	        return is;
	    }
	 
	    public static ItemStack setDescription(ItemStack is, List<String> desc) {
	        if (desc.isEmpty()) {
	            return is;
	        }
	        net.minecraft.server.v1_4_6.ItemStack cis =  CraftItemStack.asNMSCopy(is);
	 
	        NBTTagCompound tag = cis.getTag();
	 
	        if (tag == null) {
	            cis.setTag(new NBTTagCompound());
	            tag = cis.getTag();
	        }
	 
	        if (tag.getCompound("display") == null) {
	            tag.setCompound("display", new NBTTagCompound());
	        }
	 
	        NBTTagCompound display = tag.getCompound("display");
	 
	        NBTTagList lore = new NBTTagList();
	        Iterator<String> iterator = desc.iterator();
	        while (iterator.hasNext()) {
	            NBTBase line = NBTBase.createTag((byte) 8, iterator.next());
	            lore.add(line);
	        }
	 
	        display.set("Lore", lore);
	 
	        return is;
	    }
	    
	    public static String getName(ItemStack is){
	        if(is == null){
	            return null;
	        }
	        net.minecraft.server.v1_4_6.ItemStack cis =  CraftItemStack.asNMSCopy(is);

	 
	        NBTTagCompound tag = cis.getTag();
	 
	        if (tag == null) {
	            return null;
	        }
	 
	        if (tag.getCompound("display") == null) {
	            return null;
	        }
	 
	        NBTTagCompound display = tag.getCompound("display");
	        
	        if(display.getString("Name") == null) {
	            return null;
	        }
	 
	        return display.getString("Name");
	    }
	 
	    public static List<String> getDescription(ItemStack is) {
	        if (is == null) {
	            return null;
	        }
	        net.minecraft.server.v1_4_6.ItemStack cis =  CraftItemStack.asNMSCopy(is);

	 
	        NBTTagCompound tag = cis.getTag();
	 
	        if (tag == null) {
	            return null;
	        }
	 
	        if (tag.getCompound("display") == null) {
	            return null;
	        }
	 
	        NBTTagCompound display = tag.getCompound("display");
	 
	        if (display.getList("Lore") == null) {
	            return null;
	        }
	 
	        NBTTagList lore = display.getList("Lore");
	        List<String> desc = new ArrayList();
	        for (int x = 0; x < lore.size(); x++) {
	            desc.add(lore.get(x).toString());
	        }
	 
	        return desc;
	    }

}
