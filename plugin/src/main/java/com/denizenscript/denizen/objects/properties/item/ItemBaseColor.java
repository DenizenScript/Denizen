package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.TagContext;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBaseColor extends ItemProperty {

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() == Material.SHIELD;
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

    public DyeColor getBaseColor() {
        ItemMeta itemMeta = getItemMeta();
        if (itemMeta instanceof BlockStateMeta) {
            DyeColor color = ((Banner) ((BlockStateMeta) itemMeta).getBlockState()).getBaseColor();
            if (color == DyeColor.WHITE && getMaterial() == Material.SHIELD) { // Hack to avoid blank shields misdisplaying as white
                if (new ItemRawNBT(object).getFullNBTMap().getObject("BlockEntityTag") == null) {
                    return null;
                }
            }
            return color;
        }
        else {
            return ((BannerMeta) itemMeta).getBaseColor();
        }
    }

    public void setBaseColor(DyeColor color, TagContext context) {
        if (color == null && getMaterial() == Material.SHIELD) {
            ItemRawNBT property = ItemRawNBT.getFrom(object);
            MapTag nbt = property.getFullNBTMap();
            nbt.putObject("BlockEntityTag", null);
            property.setFullNBT(object, nbt, context, false);
            return;
        }
        ItemMeta itemMeta = getItemMeta();
        if (itemMeta instanceof BlockStateMeta) {
            Banner banner = (Banner) ((BlockStateMeta) itemMeta).getBlockState();
            banner.setBaseColor(color);
            banner.update();
            ((BlockStateMeta) itemMeta).setBlockState(banner);
        }
        else {
            ((BannerMeta) itemMeta).setBaseColor(color);
        }
        setItemMeta(itemMeta);
    }

    public static void register() {

        // <--[tag]
        // @attribute <ItemTag.base_color>
        // @returns ElementTag
        // @group properties
        // @mechanism ItemTag.base_color
        // @description
        // Gets the name of the base color of a shield.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // -->
        PropertyParser.registerTag(ItemBaseColor.class, ElementTag.class, "base_color", (attribute, prop) -> {
            DyeColor baseColor = prop.getBaseColor();
            if (baseColor == null) {
                return null;
            }
            return new ElementTag(baseColor);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name base_color
        // @input ElementTag
        // @description
        // Changes the base color of a shield.
        // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
        // Give no input with a shield to remove the base color (and any patterns).
        // @tags
        // <ItemTag.base_color>
        // -->
        PropertyParser.registerMechanism(ItemBaseColor.class, ElementTag.class, "base_color", (prop, mechanism, param) -> {
            prop.setBaseColor(mechanism.hasValue() ? DyeColor.valueOf(param.asString().toUpperCase()) : null, mechanism.context);
        });
    }
}
