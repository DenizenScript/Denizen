package net.aufdemrand.denizen.objects.properties.Item;


import net.aufdemrand.denizen.objects.dColor;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.DyeColor;
import org.bukkit.material.Colorable;

public class ItemColor implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack() instanceof Colorable;
    }

    public static ItemColor getFrom(dItem item) {
        if (!describes(item)) return null;
        else return new ItemColor(item);
    }


    private ItemColor(dItem _item) {
        item = _item;
        color = getColor();
    }

    dItem item;
    dColor color;

    public dColor getColor() {
        if (((Colorable)item).getColor() != DyeColor.WHITE)
            return new dColor(((Colorable) item).getColor());
        else
            return null;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        return "null";
    }


    @Override
    public String getPropertyString() {
        return color.identify();
    }

    @Override
    public String getPropertyId() {
        return "color";
    }
}
