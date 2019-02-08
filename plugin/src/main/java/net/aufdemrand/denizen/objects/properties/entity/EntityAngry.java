package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Wolf;

public class EntityAngry implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && (((dEntity) entity).getBukkitEntityType() == EntityType.WOLF
                || ((dEntity) entity).getBukkitEntityType() == EntityType.PIG_ZOMBIE);
    }

    public static EntityAngry getFrom(dObject entity) {
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
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.angry>
        // @returns Element(Boolean)
        // @mechanism dEntity.angry
        // @group properties
        // @description
        // If the entity is a wolf, returns whether the wolf is angry.
        // -->
        if (attribute.startsWith("angry")) {
            if (entity.getBukkitEntityType() == EntityType.WOLF) {
                return new Element(((Wolf) entity.getBukkitEntity()).isAngry())
                        .getAttribute(attribute.fulfill(1));
            }
            else if (entity.getBukkitEntityType() == EntityType.PIG_ZOMBIE) {
                return new Element(((PigZombie) entity.getBukkitEntity()).isAngry())
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
