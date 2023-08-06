package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;

public class EntityHive implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        Entity bukkitEntity = ((EntityTag) entity).getBukkitEntity();
        return bukkitEntity instanceof Bee;
    }

    public static EntityHive getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityHive((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "hive"
    };

    public static final String[] handledMechs = new String[] {
            "hive"
    };

    public EntityHive(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public Bee getBee() {
        return (Bee) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        if (getBee().getHive() == null) {
            return null;
        }
        return new LocationTag(getBee().getHive()).identify();
    }

    @Override
    public String getPropertyId() {
        return "hive";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.hive>
        // @returns LocationTag
        // @mechanism EntityTag.hive
        // @group properties
        // @description
        // Returns the location of a bee's hive (if any).
        // -->
        if (attribute.startsWith("hive")) {
            if (getBee().getHive() == null) {
                return null;
            }
            return new LocationTag(getBee().getHive())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name hive
        // @input LocationTag
        // @description
        // Changes the location of a bee's hive.
        // Give no input to unset the bee's hive.
        // @tags
        // <EntityTag.hive>
        // -->
        if (mechanism.matches("hive")) {
            if (mechanism.hasValue() && mechanism.requireObject(LocationTag.class)) {
                getBee().setHive(mechanism.valueAsType(LocationTag.class));
            }
            else {
                getBee().setHive(null);
            }
        }
    }
}
