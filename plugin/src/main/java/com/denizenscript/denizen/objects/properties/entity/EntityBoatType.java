package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;

public class EntityBoatType implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof dEntity && ((dEntity) object).getBukkitEntityType() == EntityType.BOAT;
    }

    public static EntityBoatType getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityBoatType((dEntity) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "boat_type"
    };

    public static final String[] handledMechs = new String[] {
            "boat_type"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityBoatType(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return ((Boat) entity.getBukkitEntity()).getWoodType().name();
    }

    @Override
    public String getPropertyId() {
        return "boat_type";
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.boat_type>
        // @returns ElementTag
        // @mechanism dEntity.boat_type
        // @group properties
        // @description
        // Returns the wood type of the boat.
        // Valid wood types: GENERIC, REDWOOD, BIRCH, JUNGLE, ACACIA, DARK_OAK.
        // -->
        if (attribute.startsWith("boat_type")) {
            return new ElementTag(((Boat) entity.getBukkitEntity()).getWoodType().name())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {
        // <--[mechanism]
        // @object dEntity
        // @name boat_type
        // @input Element
        // @description
        // Changes the wood type of the boat.
        // Valid wood types: GENERIC, REDWOOD, BIRCH, JUNGLE, ACACIA, DARK_OAK.
        // @tags
        // <e@entity.boat_type>
        // -->

        if (mechanism.matches("boat_type")) {
            TreeSpecies type = TreeSpecies.valueOf(mechanism.getValue().asString().toUpperCase());
            if (type != null) {
                ((Boat) entity.getBukkitEntity()).setWoodType(type);
            }
        }
    }
}
