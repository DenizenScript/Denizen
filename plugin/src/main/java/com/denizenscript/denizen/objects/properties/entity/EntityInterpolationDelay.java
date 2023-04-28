package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.entity.Display;

public class EntityInterpolationDelay extends EntityProperty<DurationTag> {

    // <--[property]
    // @object EntityTag
    // @name interpolation_delay
    // @input DurationTag
    // @description
    // The delay between a display entity receiving an update to an interpolated value(s) to it starting its interpolation.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public DurationTag getPropertyValue() {
        return new DurationTag((long) as(Display.class).getInterpolationDelay());
    }

    @Override
    public boolean isDefaultValue(DurationTag value) {
        return value.getTicksAsInt() == 0;
    }

    @Override
    public void setPropertyValue(DurationTag value, Mechanism mechanism) {
        as(Display.class).setInterpolationDelay(value.getTicksAsInt());
    }

    @Override
    public String getPropertyId() {
        return "interpolation_delay";
    }

    public static void register() {
        autoRegister("interpolation_delay", EntityInterpolationDelay.class, DurationTag.class, false);
    }
}
