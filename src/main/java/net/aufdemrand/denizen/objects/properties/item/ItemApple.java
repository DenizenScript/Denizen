package net.aufdemrand.denizen.objects.properties.item;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.CropState;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.NetherWarts;

public class ItemApple implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && (((dItem) item).getItemStack().getType() == Material.GOLDEN_APPLE);
    }

    public static ItemApple getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemApple((dItem)_item);
    }


    private ItemApple(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.apple_enchanted>
        // @returns Element
        // @mechanism dItem.apple_enchanted
        // @description
        // Returns whether a golden apple item is enchanted.
        // -->
        if (attribute.startsWith("apple_enchanted")) {
            return new Element(item.getItemStack().getDurability() == 1)
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.getItemStack().getDurability() == 1)
            return "true";
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "apple_enchanted";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name apple_enchanted
        // @input Element(Boolean)
        // @description
        // Changes whether a golden apple is enchanted.
        // @tags
        // <i@item.apple_enchanted>
        // -->

        if (mechanism.matches("apple_enchanted")) {
            item.getItemStack().setDurability((short)(mechanism.getValue().asBoolean() ? 1: 0));
        }
    }
}
