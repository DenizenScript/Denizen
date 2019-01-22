package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;

public class EntitySmall implements Property {


    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                ((dEntity) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntitySmall getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySmall((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[]{
            "is_small"
    };

    public static final String[] handledMechs = new String[] {
            "is_small"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySmall(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (((ArmorStand) entity.getBukkitEntity()).isSmall()) {
            return "true";
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "small";
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
        // @attribute <e@entity.is_small>
        // @returns Element(Boolean)
        // @mechanism dEntity.is_small
        // @group properties
        // @description
        // Returns whether the armor stand is small.
        // -->
        if (attribute.startsWith("is_small")) {
            return new Element(((ArmorStand) entity.getBukkitEntity()).isSmall())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name is_small
        // @input Element(Boolean)
        // @description
        // Sets whether the armor stand is small.
        // @tags
        // <e@entity.is_small>
        // -->
        if (mechanism.matches("is_small") && mechanism.requireBoolean()) {
            ((ArmorStand) entity.getBukkitEntity()).setSmall(mechanism.getValue().asBoolean());
        }
    }
}

