package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBaseColor implements Property {

    public static boolean describes(dObject item) {
        if (item instanceof dItem) {
            Material material = ((dItem) item).getItemStack().getType();
            return material == Material.BANNER
                    || material == Material.WALL_BANNER
                    || material == Material.STANDING_BANNER
                    || material == Material.SHIELD;
        }
        return false;
    }

    public static ItemBaseColor getFrom(dObject item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemBaseColor((dItem) item);
        }
    }


    private ItemBaseColor(dItem item) {
        this.item = item;
    }

    dItem item;

    private DyeColor getBaseColor() {
        ItemMeta itemMeta = item.getItemStack().getItemMeta();
        if (itemMeta instanceof BlockStateMeta) {
            return ((Banner) ((BlockStateMeta) itemMeta).getBlockState()).getBaseColor();
        }
        else {
            return ((BannerMeta) itemMeta).getBaseColor();
        }
    }

    private void setBaseColor(DyeColor color) {
        ItemStack itemStack = item.getItemStack();
        ItemMeta itemMeta = itemStack.getItemMeta();
        if (itemMeta instanceof BlockStateMeta) {
            Banner banner = (Banner) ((BlockStateMeta) itemMeta).getBlockState();
            banner.setBaseColor(color);
            ((BlockStateMeta) itemMeta).setBlockState(banner);
        }
        else {
            ((BannerMeta) itemMeta).setBaseColor(color);
        }
        itemStack.setItemMeta(itemMeta);
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

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
            DyeColor baseColor = getBaseColor();
            if (baseColor != null) {
                return new Element(baseColor.name()).getAttribute(attribute.fulfill(1));
            }
            return null;
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        DyeColor baseColor = getBaseColor();
        if (baseColor != null) {
            return baseColor.name();
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
            setBaseColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
