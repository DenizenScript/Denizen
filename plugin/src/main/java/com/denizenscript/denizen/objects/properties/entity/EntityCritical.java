package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Arrow;

public class EntityCritical implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityCritical getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCritical((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "critical"
    };

    public static final String[] handledMechs = new String[] {
            "critical"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityCritical(dEntity entity) {
        critical = entity;
    }

    dEntity critical;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!((Arrow) critical.getBukkitEntity()).isCritical()) {
            return null;
        }
        else {
            return "true";
        }
    }

    @Override
    public String getPropertyId() {
        return "critical";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.critical>
        // @returns ElementTag(Boolean)
        // @mechanism dEntity.critical
        // @group properties
        // @description
        // If the entity is an arrow or trident, returns whether the arrow/trident is critical.
        // -->
        if (attribute.startsWith("critical")) {
            return new ElementTag(((Arrow) critical.getBukkitEntity()).isCritical())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name critical
        // @input Element(Boolean)
        // @description
        // Changes whether an arrow/trident is critical.
        // @tags
        // <e@entity.critical>
        // -->

        if (mechanism.matches("critical") && mechanism.requireBoolean()) {
            ((Arrow) critical.getBukkitEntity()).setCritical(mechanism.getValue().asBoolean());
        }
    }
}
