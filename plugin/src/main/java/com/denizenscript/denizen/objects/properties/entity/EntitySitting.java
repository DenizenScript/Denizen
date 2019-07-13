package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Sittable;

public class EntitySitting implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Sittable;
    }

    public static EntitySitting getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySitting((EntityTag) entity);
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

    private EntitySitting(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

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
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.sitting>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.sitting
        // @group properties
        // @description
        // If the entity is a wolf, cat, or parrot, returns whether the animal is sitting.
        // -->
        if (attribute.startsWith("sitting")) {
            return new ElementTag(((Sittable) entity.getBukkitEntity()).isSitting())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name sitting
        // @input Element(Boolean)
        // @description
        // Changes the sitting state of a wolf, cat, or parrot.
        // @tags
        // <EntityTag.sitting>
        // -->

        if (mechanism.matches("sitting") && mechanism.requireBoolean()) {
            ((Sittable) entity.getBukkitEntity()).setSitting(mechanism.getValue().asBoolean());
        }
    }
}
