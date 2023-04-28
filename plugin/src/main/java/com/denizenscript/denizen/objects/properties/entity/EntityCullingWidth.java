package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Display;

public class EntityCullingWidth extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name culling_width
    // @input ElementTag
    // @description
    // The width of a display entity's culling box. The box will spawn half the width in every direction from the entity's position.
    // The default value is 0, which disables culling entirely.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Display.class).getDisplayWidth());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asFloat() == 0f;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireFloat()) {
            as(Display.class).setDisplayWidth(value.asFloat());
        }
    }

    @Override
    public String getPropertyId() {
        return "culling_width";
    }

    public static void register() {
        autoRegister("culling_width", EntityCullingWidth.class, ElementTag.class, false);
    }
}
