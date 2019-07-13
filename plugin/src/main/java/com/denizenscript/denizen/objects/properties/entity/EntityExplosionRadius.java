package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.Explosive;

public class EntityExplosionRadius implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && (((dEntity) entity).getBukkitEntity() instanceof Explosive
                || ((dEntity) entity).getBukkitEntity() instanceof Creeper);
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
        if (entity.getBukkitEntity() instanceof Creeper) {
            return ((Creeper) entity.getBukkitEntity()).getExplosionRadius();
        }
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
        // If this entity can explode, returns its explosion radius.
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
        // If this entity can explode, sets its explosion radius.
        // @tags
        // <e@entity.explosion_radius>
        // -->
        if (mechanism.matches("explosion_radius") && mechanism.requireFloat()) {
            if (entity.getBukkitEntity() instanceof Creeper) {
                ((Creeper) entity.getBukkitEntity()).setExplosionRadius(mechanism.getValue().asInt());
            }
            else {
                ((Explosive) entity.getBukkitEntity()).setYield(mechanism.getValue().asFloat());
            }
        }
    }
}
