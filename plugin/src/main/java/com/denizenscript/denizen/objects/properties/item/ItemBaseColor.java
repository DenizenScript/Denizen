package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.DyeColor;
import org.bukkit.Material;

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
        DyeColor color = NMSHandler.itemHelper.getShieldColor(getItemStack());
        return color != null ? new ElementTag(color) : null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.hasValue() && !mechanism.requireEnum(DyeColor.class)) {
            return;
        }
        setItemStack(NMSHandler.itemHelper.setShieldColor(getItemStack(), mechanism.hasValue() ? value.asEnum(DyeColor.class) : null));
    }

    @Override
    public String getPropertyId() {
        return "base_color";
    }

    public static void register() {
        autoRegisterNullable("base_color", ItemBaseColor.class, ElementTag.class, false);
    }
}
