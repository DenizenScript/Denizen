package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;

public class EntityFlower implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        Entity bukkitEntity = ((EntityTag) entity).getBukkitEntity();
        return bukkitEntity instanceof Bee;
    }

    public static EntityFlower getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityFlower((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "flower"
    };

    public static final String[] handledMechs = new String[] {
            "flower"
    };

    public EntityFlower(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public Bee getBee() {
        return (Bee) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        if (getBee().getFlower() == null) {
            return null;
        }
        return new LocationTag(getBee().getFlower()).identify();
    }

    @Override
    public String getPropertyId() {
        return "flower";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.flower>
        // @returns LocationTag
        // @mechanism EntityTag.flower
        // @group properties
        // @description
        // Returns the location of a bee's flower (if any).
        // -->
        if (attribute.startsWith("flower")) {
            if (getBee().getFlower() == null) {
                return null;
            }
            return new LocationTag(getBee().getFlower())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name flower
        // @input LocationTag
        // @description
        // Changes the location of a bee's flower.
        // Give no input to unset the bee's flower.
        // @tags
        // <EntityTag.flower>
        // -->
        if (mechanism.matches("flower")) {
            if (mechanism.hasValue() && mechanism.requireObject(LocationTag.class)) {
                getBee().setFlower(mechanism.valueAsType(LocationTag.class));
            }
            else {
                getBee().setFlower(null);
            }
        }
    }
}
