package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;

public class EntityBoatType implements Property {

    public static boolean describes(dObject object) {
        return object instanceof dEntity && ((dEntity) object).getBukkitEntityType() == EntityType.BOAT;
    }

    public static EntityBoatType getFrom(dObject object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityBoatType((dEntity) object);
        }
    }

    public static final String[] handledTags = new String[]{
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
        // @returns Element
        // @mechanism dEntity.boat_type
        // @group properties
        // @description
        // Returns the wood type of the boat.
        // Valid wood types: GENERIC, REDWOOD, BIRCH, JUNGLE, ACACIA, DARK_OAK.
        // -->
        if (attribute.startsWith("boat_type")) {
            return new Element(((Boat) entity.getBukkitEntity()).getWoodType().name())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {
        Element value = mechanism.getValue();

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
            TreeSpecies type = TreeSpecies.valueOf(value.asString().toUpperCase());
            if (type != null) {
                ((Boat) entity.getBukkitEntity()).setWoodType(type);
            }
        }
    }
}
