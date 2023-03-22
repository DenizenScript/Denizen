package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.AbstractArrow;

public class EntityCritical implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof AbstractArrow;
    }

    public static EntityCritical getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCritical((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "critical"
    };

    public EntityCritical(EntityTag entity) {
        critical = entity;
    }

    EntityTag critical;

    @Override
    public String getPropertyString() {
        return getAbstractArrow().isCritical() ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "critical";
    }

    public AbstractArrow getAbstractArrow() {
        return (AbstractArrow) critical.getBukkitEntity();
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.critical>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.critical
        // @group properties
        // @description
        // If the entity is an arrow or trident, returns whether the arrow/trident is critical.
        // -->
        PropertyParser.registerTag(EntityCritical.class, ElementTag.class, "critical", (attribute, object) -> {
            return new ElementTag(object.getAbstractArrow().isCritical());
        });
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
            getAbstractArrow().setCritical(mechanism.getValue().asBoolean());
        }
    }
}
