package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBaseColor implements Property {

    public static boolean describes(ObjectTag item) {
        if (item instanceof ItemTag) {
            return ((ItemTag) item).getItemStack().getType() == Material.SHIELD;
        }
        return false;
    }

    public static ItemBaseColor getFrom(ObjectTag item) {
        if (!describes(item)) {
            return null;
        }
        else {
            return new ItemBaseColor((ItemTag) item);
        }
    }

    public static final String[] handledTags = new String[] {
            "base_color"
    };

    public static final String[] handledMechs = new String[] {
            "base_color"
    };

    private ItemBaseColor(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    private DyeColor getBaseColor() {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof BlockStateMeta) {
            if (item.getItemStack().getType() == Material.SHIELD) { // Hack to avoid blank shields misdisplaying as white
                if (!itemMeta.serialize().containsKey("internal")) {
                    return null;
                }
            }
            return ((Banner) ((BlockStateMeta) itemMeta).getBlockState()).getBaseColor();
        }
        else {
            return ((BannerMeta) itemMeta).getBaseColor();
        }
    }

    private void setBaseColor(DyeColor color) {
        ItemMeta itemMeta = item.getItemMeta();
        if (itemMeta instanceof BlockStateMeta) {
            Banner banner = (Banner) ((BlockStateMeta) itemMeta).getBlockState();
            banner.setBaseColor(color);
            banner.update();
            ((BlockStateMeta) itemMeta).setBlockState(banner);
        }
        else {
            ((BannerMeta) itemMeta).setBaseColor(color);
        }
        item.setItemMeta(itemMeta);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.base_color>
        // @returns ElementTag
        // @group properties
        // @mechanism ItemTag.base_color
        // @description
        // Gets the base color of a shield.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // -->
        if (attribute.startsWith("base_color")) {
            DyeColor baseColor = getBaseColor();
            if (baseColor != null) {
                return new ElementTag(baseColor.name()).getObjectAttribute(attribute.fulfill(1));
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
        // @object ItemTag
        // @name base_color
        // @input ElementTag
        // @description
        // Changes the base color of a shield.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // @tags
        // <ItemTag.base_color>
        // -->
        if (mechanism.matches("base_color")) {
            setBaseColor(DyeColor.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
