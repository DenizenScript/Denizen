package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;

public class EntityHasStung implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        Entity bukkitEntity = ((EntityTag) entity).getBukkitEntity();
        return bukkitEntity instanceof Bee;
    }

    public static EntityHasStung getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityHasStung((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_stung"
    };

    public static final String[] handledMechs = new String[] {
            "has_stung"
    };

    public EntityHasStung(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public Bee getBee() {
        return (Bee) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return getBee().hasStung() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "has_stung";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.has_stung>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.has_stung
        // @group properties
        // @description
        // Returns whether a bee entity has already used its stinger.
        // -->
        if (attribute.startsWith("has_stung")) {
            return new ElementTag(getBee().hasStung())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name has_stung
        // @input ElementTag(Boolean)
        // @description
        // Changes whether a bee entity has already used its stinger.
        // @tags
        // <EntityTag.has_stung>
        // -->
        if (mechanism.matches("has_stung") && mechanism.requireBoolean()) {
            getBee().setHasStung(mechanism.getValue().asBoolean());
        }
    }
}
