package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Display;

public class EntityShadowRadius extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name shadow_radius
    // @input ElementTag(Decimal)
    // @description
    // The radius of a display entity's shadow.
    // Can be interpolated, see <@link language Display entity interpolation>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Display.class).getShadowRadius());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asFloat() == 0f;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireFloat()) {
            as(Display.class).setShadowRadius(value.asFloat());
        }
    }

    @Override
    public String getPropertyId() {
        return "shadow_radius";
    }

    public static void register() {
        autoRegister("shadow_radius", EntityShadowRadius.class, ElementTag.class, false);
    }
}
