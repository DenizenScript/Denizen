package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
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
        return item instanceof dItem && ((dItem) item).getMaterial().getMaterial() != Material.AIR;
    }

    public static ItemNBT getFrom(dObject item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemNBT((dItem) item);
        }
    }

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
        // @group properties
        // @description
        // Returns the value of this item's NBT key as a string Element as best it can.
        // -->
        if (attribute.matches("nbt")) {
            return new Element(CustomNBT.getCustomNBT(item.getItemStack(), attribute.getContext(1), CustomNBT.KEY_DENIZEN))
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        ItemStack itemStack = item.getItemStack();
        List<String> nbtKeys = CustomNBT.listNBT(itemStack, CustomNBT.KEY_DENIZEN);
        if (nbtKeys != null && !nbtKeys.isEmpty()) {
            dList list = new dList();
            for (String key : nbtKeys) {
                list.add(key + "/" + CustomNBT.getCustomNBT(itemStack, key, CustomNBT.KEY_DENIZEN));
            }
            return list.identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "nbt";
    }

    @Override
    public void adjust(Mechanism mechanism) {

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
