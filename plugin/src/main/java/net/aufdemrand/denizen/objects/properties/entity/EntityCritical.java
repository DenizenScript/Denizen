package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.EntityType;

public class EntityCritical implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.ARROW;
    }

    public static EntityCritical getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCritical((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[]{
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
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.critical>
        // @returns Element(Boolean)
        // @mechanism dEntity.critical
        // @group properties
        // @description
        // If the entity is an arrow, returns whether the arrow is critical.
        // -->
        if (attribute.startsWith("critical")) {
            return new Element(((Arrow) critical.getBukkitEntity()).isCritical())
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
        // Changes whether an arrow is critical.
        // @tags
        // <e@entity.critical>
        // -->

        if (mechanism.matches("critical") && mechanism.requireBoolean()) {
            ((Arrow) critical.getBukkitEntity()).setCritical(mechanism.getValue().asBoolean());
        }
    }
}
