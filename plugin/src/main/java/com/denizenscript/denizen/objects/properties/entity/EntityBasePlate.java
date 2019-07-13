package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

public class EntityBasePlate implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.ARMOR_STAND;
    }

    public static EntityBasePlate getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityBasePlate((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "base_plate"
    };

    public static final String[] handledMechs = new String[] {
            "base_plate"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityBasePlate(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        if (((ArmorStand) dentity.getBukkitEntity()).hasBasePlate()) {
            return null;
        }
        else {
            return "false";
        }
    }

    @Override
    public String getPropertyId() {
        return "base_plate";
    }

    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <EntityTag.base_plate>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.base_plate
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand has a base plate.
        // -->
        if (attribute.startsWith("base_plate")) {
            return new ElementTag(((ArmorStand) dentity.getBukkitEntity()).hasBasePlate())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name base_plate
        // @input Element(Boolean)
        // @description
        // Changes the base plate state of an armor stand.
        // @tags
        // <EntityTag.base_plate>
        // -->

        if (mechanism.matches("base_plate") && mechanism.requireBoolean()) {
            ((ArmorStand) dentity.getBukkitEntity()).setBasePlate(mechanism.getValue().asBoolean());
        }
    }
}
