package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.nbt.CustomNBT;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * This class is to serve as a placeholder until a better attributes system is available!
 */
public class ItemAttributeNBT implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem && ((dItem) item).getMaterial().getMaterial() != Material.AIR;
    }

    public static ItemAttributeNBT getFrom(dObject item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemAttributeNBT((dItem) item);
        }
    }

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
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns the NBT attribute string, if any.
        // -->
        if (attribute.startsWith("nbt_attributes")) {
            String prop = getPropertyString();
            if (prop != null) {
                return new Element(prop).getAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        ItemStack itemStack = item.getItemStack();
        List<CustomNBT.AttributeReturn> nbtKeys = CustomNBT.getAttributes(itemStack);
        if (nbtKeys != null && !nbtKeys.isEmpty()) {
            dList list = new dList();
            for (CustomNBT.AttributeReturn atr : nbtKeys) {
                list.add(EscapeTags.Escape(atr.attr) + "/" + EscapeTags.Escape(atr.slot) + "/" + atr.op + "/" + atr.amt);
            }
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
        // @input dList
        // @description
        // Sets the Denizen NBT attributes for this item in the format li@attribute/slot/op/amount|...
        // Attribute is text ( http://minecraft.gamepedia.com/Attribute ), slot is the name of the slot,
        // op is the number code for operation, and amount is a decimal.
        // @tags
        // <i@item.nbt_attributes>
        // -->
        if (mechanism.matches("nbt_attributes")) {
            dList list = mechanism.getValue().asType(dList.class);
            ItemStack itemStack = item.getItemStack();
            itemStack = CustomNBT.clearAttributes(itemStack);
            for (String string : list) {
                String[] split = string.split("/");
                String attribute = EscapeTags.unEscape(split[0]);
                String slot = EscapeTags.unEscape(split[1]);
                int op = new Element(split[2]).asInt();
                double amt = new Element(split[3]).asDouble();
                itemStack = CustomNBT.addAttribute(itemStack, attribute, slot, op, amt);
            }
            item.setItemStack(itemStack);
        }
    }
}
