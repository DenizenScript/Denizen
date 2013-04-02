package net.aufdemrand.denizen.utilities.nbt;

import net.minecraft.server.v1_5_R2.NBTTagCompound;
import org.bukkit.craftbukkit.v1_5_R2.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class NBTItem {

    public static MapOfEnchantments getEnchantments(ItemStack item) {
        return new MapOfEnchantments(item);
    }

    /*
     * Some static methods for dealing with Minecraft NBT data, which is used to store
     * custom NBT.
     */

    public static boolean hasCustomNBT(ItemStack item, String key) {
        NBTTagCompound tag;
        net.minecraft.server.v1_5_R2.ItemStack cis = CraftItemStack.asNMSCopy(item);
        if (!cis.hasTag()) return false;
        tag = cis.getTag();
        // dB.echoDebug(tag.toString());
        // if this item has the NBTData for 'owner', there is an engraving.
        return tag.hasKey(key);
    }

    public static String getCustomNBT(ItemStack item, String key) {
        net.minecraft.server.v1_5_R2.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        // if this item has the NBTData for 'owner', return the value, which is the playername of the 'owner'.
        if (tag.hasKey(key)) return tag.getString(key);
        return null;

    }

    public static ItemStack removeCustomNBT(ItemStack item, String key) {
        net.minecraft.server.v1_5_R2.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        // remove 'owner' NBTData
        tag.remove(key);
        return CraftItemStack.asCraftMirror(cis);
    }

    public static ItemStack addCustomNBT(ItemStack item, String key, String value) {
        net.minecraft.server.v1_5_R2.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;
        // Do stuff with tag
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        tag.setString(key, value);
        return CraftItemStack.asCraftMirror(cis);
    }

}


