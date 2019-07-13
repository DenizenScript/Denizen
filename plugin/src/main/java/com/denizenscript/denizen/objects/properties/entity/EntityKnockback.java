package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Arrow;

public class EntityKnockback implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityKnockback getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityKnockback((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "knockback"
    };

    public static final String[] handledMechs = new String[] {
            "knockback"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityKnockback(dEntity entity) {
        arrow = entity;
    }

    dEntity arrow;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(((Arrow) arrow.getBukkitEntity()).getKnockbackStrength());
    }

    @Override
    public String getPropertyId() {
        return "knockback";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.knockback>
        // @returns Element(Number)
        // @mechanism dEntity.knockback
        // @group properties
        // @description
        // If the entity is an arrow or trident, returns the knockback strength of the arrow/trident.
        // -->
        if (attribute.startsWith("knockback")) {
            return new Element(((Arrow) arrow.getBukkitEntity()).getKnockbackStrength())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name knockback
        // @input Element(Number)
        // @description
        // Changes an arrow's/trident's knockback strength.
        // @tags
        // <e@entity.knockback>
        // -->

        if (mechanism.matches("knockback") && mechanism.requireInteger()) {
            ((Arrow) arrow.getBukkitEntity()).setKnockbackStrength(mechanism.getValue().asInt());
        }
    }
}
