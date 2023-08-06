package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagUtil;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemNBT implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag;
    }

    public static ItemNBT getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemNBT((ItemTag) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_nbt", "nbt_keys", "nbt"
    };

    public static final String[] handledMechs = new String[] {
            "remove_nbt", "nbt"
    };

    public ItemNBT(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        if (attribute.startsWith("has_nbt")) {
            BukkitImplDeprecations.itemNbt.warn(attribute.context);
            return new ElementTag(CustomNBT.hasCustomNBT(item.getItemStack(), attribute.getParam(), CustomNBT.KEY_DENIZEN))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        if (attribute.startsWith("nbt_keys")) {
            BukkitImplDeprecations.itemNbt.warn(attribute.context);
            return new ListTag(CustomNBT.listNBT(item.getItemStack(), CustomNBT.KEY_DENIZEN))
                    .getObjectAttribute(attribute.fulfill(1));
        }

        if (attribute.matches("nbt")) {
            BukkitImplDeprecations.itemNbt.warn(attribute.context);
            if (!attribute.hasParam()) {
                ListTag list = getNBTDataList();
                if (list == null) {
                    return null;
                }
                return list.getObjectAttribute(attribute.fulfill(1));
            }
            String res = CustomNBT.getCustomNBT(item.getItemStack(), attribute.getParam(), CustomNBT.KEY_DENIZEN);
            if (res == null) {
                return null;
            }
            return new ElementTag(res)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getNBTDataList() {
        ItemStack itemStack = item.getItemStack();
        List<String> nbtKeys = CustomNBT.listNBT(itemStack, CustomNBT.KEY_DENIZEN);
        if (nbtKeys != null && !nbtKeys.isEmpty()) {
            ListTag list = new ListTag();
            for (String key : nbtKeys) {
                list.add(EscapeTagUtil.escape(key) + "/" + EscapeTagUtil.escape(CustomNBT.getCustomNBT(itemStack, key, CustomNBT.KEY_DENIZEN)));
            }
            return list;
        }
        return null;
    }

    @Override
    public String getPropertyString() {
        ListTag list = getNBTDataList();
        if (list == null) {
            return null;
        }
        return list.identify();
    }

    @Override
    public String getPropertyId() {
        return "nbt";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        if (mechanism.matches("remove_nbt")) {
            BukkitImplDeprecations.itemNbt.warn(mechanism.context);
            if (item.getMaterial().getMaterial() == Material.AIR) {
                mechanism.echoError("Cannot apply NBT to AIR!");
                return;
            }
            ItemStack itemStack = item.getItemStack();
            List<String> list;
            if (mechanism.hasValue()) {
                list = mechanism.valueAsType(ListTag.class);
            }
            else {
                list = CustomNBT.listNBT(itemStack, CustomNBT.KEY_DENIZEN);
            }
            for (String string : list) {
                itemStack = CustomNBT.removeCustomNBT(itemStack, string, CustomNBT.KEY_DENIZEN);
            }
            item.setItemStack(itemStack);
        }

        if (mechanism.matches("nbt")) {
            BukkitImplDeprecations.itemNbt.warn(mechanism.context);
            if (item.getMaterial().getMaterial() == Material.AIR) {
                mechanism.echoError("Cannot apply NBT to AIR!");
                return;
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            ItemStack itemStack = item.getItemStack();
            for (String string : list) {
                String[] split = string.split("/", 2);
                itemStack = CustomNBT.addCustomNBT(itemStack, EscapeTagUtil.unEscape(split[0]), EscapeTagUtil.unEscape(split[1]), CustomNBT.KEY_DENIZEN);
            }
            item.setItemStack(itemStack);
        }
    }
}
