package net.aufdemrand.denizen.utilities.nbt;

import net.aufdemrand.denizen.nms.NMSHandler;
import net.aufdemrand.denizen.nms.util.jnbt.CompoundTag;
import net.aufdemrand.denizen.nms.util.jnbt.Tag;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomNBT {

    public static MapOfEnchantments getEnchantments(ItemStack item) {
        return new MapOfEnchantments(item);
    }

    /*
     * Some static methods for dealing with Minecraft NBT data, which is used to store
     * custom NBT.
     */

    public static ItemStack addCustomNBT(ItemStack itemStack, String key, String value) {
        if (itemStack == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey("Denizen NBT")) {
            denizenTag = (CompoundTag) compoundTag.getValue().get("Denizen NBT");
        }
        else {
            denizenTag = NMSHandler.getInstance().createCompoundTag(new HashMap<String, Tag>());
        }

        // Add custom NBT
        denizenTag = denizenTag.createBuilder().putString(CoreUtilities.toLowerCase(key), value).build();

        compoundTag = compoundTag.createBuilder().put("Denizen NBT", denizenTag).build();

        // Write tag back
        return NMSHandler.getInstance().getItemHelper().setNbtData(itemStack, compoundTag);
    }

    public static ItemStack removeCustomNBT(ItemStack itemStack, String key) {
        if (itemStack == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey("Denizen NBT")) {
            denizenTag = (CompoundTag) compoundTag.getValue().get("Denizen NBT");
        }
        else {
            return itemStack;
        }

        // Remove custom NBT
        denizenTag = denizenTag.createBuilder().remove(CoreUtilities.toLowerCase(key)).build();

        compoundTag = compoundTag.createBuilder().put("Denizen NBT", denizenTag).build();

        // Write tag back
        return NMSHandler.getInstance().getItemHelper().setNbtData(itemStack, compoundTag);
    }

    public static boolean hasCustomNBT(ItemStack itemStack, String key) {
        if (itemStack == null) {
            return false;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey("Denizen NBT")) {
            denizenTag = (CompoundTag) compoundTag.getValue().get("Denizen NBT");
        }
        else {
            return false;
        }

        return denizenTag.getValue().containsKey(CoreUtilities.toLowerCase(key));
    }

    public static String getCustomNBT(ItemStack itemStack, String key) {
        if (itemStack == null || key == null) {
            return null;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey("Denizen NBT")) {
            denizenTag = (CompoundTag) compoundTag.getValue().get("Denizen NBT");
        }
        else {
            return null;
        }

        return denizenTag.getString(CoreUtilities.toLowerCase(key));
    }

    public static List<String> listNBT(ItemStack itemStack) {
        List<String> nbt = new ArrayList<String>();
        if (itemStack == null) {
            return nbt;
        }
        CompoundTag compoundTag = NMSHandler.getInstance().getItemHelper().getNbtData(itemStack);

        CompoundTag denizenTag;
        if (compoundTag.getValue().containsKey("Denizen NBT")) {
            denizenTag = (CompoundTag) compoundTag.getValue().get("Denizen NBT");
        }
        else {
            return nbt;
        }
        nbt.addAll(denizenTag.getValue().keySet());

        return nbt;
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


