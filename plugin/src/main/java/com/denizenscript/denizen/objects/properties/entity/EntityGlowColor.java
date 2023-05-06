package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ColorTag;
import org.bukkit.Color;
import org.bukkit.entity.Display;

public class EntityGlowColor extends EntityProperty<ColorTag> {

    // <--[property]
    // @object EntityTag
    // @name glow_color
    // @input ColorTag
    // @description
    // A display entity's glow color override, if any.
    // For the mechanism: provide no input to remove the override.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public ColorTag getPropertyValue() {
        Color glowColorOverride = as(Display.class).getGlowColorOverride();
        return glowColorOverride != null ? BukkitColorExtensions.fromColor(glowColorOverride) : null;
    }

    @Override
    public void setPropertyValue(ColorTag value, Mechanism mechanism) {
        as(Display.class).setGlowColorOverride(value != null ? BukkitColorExtensions.getColor(value) : null);
    }

    @Override
    public String getPropertyId() {
        return "glow_color";
    }

    public static void register() {
        autoRegisterNullable("glow_color", EntityGlowColor.class, ColorTag.class, false);
    }
}
