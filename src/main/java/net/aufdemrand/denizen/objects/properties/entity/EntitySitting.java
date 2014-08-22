package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.Mechanism;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dObject;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Wolf;

public class EntitySitting implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && (
                ((dEntity)entity).getEntityType() == EntityType.WOLF
                || ((dEntity)entity).getEntityType() == EntityType.OCELOT);
    }

    public static EntitySitting getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntitySitting((dEntity) entity);
    }

    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySitting(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (entity.getEntityType() == EntityType.WOLF) {
            if (!((Wolf)entity.getBukkitEntity()).isSitting())
                return null;
            else
                return "true";
        }
        else {
            if (!((Ocelot)entity.getBukkitEntity()).isSitting())
                return null;
            else
                return "true";
        }
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

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.sitting>
        // @returns Element(Boolean)
        // @mechanism dEntity.sitting
        // @group properties
        // @description
        // If the entity is a wolf or ocelot, returns whether the animal is sitting.
        // -->
        if (attribute.startsWith("sitting")) {
            if (entity.getEntityType() == EntityType.WOLF)
                return new Element(((Wolf)entity.getBukkitEntity()).isSitting())
                        .getAttribute(attribute.fulfill(1));
            else
                return new Element(((Ocelot)entity.getBukkitEntity()).isSitting())
                        .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name sitting
        // @input Element(Boolean)
        // @description
        // Changes the sitting state of a wolf or ocelot.
        // @tags
        // <e@entity.sitting>
        // -->

        if (mechanism.matches("sitting") && mechanism.requireBoolean()) {
            if (entity.getEntityType() == EntityType.WOLF)
                ((Wolf)entity.getBukkitEntity()).setSitting(mechanism.getValue().asBoolean());
            else
                ((Ocelot)entity.getBukkitEntity()).setSitting(mechanism.getValue().asBoolean());
        }
    }
}
