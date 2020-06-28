package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Wolf;

public class EntityAngry implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && (((EntityTag) entity).getBukkitEntity() instanceof Wolf
                || ((EntityTag) entity).getBukkitEntity() instanceof PigZombie);
    }

    public static EntityAngry getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAngry((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "angry"
    };

    public static final String[] handledMechs = new String[] {
            "angry"
    };

    private EntityAngry(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (entity.getBukkitEntity() instanceof Wolf) {
            if (!((Wolf) entity.getLivingEntity()).isAngry()) {
                return null;
            }
            else {
                return "true";
            }
        }
        else if (entity.getBukkitEntity() instanceof PigZombie) {
            if (!((PigZombie) entity.getLivingEntity()).isAngry()) {
                return null;
            }
            else {
                return "true";
            }
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "angry";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.angry>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.angry
        // @group properties
        // @description
        // If the entity is a wolf or PigZombie, returns whether the entity is angry.
        // -->
        if (attribute.startsWith("angry")) {
            if (entity.getBukkitEntity() instanceof Wolf) {
                return new ElementTag(((Wolf) entity.getBukkitEntity()).isAngry())
                        .getObjectAttribute(attribute.fulfill(1));
            }
            else if (entity.getBukkitEntity() instanceof PigZombie) {
                return new ElementTag(((PigZombie) entity.getBukkitEntity()).isAngry())
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name angry
        // @input ElementTag(Boolean)
        // @description
        // Changes the anger state of a Wolf or PigZombie.
        // @tags
        // <EntityTag.angry>
        // -->
        if (mechanism.matches("angry") && mechanism.requireBoolean()) {
            if (entity.getBukkitEntity() instanceof Wolf) {
                ((Wolf) entity.getBukkitEntity()).setAngry(mechanism.getValue().asBoolean());
            }
            else if (entity.getBukkitEntity() instanceof PigZombie) {
                ((PigZombie) entity.getBukkitEntity()).setAngry(mechanism.getValue().asBoolean());
            }
        }
    }
}
