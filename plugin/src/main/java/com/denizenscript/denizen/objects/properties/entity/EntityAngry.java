package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Wolf;

public class EntityAngry implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity && (((dEntity) entity).getBukkitEntityType() == EntityType.WOLF
                || ((dEntity) entity).getBukkitEntityType() == EntityType.PIG_ZOMBIE);
    }

    public static EntityAngry getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAngry((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "angry"
    };

    public static final String[] handledMechs = new String[] {
            "angry"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityAngry(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (entity.getBukkitEntityType() == EntityType.WOLF) {
            if (!((Wolf) entity.getLivingEntity()).isAngry()) {
                return null;
            }
            else {
                return "true";
            }
        }
        else if (entity.getBukkitEntityType() == EntityType.PIG_ZOMBIE) {
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

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.angry>
        // @returns ElementTag(Boolean)
        // @mechanism dEntity.angry
        // @group properties
        // @description
        // If the entity is a wolf, returns whether the wolf is angry.
        // -->
        if (attribute.startsWith("angry")) {
            if (entity.getBukkitEntityType() == EntityType.WOLF) {
                return new ElementTag(((Wolf) entity.getBukkitEntity()).isAngry())
                        .getAttribute(attribute.fulfill(1));
            }
            else if (entity.getBukkitEntityType() == EntityType.PIG_ZOMBIE) {
                return new ElementTag(((PigZombie) entity.getBukkitEntity()).isAngry())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name angry
        // @input Element(Boolean)
        // @description
        // Changes the anger state of a Wolf or PigZombie.
        // @tags
        // <e@entity.angry>
        // -->

        if (mechanism.matches("angry") && mechanism.requireBoolean()) {
            if (entity.getBukkitEntityType() == EntityType.WOLF) {
                ((Wolf) entity.getBukkitEntity()).setAngry(mechanism.getValue().asBoolean());
            }
            else if (entity.getBukkitEntityType() == EntityType.PIG_ZOMBIE) {
                ((PigZombie) entity.getBukkitEntity()).setAngry(mechanism.getValue().asBoolean());
            }
        }
    }
}
