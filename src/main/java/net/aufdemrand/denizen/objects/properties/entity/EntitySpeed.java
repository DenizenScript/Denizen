package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.entity.EntityMovement;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

public class EntitySpeed implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).isLivingEntity();
    }

    public static EntitySpeed getFrom(dObject entity) {
        if (!describes(entity)) return null;

        else return new EntitySpeed((dEntity) entity);
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySpeed(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(EntityMovement.getSpeed(entity.getBukkitEntity()));
    }

    @Override
    public String getPropertyId() {
        return "speed";
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <e@entity.speed>
        // @returns Element(Decimal)
        // @mechanism dEntity.speed
        // @group attributes
        // @description
        // Returns the living entity's current speed when walking.
        // -->
        if (attribute.startsWith("speed"))
            return new Element(EntityMovement.getSpeed(entity.getBukkitEntity()))
                    .getAttribute(attribute.fulfill(1));


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name speed
        // @input Element(Decimal)
        // @description
        // Sets how fast the entity walks.
        // @tags
        // <e@entity.speed>
        // -->
        if (mechanism.matches("speed") && mechanism.requireDouble()) {
            EntityMovement.setSpeed(entity.getBukkitEntity(), mechanism.getValue().asDouble());
        }

    }
}
