package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Explosive;

public class EntityExplosionFire implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Explosive;
    }

    public static EntityExplosionFire getFrom(ObjectTag entity) {
        return describes(entity) ? new EntityExplosionFire((EntityTag) entity) : null;
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

    public EntityExplosionFire(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(isIncendiary());
    }

    @Override
    public String getPropertyId() {
        return "explosion_fire";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.explosion_fire>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.explosion_fire
        // @group properties
        // @description
        // If this entity is explosive, returns whether its explosion creates fire.
        // -->
        if (attribute.startsWith("explosion_fire")) {
            return new ElementTag(isIncendiary())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name explosion_fire
        // @input ElementTag(Boolean)
        // @description
        // If this entity is explosive, sets whether its explosion creates fire.
        // @tags
        // <EntityTag.explosion_fire>
        // -->
        if (mechanism.matches("explosion_fire") && mechanism.requireBoolean()) {
            ((Explosive) entity.getBukkitEntity()).setIsIncendiary(mechanism.getValue().asBoolean());
        }
    }
}
