package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;

public class EntityGravity implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity;
    }

    public static EntityGravity getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityGravity((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "gravity"
    };

    public static final String[] handledMechs = new String[] {
            "gravity"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityGravity(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (dentity.getBukkitEntity().hasGravity()) {
            return null;
        }
        else {
            return "false";
        }
    }

    @Override
    public String getPropertyId() {
        return "gravity";
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
        // @attribute <e@entity.gravity>
        // @returns ElementTag(Boolean)
        // @mechanism dEntity.gravity
        // @group properties
        // @description
        // Returns whether the entity has gravity.
        // -->
        if (attribute.startsWith("gravity")) {
            return new ElementTag(dentity.getBukkitEntity().hasGravity())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name gravity
        // @input Element(Boolean)
        // @description
        // Changes the gravity state of an entity.
        // @tags
        // <e@entity.gravity>
        // -->

        if (mechanism.matches("gravity") && mechanism.requireBoolean()) {
            dentity.getBukkitEntity().setGravity(mechanism.getValue().asBoolean());
        }
    }
}
