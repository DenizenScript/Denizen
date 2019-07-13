package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.nbt.CustomNBT;
import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.core.EscapeTags;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This class is to serve as a placeholder until a better attributes system is available!
 */
public class ItemAttributeNBT implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof dItem;
    }

    public static ItemAttributeNBT getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemAttributeNBT((dItem) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "nbt_attributes"
    };

    public static final String[] handledMechs = new String[] {
            "nbt_attributes"
    };

    private ItemAttributeNBT(dItem item) {
        this.item = item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.nbt_attributes>
        // @returns ListTag
        // @group properties
        // @mechanism dItem.nbt_attributes
        // @description
        // Returns the NBT attribute data (as matches the mechanism input), if any.
        // -->
        if (attribute.startsWith("nbt_attributes")) {
            return getList().getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getList() {
        ItemStack itemStack = item.getItemStack();
        List<CustomNBT.AttributeReturn> nbtKeys = CustomNBT.getAttributes(itemStack);
        ListTag list = new ListTag();
        if (nbtKeys != null) {
            for (CustomNBT.AttributeReturn atr : nbtKeys) {
                list.add(EscapeTags.escape(atr.attr) + "/" + EscapeTags.escape(atr.slot) + "/" + atr.op + "/" + atr.amt);
            }
        }
        return list;
    }

    @Override
    public String getPropertyString() {
        ListTag list = getList();
        if (!list.isEmpty()) {
            return list.identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "nbt_attributes";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name nbt_attributes
        // @input ListTag
        // @description
        // Sets the Denizen NBT attributes for this item in the format li@attribute/slot/op/amount|...
        // Attribute is text ( http://minecraft.gamepedia.com/Attribute ), slot is the name of the slot,
        // op is the number code for operation, and amount is a decimal.
        // @tags
        // <i@item.nbt_attributes>
        // -->
        if (mechanism.matches("nbt_attributes")) {
            if (item.getMaterial().getMaterial() == Material.AIR) {
                Debug.echoError("Cannot apply NBT to AIR!");
                return;
            }
            ListTag list = mechanism.valueAsType(ListTag.class);
            ItemStack itemStack = item.getItemStack();
            itemStack = CustomNBT.clearNBT(itemStack, CustomNBT.KEY_ATTRIBUTES);
            for (String string : list) {
                String[] split = string.split("/");
                String attribute = EscapeTags.unEscape(split[0]);
                String slot = EscapeTags.unEscape(split[1]);
                int op = new ElementTag(split[2]).asInt();
                double amt = new ElementTag(split[3]).asDouble();
                itemStack = CustomNBT.addAttribute(itemStack, attribute, slot, op, amt);
            }
            item.setItemStack(itemStack);
        }
    }
}
