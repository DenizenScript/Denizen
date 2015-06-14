package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.inventory.meta.BannerMeta;

public class ItemBaseColor implements Property {

    public static boolean describes(dObject item) {
        if (item instanceof dItem) {
            Material material = ((dItem) item).getItemStack().getType();
            return material == Material.BANNER
                    || material == Material.WALL_BANNER
                    || material == Material.STANDING_BANNER;
        }
        return false;
    }

    public static ItemBaseColor getFrom(dObject item) {
        if (!describes(item)) return null;
        else return new ItemBaseColor((dItem) item);
    }


    private ItemBaseColor(dItem item) {
        this.item = item;
    }

    dItem item;

    private Element getBaseColor() {
        DyeColor baseColor = ((BannerMeta) item.getItemStack().getItemMeta()).getBaseColor();
        if (baseColor != null) {
            return new Element(baseColor.name());
        }
        return null;
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.base_color>
        // @returns Element
        // @group properties
        // @mechanism dItem.base_color
        // @description
        // Gets the base color of a banner.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // -->
        if (attribute.startsWith("base_color")) {
            Element baseColor = getBaseColor();
            if (baseColor != null) {
                return getBaseColor().getAttribute(attribute.fulfill(1));
            }
            return new Element("BLACK").getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        Element baseColor = getBaseColor();
        if (baseColor != null) {
            return getBaseColor().identify();
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "base_color";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name base_color
        // @input Element
        // @description
        // Changes the base color of a banner.
        // For the list of possible colors, see <@link url http://bit.ly/1dydq12>.
        // @tags
        // <i@item.base_color>
        // -->

        if (mechanism.matches("base_color")) {
            BannerMeta bannerMeta = (BannerMeta) item.getItemStack().getItemMeta();
            bannerMeta.setBaseColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
            item.getItemStack().setItemMeta(bannerMeta);
        }
    }
}
