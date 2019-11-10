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

/**
 * This class is to serve as a placeholder until a better attributes system is available!
 */
public class ItemAttributeNBT implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag;
    }

    public static ItemAttributeNBT getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemAttributeNBT((ItemTag) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "nbt_attributes"
    };

    public static final String[] handledMechs = new String[] {
            "nbt_attributes"
    };

    private ItemAttributeNBT(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.nbt_attributes>
        // @returns ListTag
        // @group properties
        // @mechanism ItemTag.nbt_attributes
        // @description
        // Returns the NBT attribute data (as matches the mechanism input), if any.
        // -->
        if (attribute.startsWith("nbt_attributes")) {
            return getList().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public ListTag getList() {
        ItemStack itemStack = item.getItemStack();
        List<CustomNBT.AttributeReturn> nbtKeys = CustomNBT.getAttributes(itemStack);
        ListTag list = new ListTag();
        if (nbtKeys != null) {
            for (CustomNBT.AttributeReturn atr : nbtKeys) {
                list.add(EscapeTagBase.escape(atr.attr) + "/" + EscapeTagBase.escape(atr.slot) + "/" + atr.op + "/" + atr.amt);
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
        // @object ItemTag
        // @name nbt_attributes
        // @input ListTag
        // @description
        // Sets the Denizen NBT attributes for this item in the format attribute/slot/op/amount|...
        // Attribute is text (<@link url http://minecraft.gamepedia.com/Attribute>), slot is the name of the slot,
        // op is the number code for operation, and amount is a decimal.
        // @tags
        // <ItemTag.nbt_attributes>
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
                String attribute = EscapeTagBase.unEscape(split[0]);
                String slot = EscapeTagBase.unEscape(split[1]);
                int op = new ElementTag(split[2]).asInt();
                double amt = new ElementTag(split[3]).asDouble();
                itemStack = CustomNBT.addAttribute(itemStack, attribute, slot, op, amt);
            }
            item.setItemStack(itemStack);
        }
    }
}
