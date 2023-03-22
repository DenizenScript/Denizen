package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;

public class EntityCannotEnterHive implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        Entity bukkitEntity = ((EntityTag) entity).getBukkitEntity();
        return bukkitEntity instanceof Bee;
    }

    public static EntityCannotEnterHive getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCannotEnterHive((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "cannot_enter_hive"
    };

    public static final String[] handledMechs = new String[] {
            "cannot_enter_hive"
    };

    public EntityCannotEnterHive(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public Bee getBee() {
        return (Bee) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return new DurationTag((long) getBee().getCannotEnterHiveTicks()).identify();
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
        // @attribute <EntityTag.cannot_enter_hive>
        // @returns DurationTag
        // @mechanism EntityTag.cannot_enter_hive
        // @group properties
        // @description
        // Returns the minimum duration until a Bee entity is allowed to enter a hive.
        // -->
        if (attribute.startsWith("cannot_enter_hive")) {
            return new DurationTag((long) getBee().getCannotEnterHiveTicks())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name cannot_enter_hive
        // @input DurationTag
        // @description
        // Changes the minimum duration until a Bee entity is allowed to enter a hive.
        // @tags
        // <EntityTag.cannot_enter_hive>
        // -->
        if (mechanism.matches("cannot_enter_hive") && mechanism.requireObject(DurationTag.class)) {
            getBee().setCannotEnterHiveTicks(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}
