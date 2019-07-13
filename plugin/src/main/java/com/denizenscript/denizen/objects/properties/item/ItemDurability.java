package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.dItem;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class ItemDurability implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof dItem
                && ((dItem) item).isRepairable();
    }

    public static ItemDurability getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemDurability((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "durability", "max_durability"
    };

    public static final String[] handledMechs = new String[] {
            "durability"
    };


    private ItemDurability(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.durability>
        // @returns ElementTag(Number)
        // @mechanism dItem.durability
        // @group properties
        // @description
        // Returns the current durability (number of uses) on the item.
        // -->
        if (attribute.startsWith("durability")) {
            return new ElementTag(item.getItemStack().getDurability())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.max_durability>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum durability (number of uses) of this item.
        // For use with <@link tag i@item.durability> and <@link mechanism dItem.durability>.
        // -->
        if (attribute.startsWith("max_durability")) {
            return new ElementTag(item.getMaterial().getMaterial().getMaxDurability())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.getItemStack().getDurability() != 0) {
            return String.valueOf(item.getItemStack().getDurability());
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "durability";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name durability
        // @input Element(Number)
        // @description
        // Changes the durability of damageable items.
        // @tags
        // <i@item.durability>
        // <i@item.max_durability>
        // <i@item.repairable>
        // -->

        if (mechanism.matches("durability") && mechanism.requireInteger()) {
            item.getItemStack().getData().setData((byte) mechanism.getValue().asInt());
            item.setDurability((short) mechanism.getValue().asInt());
        }
    }
}
