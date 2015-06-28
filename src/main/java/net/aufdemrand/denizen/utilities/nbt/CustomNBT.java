package net.aufdemrand.denizen.utilities.nbt;

import net.minecraft.server.v1_8_R3.EntityLiving;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

public class CustomNBT {

    public static MapOfEnchantments getEnchantments(ItemStack item) {
        return new MapOfEnchantments(item);
    }

    /*
     * Some static methods for dealing with Minecraft NBT data, which is used to store
     * custom NBT.
     */

    public static boolean hasCustomNBT(ItemStack item, String key) {
        if (item == null) return false;
        NBTTagCompound tag;
        net.minecraft.server.v1_8_R3.ItemStack cis = CraftItemStack.asNMSCopy(item);
        if (!cis.hasTag()) return false;
        tag = cis.getTag();
        // dB.echoDebug(tag.toString());
        // if this item has the NBTData for 'owner', there is an engraving.
        return tag.hasKey(key);
    }

    public static String getCustomNBT(ItemStack item, String key) {
        if (item == null) return null;
        net.minecraft.server.v1_8_R3.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        // if this item has the NBTData for 'owner', return the value, which is the playername of the 'owner'.
        if (tag.hasKey(key)) return tag.getString(key);
        return null;

    }

    public static ItemStack removeCustomNBT(ItemStack item, String key) {
        if (item == null) return null;
        net.minecraft.server.v1_8_R3.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        // remove 'owner' NBTData
        tag.remove(key);
        return CraftItemStack.asCraftMirror(cis);
    }

    public static ItemStack addCustomNBT(ItemStack item, String key, String value) {
        if (item == null) return null;
        net.minecraft.server.v1_8_R3.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;
        // Do stuff with tag
        if (!cis.hasTag())
            cis.setTag(new NBTTagCompound());
        tag = cis.getTag();
        tag.setString(key, value);
        return CraftItemStack.asCraftMirror(cis);
    }

    public static LivingEntity addCustomNBT(LivingEntity entity, String key, String value) {
        if (entity == null) return null;
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        // Writes the entity's NBT data to tag
        nmsEntity.c(tag);

        // Add custom NBT
        tag.setString(key, value);

        // Write tag back
        ((EntityLiving) nmsEntity).a(tag);
        return entity;
    }

    public static LivingEntity removeCustomNBT(LivingEntity entity, String key) {
        if (entity == null) return null;
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        // Writes the entity's NBT data to tag
        nmsEntity.c(tag);

        // Remove custom NBT
        tag.remove(key);

        // Write tag back
        ((EntityLiving) nmsEntity).a(tag);
        return entity;
    }

    public static boolean hasCustomNBT(LivingEntity entity, String key) {
        if (entity == null) return false;
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        // Writes the entity's NBT data to tag
        nmsEntity.c(tag);

        // Check for key
        return tag.hasKey(key);
    }

    public static String getCustomNBT(LivingEntity entity, String key) {
        if (entity == null) return null;
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_8_R3.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        // Writes the entity's NBT data to tag
        nmsEntity.c(tag);

        // Return contents of the tag
        return tag.getString(key);
    }
}


