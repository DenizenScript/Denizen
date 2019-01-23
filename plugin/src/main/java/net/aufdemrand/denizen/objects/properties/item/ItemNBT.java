package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class ItemNBT implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem;
    }

    public static ItemNBT getFrom(dObject item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemNBT((dItem) item);
        }
    }

    public static final String[] handledTags = new String[]{
            "has_nbt", "nbt_keys", "nbt"
    };

    public static final String[] handledMechs = new String[] {
            "remove_nbt", "nbt"
    };


    private ItemNBT(dItem item) {
        this.item = item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.has_nbt[<key>]>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether this item has the specified NBT key.
        // -->
        if (attribute.startsWith("has_nbt")) {
            return new Element(CustomNBT.hasCustomNBT(item.getItemStack(), attribute.getContext(1), CustomNBT.KEY_DENIZEN))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.nbt_keys>
        // @returns dList
        // @group properties
        // @description
        // Returns all of this item's NBT keys as a dList.
        // -->
        if (attribute.startsWith("nbt_keys")) {
            return new dList(CustomNBT.listNBT(item.getItemStack(), CustomNBT.KEY_DENIZEN))
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.nbt[<key>]>
        // @returns Element
        // @mechanism dItem.nbt
        // @group properties
        // @description
        // Returns the value of this item's NBT key as an Element as best it can.
        // If no key is specified, returns the full list of NBT key/value pairs (valid for input to nbt mechanism).
        // -->
        if (attribute.matches("nbt")) {
            if (!attribute.hasContext(1)) {
                dList list = getNBTDataList();
                if (list == null) {
                    return null;
                }
                return list.getAttribute(attribute.fulfill(1));
            }
            String res = CustomNBT.getCustomNBT(item.getItemStack(), attribute.getContext(1), CustomNBT.KEY_DENIZEN);
            if (res == null) {
                return null;
            }
            return new Element(res)
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public dList getNBTDataList() {
        ItemStack itemStack = item.getItemStack();
        List<String> nbtKeys = CustomNBT.listNBT(itemStack, CustomNBT.KEY_DENIZEN);
        if (nbtKeys != null && !nbtKeys.isEmpty()) {
            dList list = new dList();
            for (String key : nbtKeys) {
                list.add(key + "/" + CustomNBT.getCustomNBT(itemStack, key, CustomNBT.KEY_DENIZEN));
            }
            return list;
        }
        return null;
    }

    @Override
    public String getPropertyString() {
        dList list = getNBTDataList();
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
        // @object dItem
        // @name remove_nbt
        // @input dList
        // @description
        // Removes the Denizen NBT keys specified, or all Denizen NBT if no value is given.
        // @tags
        // <i@item.has_nbt[<key>]>
        // <i@item.nbt_keys>
        // <i@item.nbt[<key>]>
        // -->
        if (mechanism.matches("remove_nbt")) {
            if (item.getMaterial().getMaterial() == Material.AIR) {
                dB.echoError("Cannot apply NBT to AIR!");
                return;
            }
            ItemStack itemStack = item.getItemStack();
            List<String> list;
            if (mechanism.hasValue()) {
                list = mechanism.getValue().asType(dList.class);
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
        // @object dItem
        // @name nbt
        // @input dList
        // @description
        // Sets the Denizen NBT for this item in the format li@key/value|key/value...
        // @tags
        // <i@item.has_nbt[<key>]>
        // <i@item.nbt_keys>
        // <i@item.nbt[<key>]>
        // -->
        if (mechanism.matches("nbt")) {
            if (item.getMaterial().getMaterial() == Material.AIR) {
                dB.echoError("Cannot apply NBT to AIR!");
                return;
            }
            dList list = mechanism.getValue().asType(dList.class);
            ItemStack itemStack = item.getItemStack();
            for (String string : list) {
                String[] split = string.split("/", 2);
                itemStack = CustomNBT.addCustomNBT(itemStack, split[0], split[1], CustomNBT.KEY_DENIZEN);
            }
            item.setItemStack(itemStack);
        }
    }
}
