package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

public class ItemRepairCost implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getItemMeta() instanceof Repairable;
    }

    public static ItemRepairCost getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemRepairCost((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
           "repair_cost"
    };

    public static final String[] handledMechs = new String[] {
            "repair_cost"
    };


    private ItemRepairCost(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.repair_cost>
        // @returns ElementTag(Number)
        // @mechanism dItem.repair_cost
        // @group properties
        // @description
        // Returns the current repair cost (on an anvil) for this item.
        // Note that zero indicates no repair cost.
        // -->
        if (attribute.startsWith("repair_cost")) {
            return new ElementTag(((Repairable) item.getItemStack().getItemMeta()).getRepairCost())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        int cost = ((Repairable) item.getItemStack().getItemMeta()).getRepairCost();
        if (cost != 0) {
            return String.valueOf(cost);
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "repair_cost";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name repair_cost
        // @input Element(Number)
        // @description
        // Changes the repair cost (on an anvil) of the item.
        // @tags
        // <i@item.repair_cost>
        // -->
        if (mechanism.matches("repair_cost") && mechanism.requireInteger()) {
            Repairable meta = ((Repairable) item.getItemStack().getItemMeta());
            meta.setRepairCost(mechanism.getValue().asInt());
            item.getItemStack().setItemMeta((ItemMeta) meta);
        }
    }
}
