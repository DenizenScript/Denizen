package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Material;

public class ItemMap implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && (((dItem) item).getItemStack().getType() == Material.MAP);
    }

    public static ItemMap getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemMap((dItem) _item);
    }


    private ItemMap(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.map>
        // @returns Element(Number)
        // @group properties
        // @mechanism dItem.map
        // @description
        // Returns the ID number of the map item's map.
        // -->
        if (attribute.startsWith("map")) {
            return new Element(item.getItemStack().getDurability())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        return String.valueOf(item.getItemStack().getDurability());
    }

    @Override
    public String getPropertyId() {
        return "map";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name map
        // @input Element(Number)
        // @description
        // Changes what map ID number a map item uses.
        // @tags
        // <i@item.map>
        // -->

        if (mechanism.matches("map") && mechanism.requireInteger()) {
            item.getItemStack().setDurability((short) (mechanism.getValue().asInt()));
        }
    }
}
