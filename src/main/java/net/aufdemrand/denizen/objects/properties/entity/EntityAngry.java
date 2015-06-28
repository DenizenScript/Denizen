package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Wolf;

public class EntityAngry implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.WOLF;
    }

    public static EntityAngry getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntityAngry((dEntity) entity);
    }

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
        if (!((Wolf) entity.getBukkitEntity()).isAngry())
            return null;
        else
            return "true";
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
        // @attribute <e@entity.angry>
        // @returns Element(Boolean)
        // @mechanism dEntity.angry
        // @group properties
        // @description
        // If the entity is a wolf, returns whether the wolf is angry.
        // -->
        if (attribute.startsWith("angry"))
            return new Element(((Wolf) entity.getBukkitEntity()).isAngry())
                    .getAttribute(attribute.fulfill(1));

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name angry
        // @input Element(Boolean)
        // @description
        // Changes the anger state of a wolf.
        // @tags
        // <e@entity.angry>
        // -->

        if (mechanism.matches("angry") && mechanism.requireBoolean()) {
            ((Wolf) entity.getBukkitEntity()).setAngry(mechanism.getValue().asBoolean());
        }
    }
}
