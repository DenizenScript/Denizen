package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Display;

public class EntityViewRange extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name view_range
    // @input ElementTag(Decimal)
    // @description
    // A display entity's view range, how far away from a player will it still be visible to them.
    // Note that the final value used depends on client settings such as entity distance scaling, and is multiplied by 64 client-side.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Display.class).getViewRange());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asFloat() == 1f;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireFloat()) {
            as(Display.class).setViewRange(value.asFloat());
        }
    }

    @Override
    public String getPropertyId() {
        return "view_range";
    }

    public static void register() {
        autoRegister("view_range", EntityViewRange.class, ElementTag.class, false);
    }
}
