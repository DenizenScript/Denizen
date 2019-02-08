package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.PigZombie;

public class EntityAnger implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.PIG_ZOMBIE;
    }

    public static EntityAnger getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAnger((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "anger"
    };

    public static final String[] handledMechs = new String[] {
            "anger"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityAnger(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(((PigZombie) entity.getBukkitEntity()).getAnger());
    }

    @Override
    public String getPropertyId() {
        return "anger";
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
        // @attribute <e@entity.anger>
        // @returns Element(Number)
        // @mechanism dEntity.anger
        // @group properties
        // @description
        // Returns the anger level of a PigZombie.
        // -->
        if (attribute.startsWith("anger")) {
            if (entity.getBukkitEntityType() == EntityType.PIG_ZOMBIE) {
                return new Element(((PigZombie) entity.getBukkitEntity()).getAnger())
                        .getAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name anger
        // @input Element(Boolean)
        // @description
        // Changes the anger level of a PigZombie.
        // @tags
        // <e@entity.anger>
        // -->

        if (mechanism.matches("anger") && mechanism.requireInteger()) {
            if (entity.getBukkitEntityType() == EntityType.PIG_ZOMBIE) {
                ((PigZombie) entity.getBukkitEntity()).setAnger(mechanism.getValue().asInt());
            }
        }
    }
}
