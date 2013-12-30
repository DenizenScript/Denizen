package net.aufdemrand.denizen.objects.properties.Item;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;

public class ItemDisplayname implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().hasItemMeta()
                && ((dItem) item).getItemStack().getItemMeta().hasDisplayName();
    }

    public static ItemDisplayname getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemDisplayname((dItem)_item);
    }


    private ItemDisplayname(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.display>
        // @returns Element
        // @description
        // Returns the display name of the item, as set by plugin or an anvil.
        // -->
        if (attribute.startsWith("display"))
            return new Element(item.getItemStack().getItemMeta().getDisplayName())
                .getAttribute(attribute.fulfill(1));

        return null;
    }


    @Override
    public String getPropertyString() {
        return ItemBook.Escape(item.getItemStack().getItemMeta().getDisplayName());
    }

    @Override
    public String getPropertyId() {
        return "display_name";
    }
}
