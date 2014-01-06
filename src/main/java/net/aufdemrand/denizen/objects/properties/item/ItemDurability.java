package net.aufdemrand.denizen.objects.properties.item;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;

public class ItemDurability implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).isRepairable();
    }

    public static ItemDurability getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemDurability((dItem)_item);
    }


    private ItemDurability(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.durability>
        // @returns Element(Number)
        // @description
        // Returns the current durability (number of uses) on the item.
        // -->
        if (attribute.startsWith("durability"))
            return new Element(item.getItemStack().getDurability())
                    .getAttribute(attribute.fulfill(1));

        // <--[tag]
        // @attribute <i@item.max_durability>
        // @returns Element(Number)
        // @description
        // Returns the maximum durability (number of uses) of this item.
        // -->
        if (attribute.startsWith("max_durability"))
            return new Element(item.getMaterial().getMaterial().getMaxDurability())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.getItemStack().getDurability() != 0)
            return String.valueOf(item.getItemStack().getDurability());
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "durability";
    }
}
