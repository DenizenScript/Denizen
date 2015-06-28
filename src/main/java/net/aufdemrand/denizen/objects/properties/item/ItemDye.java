package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dColor;
import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Material;
import org.bukkit.inventory.meta.LeatherArmorMeta;

public class ItemDye implements Property {

    public static boolean describes(dObject item) {
        // Leather armor only, the rest is handled by dMaterials
        return item instanceof dItem
                && (((dItem) item).getItemStack().getType() == Material.LEATHER_BOOTS
                || ((dItem) item).getItemStack().getType() == Material.LEATHER_CHESTPLATE
                || ((dItem) item).getItemStack().getType() == Material.LEATHER_HELMET
                || ((dItem) item).getItemStack().getType() == Material.LEATHER_LEGGINGS);
    }

    public static ItemDye getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemDye((dItem) _item);
    }

    private ItemDye(dItem _item) {
        item = _item;
    }

    dItem item;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.dye_color>
        // @returns dColor
        // @mechanism dItem.dye
        // @group properties
        // @description
        // Returns the color of the leather armor item.
        // -->
        if (attribute.startsWith("dye_color")) {
            return new dColor(((LeatherArmorMeta) item.getItemStack().getItemMeta()).getColor()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        return new dColor(((LeatherArmorMeta) item.getItemStack().getItemMeta()).getColor()).identify();
    }

    @Override
    public String getPropertyId() {
        return "dye";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name dye
        // @input dColor
        // @description
        // Sets the leather armor item's dye color in the format RED,GREEN,BLUE
        // See <@link language Property Escaping>
        // @tags
        // <i@item.lore>
        // -->
        if (mechanism.matches("dye") && mechanism.requireObject(dColor.class)) {
            dColor color = mechanism.getValue().asType(dColor.class);
            LeatherArmorMeta meta = ((LeatherArmorMeta) item.getItemStack().getItemMeta());
            meta.setColor(color.getColor());
            item.getItemStack().setItemMeta(meta);
        }
    }
}
