package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.AbstractArrow;

public class EntityArrowPierceLevel extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name pierce_level
    // @input ElementTag(Decimal)
    // @description
    // Controls the number of times this arrow can pierce through an entity. Must be between 0 and 127 times.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof AbstractArrow;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(AbstractArrow.class).getPierceLevel());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireDouble()) {
            as(AbstractArrow.class).setPierceLevel(value.asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "pierce_level";
    }

    public static void register() {
        autoRegister("pierce_level", EntityArrowPierceLevel.class, ElementTag.class, false);
    }
}