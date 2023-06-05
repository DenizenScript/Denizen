package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ColorTag;
import org.bukkit.Color;
import org.bukkit.entity.TextDisplay;

public class EntityBackgroundColor extends EntityProperty<ColorTag> {

    // <--[property]
    // @object EntityTag
    // @name background_color
    // @input ColorTag
    // @description
    // A text display entity's background color.
    // Note that this is based on experimental API; while unlikely, breaking changes aren't impossible.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof TextDisplay;
    }

    @Override
    public ColorTag getPropertyValue() {
        Color backgroundColor = as(TextDisplay.class).getBackgroundColor();
        return BukkitColorExtensions.fromColor(backgroundColor != null ? backgroundColor : Color.WHITE);
    }

    @Override
    public boolean isDefaultValue(ColorTag value) {
        return value.red == 0 && value.green == 0 && value.blue == 0 && value.alpha == 64;
    }

    @Override
    public void setPropertyValue(ColorTag value, Mechanism mechanism) {
        as(TextDisplay.class).setBackgroundColor(BukkitColorExtensions.getColor(value));
    }

    @Override
    public String getPropertyId() {
        return "background_color";
    }

    public static void register() {
        autoRegister("background_color", EntityBackgroundColor.class, ColorTag.class, false);
    }
}
