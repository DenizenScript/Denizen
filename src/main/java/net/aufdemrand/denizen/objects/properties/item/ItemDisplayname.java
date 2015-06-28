package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.tags.core.EscapeTags;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemDisplayname implements Property {

    public static boolean describes(dObject item) {
        // Technically, all items can have a display name
        return item instanceof dItem;
    }

    public static ItemDisplayname getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemDisplayname((dItem) _item);
    }

    private ItemDisplayname(dItem _item) {
        item = _item;
    }

    dItem item;

    public boolean hasDisplayName() {
        return item.getItemStack().hasItemMeta()
                && item.getItemStack().getItemMeta().hasDisplayName();
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.display>
        // @returns Element
        // @mechanism dItem.display_name
        // @group properties
        // @description
        // Returns the display name of the item, as set by plugin or an anvil.
        // -->
        if (attribute.startsWith("display")) {
            if (hasDisplayName()) {
                return new Element(item.getItemStack().getItemMeta().getDisplayName())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <i@item.has_display>
        // @returns Element(Boolean)
        // @mechanism dItem.display_name
        // @group properties
        // @description
        // Returns whether the item has a custom set display name.
        // -->
        if (attribute.startsWith("has_display")) {
            return new Element(hasDisplayName())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        if (hasDisplayName()) {
            return EscapeTags.Escape(item.getItemStack().getItemMeta().getDisplayName());
        }
        else {
            return null;
        }
    }

    @Override
    public String getPropertyId() {
        return "display_name";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name display_name
        // @input Element
        // @description
        // Changes the items display name.
        // See <@link language Property Escaping>
        // @tags
        // <i@item.display>
        // -->

        if (mechanism.matches("display_name")) {
            ItemMeta meta = item.getItemStack().getItemMeta();
            meta.setDisplayName(EscapeTags.unEscape(mechanism.getValue().asString()));
            item.getItemStack().setItemMeta(meta);
        }
    }
}
