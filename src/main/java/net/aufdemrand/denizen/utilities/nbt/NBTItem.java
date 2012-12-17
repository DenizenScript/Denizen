package net.aufdemrand.denizen.utilities.nbt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.server.v1_4_5.NBTBase;
import net.minecraft.server.v1_4_5.NBTTagCompound;
import net.minecraft.server.v1_4_5.NBTTagList;
import net.minecraft.server.v1_4_5.NBTTagString;

import org.bukkit.craftbukkit.v1_4_5.inventory.CraftItemStack;
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
	        CraftItemStack cis = (CraftItemStack) is;
	 
	        NBTTagCompound tag = cis.getHandle().getTag();
	 
	        if (tag == null) {
	            cis.getHandle().setTag(new NBTTagCompound());
	            tag = cis.getHandle().getTag();
	        }
	 
	        if (tag.getCompound("display") == null) {
	            tag.setCompound("display", new NBTTagCompound());
	        }
	 
	        NBTTagCompound display = tag.getCompound("display");
	 
	        display.remove("Name");
	        display.setString("Name", name);
	 
	        return is;
	    }
	 
	    public static ItemStack setDescription(ItemStack is, List<String> desc) {
	        if (desc.isEmpty()) {
	            return is;
	        }
	        CraftItemStack cis = (CraftItemStack) is;
	 
	        NBTTagCompound tag = cis.getHandle().getTag();
	 
	        if (tag == null) {
	            cis.getHandle().setTag(new NBTTagCompound());
	            tag = cis.getHandle().getTag();
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
	 
	        display.remove("Lore");
	        display.set("Lore", lore);
	 
	        return is;
	    }
	    
	    public static String getName(ItemStack is){
	        if(is == null){
	            return null;
	        }
	        CraftItemStack cis = (CraftItemStack) is;
	 
	        NBTTagCompound tag = cis.getHandle().getTag();
	 
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
	        CraftItemStack cis = (CraftItemStack) is;
	 
	        NBTTagCompound tag = cis.getHandle().getTag();
	 
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

	
	
	

	public static boolean hasEngraving(ItemStack item) {
        NBTTagCompound tag;
        if (!((CraftItemStack) item).getHandle().hasTag()) return false;
		tag = ((CraftItemStack) item).getHandle().getTag();
		return tag.hasKey("owner");
	}

	public static String getEngraving(ItemStack item) {
        NBTTagCompound tag;
        if (!((CraftItemStack) item).getHandle().hasTag())
			((CraftItemStack) item).getHandle().setTag(new NBTTagCompound());
		tag = ((CraftItemStack) item).getHandle().getTag();
		if (tag.hasKey("owner")) return tag.getString("owner");
		return "";
	}

	public static void removeEngraving(ItemStack item, String player) {
		NBTTagCompound tag;
		if (!((CraftItemStack) item).getHandle().hasTag())
			((CraftItemStack) item).getHandle().setTag(new NBTTagCompound());
		tag = ((CraftItemStack) item).getHandle().getTag();
		tag.remove("owner");
	}

	public static void addEngraving(ItemStack item, String player) {
//        net.minecraft.server.v1_4_5.ItemStack itemStack = ((CraftItemStack) item).getHandle();
//		NBTTagCompound tag = itemStack.tag.getCompound("display");
//        NBTTagList list = new NBTTagList();
//        list.add(new NBTTagString("A unique engraving for " + player));
//        tag.set("Lore", list);
//        itemStack.tag.setCompound("display", tag);
		NBTTagCompound tag;
		if (!((CraftItemStack) item).getHandle().hasTag())
			((CraftItemStack) item).getHandle().setTag(new NBTTagCompound());
		tag = ((CraftItemStack) item).getHandle().getTag();
		tag.setString("owner", player);
//		if (!tag.hasKey("display"))
//			tag.setCompound("display", new NBTTagCompound());
//		NBTTagCompound display = tag.getCompound("display");
//		NBTTagList list =  new NBTTagList();
//		if (display.hasKey("Lore"))
//			list = display.getList("Lore");
//		list.add(new NBTTagString("A unique engraving for " + player));
//		((CraftItemStack) item).getHandle().tag = tag;
	}

}
