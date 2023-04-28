package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Display;

public class EntityCullingHeight extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name culling_height
    // @input ElementTag
    // @description
    // The height of a display entity's culling box. The box will spawn from the entity's y to the entity's y + the height.
    // The default value is 0, which disables culling entirely.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Display.class).getDisplayHeight());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asFloat() == 0f;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireFloat()) {
            as(Display.class).setDisplayHeight(value.asFloat());
        }
    }

    @Override
    public String getPropertyId() {
        return "culling_height";
    }

    public static void register() {
        autoRegister("culling_height", EntityCullingHeight.class, ElementTag.class, false);
    }
}
