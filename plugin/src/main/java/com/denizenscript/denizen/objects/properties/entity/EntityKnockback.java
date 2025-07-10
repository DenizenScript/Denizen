package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.AbstractArrow;

public class EntityKnockback implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof EntityTag entity && entity.getBukkitEntity() instanceof AbstractArrow;
    }

    public static EntityKnockback getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityKnockback((EntityTag) object);
        }
    }

    public EntityKnockback(EntityTag entity) {
        arrow = entity;
    }

    EntityTag arrow;

    @Override
    public String getPropertyString() {
        return String.valueOf(getAbstractArrow().getKnockbackStrength());
    }

    @Override
    public String getPropertyId() {
        return "knockback";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.knockback>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.knockback
        // @group properties
        // @description
        // Returns the knockback strength of an arrow or trident.
        // -->
        PropertyParser.registerTag(EntityKnockback.class, ElementTag.class, "knockback", (attribute, object) -> {
            return new ElementTag(object.getAbstractArrow().getKnockbackStrength());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name knockback
        // @input ElementTag(Number)
        // @description
        // Sets the knockback strength of an arrow or trident.
        // @tags
        // <EntityTag.knockback>
        // -->
        PropertyParser.registerMechanism(EntityKnockback.class, ElementTag.class, "knockback", (object, mechanism, input) -> {
            if (mechanism.requireInteger()) {
                object.getAbstractArrow().setKnockbackStrength(input.asInt());
            }
        });
    }

    public AbstractArrow getAbstractArrow() {
        return (AbstractArrow) arrow.getBukkitEntity();
    }
}
