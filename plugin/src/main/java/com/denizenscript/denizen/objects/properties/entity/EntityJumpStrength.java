package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.AbstractHorse;

public class EntityJumpStrength implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                ((EntityTag) entity).getBukkitEntity() instanceof AbstractHorse;
    }

    public static EntityJumpStrength getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityJumpStrength((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "jump_strength"
    };

    public static final String[] handledMechs = new String[] {
            "jump_strength"
    };

    public EntityJumpStrength(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(((AbstractHorse) entity.getBukkitEntity()).getJumpStrength());
    }

    @Override
    public String getPropertyId() {
        return "jump_strength";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.jump_strength>
        // @returns ElementTag(Number)
        // @mechanism EntityTag.jump_strength
        // @group properties
        // @synonyms EntityTag.horse_jump_height
        // @description
        // Returns the power of a horse's jump.
        // Also applies to horse-like mobs, such as donkeys and mules.
        // -->
        if (attribute.startsWith("jump_strength")) {
            return new ElementTag(((AbstractHorse) entity.getBukkitEntity()).getJumpStrength())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name jump_strength
        // @input ElementTag(Number)
        // @synonyms EntityTag.horse_jump_height
        // @description
        // Sets the power of the horse's jump.
        // Also applies to horse-like mobs, such as donkeys and mules.
        // @tags
        // <EntityTag.jump_strength>
        // -->
        if (mechanism.matches("jump_strength") && mechanism.requireDouble()) {
            ((AbstractHorse) entity.getBukkitEntity()).setJumpStrength(mechanism.getValue().asDouble());
        }
    }
}
