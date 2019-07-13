package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Explosive;

public class EntityExplosionFire implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntity() instanceof Explosive;
    }

    public static EntityExplosionFire getFrom(dObject entity) {
        return describes(entity) ? new EntityExplosionFire((dEntity) entity) : null;
    }

    public static final String[] handledTags = new String[] {
            "explosion_fire"
    };

    public static final String[] handledMechs = new String[] {
            "explosion_fire"
    };

    public boolean isIncendiary() {
        return ((Explosive) entity.getBukkitEntity()).isIncendiary();
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityExplosionFire(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(isIncendiary());
    }

    @Override
    public String getPropertyId() {
        return "explosion_fire";
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
        // @attribute <e@entity.explosion_fire>
        // @returns Element(Boolean)
        // @mechanism dEntity.explosion_fire
        // @group properties
        // @description
        // If this entity is explosive, returns whether its explosion creates fire.
        // -->
        if (attribute.startsWith("explosion_fire")) {
            return new Element(isIncendiary())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name explosion_fire
        // @input Element(Boolean)
        // @description
        // If this entity is explosive, sets whether its explosion creates fire.
        // @tags
        // <e@entity.explosion_fire>
        // -->
        if (mechanism.matches("explosion_fire") && mechanism.requireBoolean()) {
            ((Explosive) entity.getBukkitEntity()).setIsIncendiary(mechanism.getValue().asBoolean());
        }
    }
}
