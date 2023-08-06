package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;

public class EntityHasNectar implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        Entity bukkitEntity = ((EntityTag) entity).getBukkitEntity();
        return bukkitEntity instanceof Bee;
    }

    public static EntityHasNectar getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityHasNectar((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "has_nectar"
    };

    public static final String[] handledMechs = new String[] {
            "has_nectar"
    };

    public EntityHasNectar(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public Bee getBee() {
        return (Bee) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return getBee().hasNectar() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "has_nectar";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.has_nectar>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.has_nectar
        // @group properties
        // @description
        // Returns whether a bee entity has nectar on it.
        // -->
        if (attribute.startsWith("has_nectar")) {
            return new ElementTag(getBee().hasNectar())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name has_nectar
        // @input ElementTag(Boolean)
        // @description
        // Changes whether a bee entity has nectar on it.
        // @tags
        // <EntityTag.has_nectar>
        // -->
        if (mechanism.matches("has_nectar") && mechanism.requireBoolean()) {
            getBee().setHasNectar(mechanism.getValue().asBoolean());
        }
    }
}
