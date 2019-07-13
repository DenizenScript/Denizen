package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Sittable;

public class EntitySitting implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).getBukkitEntity() instanceof Sittable;
    }

    public static EntitySitting getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySitting((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "sitting"
    };

    public static final String[] handledMechs = new String[] {
            "sitting"
    };


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
        if (((Sittable) entity.getBukkitEntity()).isSitting()) {
            return "true";
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "sitting";
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
        // @attribute <e@entity.sitting>
        // @returns Element(Boolean)
        // @mechanism dEntity.sitting
        // @group properties
        // @description
        // If the entity is a wolf, cat, or parrot, returns whether the animal is sitting.
        // -->
        if (attribute.startsWith("sitting")) {
            return new Element(((Sittable) entity.getBukkitEntity()).isSitting())
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
        // Changes the sitting state of a wolf, cat, or parrot.
        // @tags
        // <e@entity.sitting>
        // -->

        if (mechanism.matches("sitting") && mechanism.requireBoolean()) {
            ((Sittable) entity.getBukkitEntity()).setSitting(mechanism.getValue().asBoolean());
        }
    }
}
