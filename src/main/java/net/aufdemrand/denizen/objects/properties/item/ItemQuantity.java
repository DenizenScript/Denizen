package net.aufdemrand.denizen.objects.properties.item;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;

public class ItemQuantity implements Property {

    public static boolean describes(dObject item) {
        // all items can have a quantity
        return item instanceof dItem;
    }

    public static ItemQuantity getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemQuantity((dItem)_item);
    }


    private ItemQuantity(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.qty>
        // @returns Element(Number)
        // @description
        // Returns the number of items in the dItem's itemstack.
        // -->
        if (attribute.startsWith("qty"))
            return new Element(item.getItemStack().getAmount())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.max_stack>
        // @returns Element(Number)
        // @description
        // Returns the max number of this item possible in a single stack of this type.
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
        // <i@item.qty>
        // <i@item.max_stack>
        // -->

        if (mechanism.matches("quantity") && mechanism.requireInteger()) {
            item.setAmount(mechanism.getValue().asInt());
        }

    }
}
