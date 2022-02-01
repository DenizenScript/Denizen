package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Arrow;

public class EntityArrowDamage implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityArrowDamage getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityArrowDamage((EntityTag) entity);
    }

    public static final String[] handledMechs = {
            "damage"
    };

    private EntityArrowDamage(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public String getPropertyString() {
        return String.valueOf(getArrow().getDamage());
    }

    @Override
    public String getPropertyId() {
        return "damage";
    }

    public Arrow getArrow() {
        return (Arrow) dentity.getBukkitEntity();
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.damage>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.damage
        // @group properties
        // @description
        // Returns the damage that the arrow/trident will inflict.
        // NOTE: The actual damage dealt by the arrow/trident may be different depending on the projectile's flight speed.
        // -->
        PropertyParser.<EntityArrowDamage, ElementTag>registerTag(ElementTag.class, "damage", (attribute, object) -> {
            return new ElementTag(object.getArrow().getDamage());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name damage
        // @input ElementTag(Decimal)
        // @description
        // Changes how much damage an arrow/trident will inflict.
        // @tags
        // <EntityTag.damage>
        // -->
        if (mechanism.matches("damage") && mechanism.requireDouble()) {
            getArrow().setDamage(mechanism.getValue().asDouble());
        }
    }
}
