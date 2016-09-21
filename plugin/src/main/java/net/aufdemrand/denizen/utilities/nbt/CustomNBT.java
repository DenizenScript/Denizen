package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.ListTag;
import net.aufdemrand.denizen.nms.util.jnbt.StringTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(item);
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
                Tag base = compoundTag.getValue().get(subkey);
                if (!subkeys.hasNext()) {
                    if (base instanceof StringTag) {
                        return ((StringTag) base).getValue();
                    }
                    else {
                        return base.toString();
                    }
                }
                else if (base instanceof CompoundTag) {
                    compoundTag = (CompoundTag) base;
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
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(item);
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
                    compoundTag = compoundTag.createBuilder().remove(subkey).build();
                    item = unregisterNBT(NMSHandler.getInstance().getItemHelper().setNbtData(item, compoundTag), key);
                }
                else if (compoundTag.getValue().get(subkey) instanceof CompoundTag) {
                    compoundTag = (CompoundTag) compoundTag.getValue().get(subkey);
                    continue;
                }
                break;
            }
        }
        return item;
    }

    public static List<String> listNBT(ItemStack item, String filter) {
        if (item == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(item);
        return recursiveSearch(compoundTag, "", filter);
    }

    private static List<String> recursiveSearch(CompoundTag compound, String base, String filter) {
        Map<String, Tag> value = compound.getValue();
        Set<String> keys = compound.getValue().keySet();
        List<String> finalKeys = new ArrayList<String>();
        filter = CoreUtilities.toLowerCase(filter);
        for (String key : keys) {
            String full = base + key;
            if (CoreUtilities.toLowerCase(full).startsWith(filter)) {
                finalKeys.add(full);
            }
            if (value.get(key) instanceof CompoundTag) {
                finalKeys.addAll(recursiveSearch((CompoundTag) value.get(key), full + ".", filter));
            }
        }
        return finalKeys;
    }

    public static ItemStack addCustomNBT(ItemStack item, String key, String value) {
        if (item == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(item);
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
                Tag base = compoundTag.getValue().get(subkey);
                if (base instanceof CompoundTag) {
                    compoundTag = (CompoundTag) base;
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
                if (!subkeys.hasNext()) {
                    compoundTag = compoundTag.createBuilder().putString(subkey, value).build();
                    item = registerNBT(item, key);
                    break;
                }
                else {
                    compoundTag = compoundTag.createBuilder().put(subkey, NMSHandler.getInstance().createCompoundTag(new HashMap<String, Tag>())).build();
                    compoundTag = (CompoundTag) compoundTag.getValue().get(subkey);
                }
            }
        }
        else {
            compoundTag = compoundTag.createBuilder().putString(finalKey, value).build();
        }
        return NMSHandler.getInstance().getItemHelper().setNbtData(item, compoundTag);
    }

    private static ItemStack registerNBT(ItemStack item, String key) {
        if (item == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(item);
        List<Tag> list = compoundTag.getList("Denizen-Registered-Keys");
        list.add(new StringTag(key));
        compoundTag = compoundTag.createBuilder().put("Denizen-Registered-Keys", new ListTag(StringTag.class, list)).build();
        return NMSHandler.getInstance().getItemHelper().setNbtData(item, compoundTag);
    }

    private static ItemStack unregisterNBT(ItemStack item, String key) {
        if (item == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(item);
        ListTag list = compoundTag.getListTag("Denizen-Registered-Keys");
        if (list.getValue().isEmpty()) {
            return item;
        }
        List<Tag> value = list.getValue();
        String lowerKey = CoreUtilities.toLowerCase(key);
        for (int i = 0; i < list.getValue().size(); i++) {
            if (CoreUtilities.toLowerCase(list.getString(i)).equals(lowerKey)) {
                value.remove(i);
                break;
            }
        }
        list.setValue(value);
        compoundTag = compoundTag.createBuilder().put("Denizen-Registered-Keys", list).build();
        return NMSHandler.getInstance().getItemHelper().setNbtData(item, compoundTag);
    }

    public static List<String> getRegisteredNBT(ItemStack item) {
        if (item == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(item);
        ListTag list = compoundTag.getListTag("Denizen-Registered-Keys");
        if (list.getValue().isEmpty()) {
            return null;
        }
        List<String> ret = new ArrayList<String>();
        for (int i = 0; i < list.getValue().size(); i++) {
            ret.add(list.getString(i));
        }
        return ret;
    }

    public static Entity addCustomNBT(Entity entity, String key, String value) {
        if (entity == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getEntityHelper().getNbtData(entity);

        // Add custom NBT
        compoundTag = compoundTag.createBuilder().putString(key, value).build();

        // Write tag back
        NMSHandler.getInstance().getEntityHelper().setNbtData(entity, compoundTag);
        return entity;
    }

    public static Entity removeCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getEntityHelper().getNbtData(entity);

        // Remove custom NBT
        compoundTag = compoundTag.createBuilder().remove(key).build();

        // Write tag back
        NMSHandler.getInstance().getEntityHelper().setNbtData(entity, compoundTag);
        return entity;
    }

    public static boolean hasCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return false;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getEntityHelper().getNbtData(entity);

        // Check for key
        return compoundTag.getValue().containsKey(key);
    }

    public static String getCustomNBT(Entity entity, String key) {
        if (entity == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getEntityHelper().getNbtData(entity);

        // Return contents of the tag
        return compoundTag.getString(key);
    }
}


