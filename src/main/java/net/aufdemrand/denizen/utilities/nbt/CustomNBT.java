package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_9_R1.EntityLiving;
import net.minecraft.server.v1_9_R1.NBTBase;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagString;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_9_R1.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class CustomNBT {

    public static MapOfEnchantments getEnchantments(ItemStack item) {
        return new MapOfEnchantments(item);
    }

    /*
     * Some static methods for dealing with Minecraft NBT data, which is used to store
     * custom NBT.
     */

    public static boolean hasCustomNBT(ItemStack item, String key) {
        if (item == null) {
            return false;
        }
        net.minecraft.server.v1_9_R1.ItemStack cis = CraftItemStack.asNMSCopy(item);
        if (!cis.hasTag()) {
            return false;
        }
        key = CoreUtilities.toLowerCase(key);
        List<String> keys = listNBT(item, "");
        for (String string : keys) {
            if (CoreUtilities.toLowerCase(string).equals(key)) {
                return true;
            }
        }
        return false;
    }

    public static String getCustomNBT(ItemStack item, String key) {
        if (item == null) {
            return null;
        }
        net.minecraft.server.v1_9_R1.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!cis.hasTag()) {
            cis.setTag(new NBTTagCompound());
        }
        tag = cis.getTag();
        key = CoreUtilities.toLowerCase(key);
        String finalKey = null;
        List<String> keys = listNBT(item, "");
        for (String string : keys) {
            if (CoreUtilities.toLowerCase(string).equals(key)) {
                finalKey = string;
                break;
            }
        }
        if (finalKey == null) {
            return null;
        }
        Iterator<String> subkeys = CoreUtilities.split(finalKey, '.').iterator();
        if (subkeys.hasNext()) {
            while (true) {
                String subkey = subkeys.next();
                NBTBase base = tag.get(subkey);
                if (!subkeys.hasNext()) {
                    if (base instanceof NBTTagString) {
                        return ((NBTTagString) base).a_();
                    }
                    else if (base instanceof NBTBase.NBTNumber) {
                        return new Element(((NBTBase.NBTNumber) base).h()).asString();
                    }
                    else {
                        return base.toString();
                    }
                }
                else if (base instanceof NBTTagCompound) {
                    tag = (NBTTagCompound) base;
                }
                else {
                    return null;
                }
            }
        }
        return null;

    }

    public static ItemStack removeCustomNBT(ItemStack item, String key) {
        if (item == null) {
            return null;
        }
        net.minecraft.server.v1_9_R1.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!cis.hasTag()) {
            cis.setTag(new NBTTagCompound());
        }
        tag = cis.getTag();
        key = CoreUtilities.toLowerCase(key);
        String finalKey = null;
        List<String> keys = listNBT(item, "");
        for (String string : keys) {
            if (CoreUtilities.toLowerCase(string).equals(key)) {
                finalKey = string;
                break;
            }
        }
        if (finalKey == null) {
            return null;
        }
        Iterator<String> subkeys = CoreUtilities.split(finalKey, '.').iterator();
        if (subkeys.hasNext()) {
            while (true) {
                String subkey = subkeys.next();
                if (!subkeys.hasNext()) {
                    tag.remove(subkey);
                }
                else if (tag.get(subkey).getTypeId() == tag.getTypeId()) {
                    tag = tag.getCompound(subkey);
                    continue;
                }
                break;
            }
        }
        return CraftItemStack.asCraftMirror(cis);
    }

    public static List<String> listNBT(ItemStack item, String filter) {
        if (item == null) {
            return null;
        }
        net.minecraft.server.v1_9_R1.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!cis.hasTag()) {
            cis.setTag(new NBTTagCompound());
        }
        tag = cis.getTag();
        return recursiveSearch(tag, "", filter);
    }

    private static List<String> recursiveSearch(NBTTagCompound compound, String base, String filter) {
        Set<String> keys = compound.c();
        List<String> finalKeys = new ArrayList<String>();
        filter = CoreUtilities.toLowerCase(filter);
        for (String key : keys) {
            String full = base + key;
            if (CoreUtilities.toLowerCase(full).startsWith(filter)) {
                finalKeys.add(full);
            }
            if (compound.get(key).getTypeId() == compound.getTypeId()) {
                finalKeys.addAll(recursiveSearch(compound.getCompound(key), full + ".", filter));
            }
        }
        return finalKeys;
    }

    public static ItemStack addCustomNBT(ItemStack item, String key, String value) {
        if (item == null) {
            return null;
        }
        net.minecraft.server.v1_9_R1.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag = null;
        // Do stuff with tag
        if (!cis.hasTag()) {
            cis.setTag(new NBTTagCompound());
        }
        tag = cis.getTag();
        tag.setString(key, value);
        return CraftItemStack.asCraftMirror(cis);
    }

    public static LivingEntity addCustomNBT(LivingEntity entity, String key, String value) {
        if (entity == null) {
            return null;
        }
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
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
        if (entity == null) {
            return null;
        }
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
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
        if (entity == null) {
            return false;
        }
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        // Writes the entity's NBT data to tag
        nmsEntity.c(tag);

        // Check for key
        return tag.hasKey(key);
    }

    public static String getCustomNBT(LivingEntity entity, String key) {
        if (entity == null) {
            return null;
        }
        Entity bukkitEntity = entity;
        net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();

        // Writes the entity's NBT data to tag
        nmsEntity.c(tag);

        // Return contents of the tag
        return tag.getString(key);
    }
}


