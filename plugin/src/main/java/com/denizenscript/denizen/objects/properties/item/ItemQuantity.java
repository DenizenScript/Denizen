package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;

public class ItemQuantity implements Property {

    public static boolean describes(ObjectTag item) {
        // all items can have a quantity
        return item instanceof ItemTag;
    }

    public static ItemQuantity getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemQuantity((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "quantity", "qty", "max_stack"
    };

    public static final String[] handledMechs = new String[] {
            "quantity"
    };

    public ItemQuantity(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.quantity>
        // @returns ElementTag(Number)
        // @mechanism ItemTag.quantity
        // @group properties
        // @description
        // Returns the number of items in the ItemTag's itemstack.
        // -->
        if (attribute.startsWith("qty")) {
            BukkitImplDeprecations.qtyTags.warn(attribute.context);
            return new ElementTag(item.getItemStack().getAmount())
                    .getObjectAttribute(attribute.fulfill(1));
        }
        if (attribute.startsWith("quantity")) {
            return new ElementTag(item.getItemStack().getAmount())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.max_stack>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the max number of this item possible in a single stack of this type.
        // For use with <@link tag ItemTag.quantity> and <@link mechanism ItemTag.quantity>.
        // -->
        if (attribute.startsWith("max_stack")) {
            return new ElementTag(item.getItemStack().getMaxStackSize())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public String getPropertyString() {
        if (item.getItemStack().getAmount() > 1) {
            return String.valueOf(item.getItemStack().getAmount());
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "quantity";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name quantity
        // @input ElementTag(Number)
        // @description
        // Changes the number of items in this stack.
        // @tags
        // <ItemTag.quantity>
        // <ItemTag.max_stack>
        // -->
        if (mechanism.matches("quantity") && mechanism.requireInteger()) {
            item.setAmount(mechanism.getValue().asInt());
        }

    }
}
