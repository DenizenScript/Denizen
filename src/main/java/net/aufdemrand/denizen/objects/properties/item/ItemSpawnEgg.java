package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.Material;

public class ItemSpawnEgg implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && (((dItem) item).getItemStack().getType() == Material.MONSTER_EGG);
    }

    public static ItemSpawnEgg getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemSpawnEgg((dItem)_item);
    }


    private ItemSpawnEgg(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.spawn_egg_entity>
        // @returns Element
        // @group properties
        // @mechanism dItem.spawn_egg
        // @description
        // Returns the spawn egg number of the item.
        // -->
        if (attribute.startsWith("spawn_egg_entity")) {
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
        return "spawn_egg";
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

        if (mechanism.matches("spawn_egg")) {
            item.getItemStack().setDurability((short)(mechanism.getValue().asInt()));
        }
    }
}
