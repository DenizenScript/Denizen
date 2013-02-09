package net.aufdemrand.denizen.utilities.nbt;

import net.minecraft.server.v1_4_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_4_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

import java.lang.reflect.Field;

public class NBTItem {

    public static MapOfEnchantments getEnchantments(ItemStack item) {
        return new MapOfEnchantments(item);
    }

    public static ListOfLore getLore(ItemStack item) {
        return new ListOfLore(item);
    }

    /*
     * Some static methods for dealing with Minecraft NBT data, which is used to store
     * custom NBT.
     */

    public static boolean hasCustomNBT(ItemStack item, String key) {
        NBTTagCompound tag;
        net.minecraft.server.v1_4_R1.ItemStack cis =  getItemStackHandle(item);
        if (!cis.hasTag()) return false;
        tag = cis.getTag();
        // if this item has the NBTData for 'owner', there is an engraving.
        return tag.hasKey(key);
    }

    public static String getCustomNBT(ItemStack item, String key) {
        net.minecraft.server.v1_4_R1.ItemStack cis =  getItemStackHandle(item);
        NBTTagCompound tag;
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        // if this item has the NBTData for 'owner', return the value, which is the playername of the 'owner'.
        if (tag.hasKey(key)) return tag.getString(key);
        return null;
    }

    public static void removeCustomNBT(ItemStack item, String key) {
        net.minecraft.server.v1_4_R1.ItemStack cis =  getItemStackHandle(item);
        NBTTagCompound tag;
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        // remove 'owner' NBTData
        tag.remove(key);
    }

    public static void addCustomNBT(ItemStack item, String key, String value) {
        net.minecraft.server.v1_4_R1.ItemStack cis = getItemStackHandle(item);
        NBTTagCompound tag = null;
        // Do stuff with tag
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        tag.setString(key, value);
    }

    public static net.minecraft.server.v1_4_R1.ItemStack getItemStackHandle(ItemStack item) {
        CraftItemStack cis = (CraftItemStack) item;
        Field f = null;
        try {
            // Use reflection to grant access to CraftItemStack field 'handle'
            // which is not public
            f = cis.getClass().getDeclaredField("handle");
            f.setAccessible(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        net.minecraft.server.v1_4_R1.ItemStack is = null;
        try {
            // Use reflection to get handle
            is = (net.minecraft.server.v1_4_R1.ItemStack) f.get(item);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        // Return the itemstack
        return is;
    }

}


