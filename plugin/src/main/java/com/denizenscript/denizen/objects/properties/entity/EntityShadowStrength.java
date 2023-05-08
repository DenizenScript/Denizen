package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Display;

public class EntityShadowStrength extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name shadow_strength
    // @input ElementTag(Decimal)
    // @description
    // The strength of a display entity's shadow.
    // Note that the final opacity will change based on the entity's distance to the block the shadow is on.
    // Can be interpolated, see <@link language Display entity interpolation>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Display;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Display.class).getShadowStrength());
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asFloat() == 1f;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireFloat()) {
            as(Display.class).setShadowStrength(value.asFloat());
        }
    }

    @Override
    public String getPropertyId() {
        return "shadow_strength";
    }

    public static void register() {
        autoRegister("shadow_strength", EntityShadowStrength.class, ElementTag.class, false);
    }
}
