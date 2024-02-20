package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.entity.Display;

public class EntityTeleportDuration extends EntityProperty<DurationTag> {

    // <--[property]
    // @object EntityTag
    // @name teleport_duration
    // @input DurationTag
    // @description
    // The duration a display entity will spend teleporting between positions.
    // See also <@link language Display entity interpolation>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public DurationTag getPropertyValue() {
        return new DurationTag((long) as(Display.class).getTeleportDuration());
    }

    @Override
    public boolean isDefaultValue(DurationTag value) {
        return value.getTicksAsInt() == 0;
    }

    @Override
    public void setPropertyValue(DurationTag value, Mechanism mechanism) {
        as(Display.class).setTeleportDuration(value.getTicksAsInt());
    }

    @Override
    public String getPropertyId() {
        return "teleport_duration";
    }

    public static void register() {
        autoRegister("teleport_duration", EntityTeleportDuration.class, DurationTag.class, false);
    }
}
