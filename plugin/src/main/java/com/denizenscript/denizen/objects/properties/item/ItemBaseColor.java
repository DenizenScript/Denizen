package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.tags.TagContext;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Banner;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemBaseColor extends ItemProperty<ElementTag> {

    // <--[property]
    // @object ItemTag
    // @name base_color
    // @input ElementTag
    // @description
    // Controls the base color of a shield.
    // For the list of possible colors, see <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/DyeColor.html>.
    // Give no input with a shield to remove the base color (and any patterns).
    // Tag returns null if there is no base color or patterns.
    // -->

    public static boolean describes(ItemTag item) {
        return item.getBukkitMaterial() == Material.SHIELD;
    }

    @Override
    public ElementTag getPropertyValue() {
        DyeColor baseColor = getBaseColor();
        if (baseColor != null) {
            return new ElementTag(baseColor.name());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        setBaseColor(mechanism.hasValue() ? val.asEnum(DyeColor.class) : null, mechanism.context);
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
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            // TODO: 1.20.6: Banner color has been part of the item type for a while, and Spigot removed this API
            return ((BannerMeta) itemMeta).getBaseColor();
        }
        return null;
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
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19)) {
            // TODO: 1.20.6: Banner color has been part of the item type for a while, and Spigot removed this API
            ((BannerMeta) itemMeta).setBaseColor(color);
        }
        setItemMeta(itemMeta);
    }

    public static void register() {
        autoRegister("base_color", ItemBaseColor.class, ElementTag.class, false);
    }
}
