package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTagBase;
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


    private ItemNBT(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.has_nbt[<key>]>
        // @returns ElementTag(Boolean)
        // @group properties
        // @description
        // Returns whether this item has the specified NBT key.
        // -->
        if (attribute.startsWith("has_nbt")) {
            return new ElementTag(CustomNBT.hasCustomNBT(item.getItemStack(), attribute.getContext(1), CustomNBT.KEY_DENIZEN))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.nbt_keys>
        // @returns ListTag
        // @group properties
        // @description
        // Returns all of this item's NBT keys as a ListTag.
        // -->
        if (attribute.startsWith("nbt_keys")) {
            return new ListTag(CustomNBT.listNBT(item.getItemStack(), CustomNBT.KEY_DENIZEN))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.nbt[<key>]>
        // @returns ElementTag
        // @mechanism ItemTag.nbt
        // @group properties
        // @description
        // Returns the value of this item's NBT key as an ElementTag as best it can.
        // If no key is specified, returns the full list of NBT key/value pairs (valid for input to nbt mechanism).
        // See also <@link language property escaping>.
        // -->
        if (attribute.matches("nbt")) {
            if (!attribute.hasContext(1)) {
                ListTag list = getNBTDataList();
                if (list == null) {
                    return null;
                }
                return list.getAttribute(attribute.fulfill(1));
            }
            String res = CustomNBT.getCustomNBT(item.getItemStack(), attribute.getContext(1), CustomNBT.KEY_DENIZEN);
            if (res == null) {
                return null;
            }
            return new ElementTag(res)
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getNBTDataList() {
        ItemStack itemStack = item.getItemStack();
        List<String> nbtKeys = CustomNBT.listNBT(itemStack, CustomNBT.KEY_DENIZEN);
        if (nbtKeys != null && !nbtKeys.isEmpty()) {
            ListTag list = new ListTag();
            for (String key : nbtKeys) {
                list.add(EscapeTagBase.escape(key) + "/" + EscapeTagBase.escape(CustomNBT.getCustomNBT(itemStack, key, CustomNBT.KEY_DENIZEN)));
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

        // <--[mechanism]
        // @object ItemTag
        // @name remove_nbt
        // @input ListTag
        // @description
        // Removes the Denizen NBT keys specified, or all Denizen NBT if no value is given.
        // @tags
        // <ItemTag.has_nbt[<key>]>
        // <ItemTag.nbt_keys>
        // <ItemTag.nbt[<key>]>
        // -->
        if (mechanism.matches("remove_nbt")) {
            if (item.getMaterial().getMaterial() == Material.AIR) {
                Debug.echoError("Cannot apply NBT to AIR!");
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

        // <--[mechanism]
        // @object ItemTag
        // @name nbt
        // @input ListTag
        // @description
        // Sets the Denizen NBT for this item in the format li@key/value|key/value...
        // See also <@link language property escaping>.
        // @tags
        // <ItemTag.has_nbt[<key>]>
        // <ItemTag.nbt_keys>
        // <ItemTag.nbt[<key>]>
        // -->
        if (mechanism.matches("nbt")) {
            if (item.getMaterial().getMaterial() == Material.AIR) {
                Debug.echoError("Cannot apply NBT to AIR!");
                return;
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            ItemStack itemStack = item.getItemStack();
            for (String string : list) {
                String[] split = string.split("/", 2);
                itemStack = CustomNBT.addCustomNBT(itemStack, EscapeTagBase.unEscape(split[0]), EscapeTagBase.unEscape(split[1]), CustomNBT.KEY_DENIZEN);
            }
            item.setItemStack(itemStack);
        }
    }
}
