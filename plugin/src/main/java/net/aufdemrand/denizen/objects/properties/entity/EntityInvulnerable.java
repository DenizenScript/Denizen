package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;

public class EntityInvulnerable implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity;
    }

    public static EntityInvulnerable getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityInvulnerable((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[]{
            "invulnerable"
    };

    public static final String[] handledMechs = new String[] {
            "invulnerable"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityInvulnerable(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (!dentity.getBukkitEntity().isInvulnerable()) {
            return null;
        }
        else {
            return "true";
        }
    }

    @Override
    public String getPropertyId() {
        return "invulnerable";
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
        // @attribute <e@entity.invulnerable>
        // @returns Element(Boolean)
        // @mechanism dEntity.invulnerable
        // @group properties
        // @description
        // Returns whether the entity is invulnerable (cannot be damaged).
        // -->
        if (attribute.startsWith("invulnerable")) {
            return new Element(dentity.getBukkitEntity().isInvulnerable())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name invulnerable
        // @input Element(Boolean)
        // @description
        // Sets whether the entity is invulnerable (cannot be damaged).
        // @tags
        // <e@entity.invulnerable>
        // -->

        if (mechanism.matches("invulnerable") && mechanism.requireBoolean()) {
            dentity.getBukkitEntity().setInvulnerable(mechanism.getValue().asBoolean());
        }
    }
}
