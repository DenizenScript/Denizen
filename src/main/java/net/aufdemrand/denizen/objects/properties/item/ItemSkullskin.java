package net.aufdemrand.denizen.objects.properties.item;


import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Material;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemSkullskin implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getType() == Material.SKULL_ITEM
                && ((dItem) item).getItemStack().getData().getData() == 3;
    }

    public static ItemSkullskin getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemSkullskin((dItem)_item);
    }


    private ItemSkullskin(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.skin>
        // @returns Element
        // @description
        // Returns the name of the player whose skin a skull item uses.
        // Note: Item must be a 'skull_item' with a skin.
        // -->
        if (attribute.startsWith("skin")) {
            if (item.getItemStack().hasItemMeta()
                    && item.getItemStack().getItemMeta() instanceof SkullMeta
                    && ((SkullMeta)item.getItemStack().getItemMeta()).hasOwner())
                return new Element(((SkullMeta)item.getItemStack().getItemMeta()).getOwner())
                    .getAttribute(attribute.fulfill(1));
            else
                dB.echoError("This skull_item does not have a skin set!");
        }

        // <--[tag]
        // @attribute <i@item.has_skin>
        // @returns Element(Boolean)
        // @description
        // Returns whether the item has a custom skin set.
        // (Only for human 'skull_item's)
        // -->
        if (attribute.startsWith("has_skin"))
            return new Element(item.getItemStack().hasItemMeta()
                                && item.getItemStack().getItemMeta() instanceof SkullMeta
                                && ((SkullMeta)item.getItemStack().getItemMeta()).hasOwner())
                    .getAttribute(attribute.fulfill(1));


        return null;
    }


    @Override
    public String getPropertyString() {
        if (item.getItemStack().hasItemMeta()
                && item.getItemStack().getItemMeta() instanceof SkullMeta
                && ((SkullMeta)item.getItemStack().getItemMeta()).hasOwner())
            return ((SkullMeta)item.getItemStack().getItemMeta()).getOwner();
        else
            return null;
    }

    @Override
    public String getPropertyId() {
        return "skull_skin";
    }
}
