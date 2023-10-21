package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.AbstractArrow;

public class EntityArrowDamage extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name damage
    // @input ElementTag(Decimal)
    // @description
    // The amount of damage an arrow/trident will inflict.
    // Note that the actual damage dealt by the arrow/trident may be different depending on the projectile's flight speed.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof AbstractArrow;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(AbstractArrow.class).getDamage());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireDouble()) {
            as(AbstractArrow.class).setDamage(value.asDouble());
        }
    }

    @Override
    public String getPropertyId() {
        return "damage";
    }

    public static void register() {
        autoRegister("damage", EntityArrowDamage.class, ElementTag.class, false);
    }
}
