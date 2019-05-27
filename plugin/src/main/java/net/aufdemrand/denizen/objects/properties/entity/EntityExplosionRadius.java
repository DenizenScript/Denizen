package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.Explosive;

public class EntityExplosionRadius implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntity() instanceof Explosive;
    }

    public static EntityExplosionRadius getFrom(dObject entity) {
        return describes(entity) ? new EntityExplosionRadius((dEntity) entity) : null;
    }

    public static final String[] handledTags = new String[] {
            "explosion_radius"
    };

    public static final String[] handledMechs = new String[] {
            "explosion_radius"
    };

    public float getExplosionRadius() {
        return ((Explosive) entity.getBukkitEntity()).getYield();
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityExplosionRadius(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(getExplosionRadius());
    }

    @Override
    public String getPropertyId() {
        return "explosion_radius";
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
        // @attribute <e@entity.explosion_radius>
        // @returns Element(Decimal)
        // @mechanism dEntity.explosion_radius
        // @group properties
        // @description
        // If this entity is explosive, returns its explosion radius.
        // -->
        if (attribute.startsWith("explosion_radius")) {
            return new Element(getExplosionRadius())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name explosion_radius
        // @input Element(Decimal)
        // @description
        // If this entity is explosive, sets its explosion radius.
        // @tags
        // <e@entity.explosion_radius>
        // -->
        if (mechanism.matches("explosion_radius") && mechanism.requireFloat()) {
            ((Explosive) entity.getBukkitEntity()).setYield(mechanism.getValue().asFloat());
        }
    }
}
