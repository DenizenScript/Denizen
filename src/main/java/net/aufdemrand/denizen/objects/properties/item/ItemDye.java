package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Color;
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
        else return new ItemDye((dItem)_item);
    }

    private ItemDye(dItem _item) {
        item = _item;
    }

    dItem item;

    public String GetColor() {
        Color c = ((LeatherArmorMeta) item.getItemStack().getItemMeta()).getColor();
        return c.getRed() + "," + c.getGreen() + "," + c.getBlue();
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.dye_color>
        // @returns dList
        // @mechanism dItem.dye
        // @group properties
        // @description
        // Returns the color of the leather armor item.
        // -->
        if (attribute.startsWith("dye_color")) {
            return new Element(GetColor()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        return GetColor();
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
        // @input Element
        // @description
        // Sets the leather armor item's dye color in the format RED,GREEN,BLUE
        // See <@link language Property Escaping>
        // @tags
        // <i@item.lore>
        // -->

        if (mechanism.matches("dye")) {
            String[] colors = mechanism.getValue().asString().split(",");
            if (colors.length != 3) {
                dB.echoError("Invalid color '" + mechanism.getValue().asString() + "'");
                return;
            }
            Element red = new Element(colors[0]);
            Element green = new Element(colors[1]);
            Element blue = new Element(colors[2]);
            LeatherArmorMeta meta = ((LeatherArmorMeta) item.getItemStack().getItemMeta());
            meta.setColor(Color.fromRGB(red.asInt(), green.asInt(), blue.asInt()));
            item.getItemStack().setItemMeta(meta);
        }
    }
}
