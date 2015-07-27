package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

public class ItemQuantity implements Property {

    public static boolean describes(dObject item) {
        // all items can have a quantity
        return item instanceof dItem;
    }

    public static ItemQuantity getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemQuantity((dItem) _item);
    }


    private ItemQuantity(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.quantity>
        // @returns Element(Number)
        // @mechanism dItem.quantity
        // @group properties
        // @description
        // Returns the number of items in the dItem's itemstack.
        // -->
        if (attribute.startsWith("quantity") || attribute.startsWith("qty"))
            return new Element(item.getItemStack().getAmount())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.max_stack>
        // @returns Element(Number)
        // @group properties
        // @description
        // Returns the max number of this item possible in a single stack of this type.
        // For use with <@link tag i@item.quantity> and <@link mechanism dItem.quantity>.
        // -->
        if (attribute.startsWith("max_stack"))
            return new Element(item.getItemStack().getMaxStackSize())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.getItemStack().getAmount() > 1)
            return String.valueOf(item.getItemStack().getAmount());
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "quantity";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name quantity
        // @input Element(Number)
        // @description
        // Changes the number of items in this stack.
        // @tags
        // <i@item.quantity>
        // <i@item.max_stack>
        // -->

        if (mechanism.matches("quantity") && mechanism.requireInteger()) {
            item.setAmount(mechanism.getValue().asInt());
        }

    }
}
