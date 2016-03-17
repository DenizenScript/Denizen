package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import net.minecraft.server.v1_9_R1.EntityLiving;
import net.minecraft.server.v1_9_R1.NBTBase;
import net.minecraft.server.v1_9_R1.NBTTagCompound;
import net.minecraft.server.v1_9_R1.NBTTagList;
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
                    cis = unregisterNBT(cis, key);
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
        NBTTagCompound tag;
        // Do stuff with tag
        if (!cis.hasTag()) {
            cis.setTag(new NBTTagCompound());
        }
        tag = cis.getTag();
        List<String> existingKeys = listNBT(item, "");
        String existing = "";
        String lowerKey = CoreUtilities.toLowerCase(key);
        for (String existingKey : existingKeys) {
            String exKeyLower = CoreUtilities.toLowerCase(existingKey);
            if (lowerKey.equals(exKeyLower)) {
                existing = existingKey;
                break;
            }
            if (lowerKey.startsWith(CoreUtilities.toLowerCase(existingKey))
                    && existingKey.length() > existing.length()
                    && lowerKey.substring(existingKey.length(), existingKey.length()+1).equals(".")) {
                existing = existingKey;
            }
        }
        String finalKey = null;
        if (!existing.equals("")) {
            for (String subkey : CoreUtilities.split(existing, '.')) {
                NBTBase base = tag.get(subkey);
                if (base instanceof NBTTagCompound) {
                    tag = (NBTTagCompound) base;
                }
                else {
                    finalKey = subkey;
                }
            }
        }
        if (finalKey == null) {
            Iterator<String> subkeys = CoreUtilities.split(key.substring(existing.equals("") ? 0 : existing.length() + 1), '.').iterator();
            while (true) {
                String subkey = subkeys.next();
                dB.log(subkey);
                if (!subkeys.hasNext()) {
                    tag.setString(subkey, value);
                    cis = registerNBT(cis, key);
                    break;
                }
                else {
                    tag.set(subkey, new NBTTagCompound());
                    tag = tag.getCompound(subkey);
                }
            }
        }
        else {
            tag.setString(finalKey, value);
        }
        return CraftItemStack.asCraftMirror(cis);
    }

    private static net.minecraft.server.v1_9_R1.ItemStack registerNBT(net.minecraft.server.v1_9_R1.ItemStack item, String key) {
        if (item == null) {
            return null;
        }
        NBTTagCompound tag;
        if (!item.hasTag()) {
            item.setTag(new NBTTagCompound());
        }
        tag = item.getTag();
        NBTTagList list = new NBTTagList();
        if (tag.hasKeyOfType("Denizen-Registered-Keys", list.getTypeId())) {
            list = tag.getList("Denizen-Registered-Keys", new NBTTagString().getTypeId());
        }
        list.add(new NBTTagString(key));
        tag.set("Denizen-Registered-Keys", list);
        return item;
    }

    private static net.minecraft.server.v1_9_R1.ItemStack unregisterNBT(net.minecraft.server.v1_9_R1.ItemStack item, String key) {
        if (item == null) {
            return null;
        }
        NBTTagCompound tag;
        if (!item.hasTag()) {
            return item;
        }
        tag = item.getTag();
        NBTTagList list = new NBTTagList();
        if (tag.hasKeyOfType("Denizen-Registered-Keys", list.getTypeId())) {
            list = tag.getList("Denizen-Registered-Keys", new NBTTagString().getTypeId());
        }
        else {
            return item;
        }
        String lowerKey = CoreUtilities.toLowerCase(key);
        for (int i = 0; i < list.size(); i++) {
            if (CoreUtilities.toLowerCase(list.getString(i)).equals(lowerKey)) {
                list.remove(i);
                break;
            }
        }
        tag.set("Denizen-Registered-Keys", list);
        return item;
    }

    public static List<String> getRegisteredNBT(ItemStack item) {
        if (item == null) {
            return null;
        }
        net.minecraft.server.v1_9_R1.ItemStack cis = CraftItemStack.asNMSCopy(item);
        NBTTagCompound tag;
        if (!cis.hasTag()) {
            return null;
        }
        tag = cis.getTag();
        NBTTagList list = new NBTTagList();
        if (tag.hasKeyOfType("Denizen-Registered-Keys", list.getTypeId())) {
            list = tag.getList("Denizen-Registered-Keys", new NBTTagString().getTypeId());
        }
        else {
            return null;
        }
        List<String> ret = new ArrayList<String>();
        for (int i = 0; i < list.size(); i++) {
            ret.add(list.getString(i));
        }
        return ret;
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


