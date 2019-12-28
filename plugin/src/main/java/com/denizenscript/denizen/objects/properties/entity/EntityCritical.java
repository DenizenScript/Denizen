package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Arrow;

public class EntityCritical implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof Arrow;
    }

    public static EntityCritical getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCritical((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "critical"
    };

    public static final String[] handledMechs = new String[] {
            "critical"
    };

    private EntityCritical(EntityTag entity) {
        critical = entity;
    }

    EntityTag critical;

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

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.critical>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.critical
        // @group properties
        // @description
        // If the entity is an arrow or trident, returns whether the arrow/trident is critical.
        // -->
        if (attribute.startsWith("critical")) {
            return new ElementTag(((Arrow) critical.getBukkitEntity()).isCritical())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name critical
        // @input ElementTag(Boolean)
        // @description
        // Changes whether an arrow/trident is critical.
        // @tags
        // <EntityTag.critical>
        // -->

        if (mechanism.matches("critical") && mechanism.requireBoolean()) {
            ((Arrow) critical.getBukkitEntity()).setCritical(mechanism.getValue().asBoolean());
        }
    }
}
