package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.TreeSpecies;
import org.bukkit.entity.Boat;

public class EntityBoatType implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof EntityTag
                && ((EntityTag) object).getBukkitEntity() instanceof Boat;
    }

    public static EntityBoatType getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityBoatType((EntityTag) object);
        }
    }

    public static final String[] handledMechs = new String[] {
            "boat_type"
    };

    private EntityBoatType(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return getBoat().getWoodType().name();
    }

    @Override
    public String getPropertyId() {
        return "boat_type";
    }

    public Boat getBoat() {
        return (Boat) entity.getBukkitEntity();
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.boat_type>
        // @returns ElementTag
        // @mechanism EntityTag.boat_type
        // @group properties
        // @description
        // Returns the wood type of the boat.
        // Valid wood types: GENERIC, REDWOOD, BIRCH, JUNGLE, ACACIA, DARK_OAK.
        // -->
        PropertyParser.registerTag(EntityBoatType.class, ElementTag.class, "boat_type", (attribute, object) -> {
            return new ElementTag(object.getBoat().getWoodType());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name boat_type
        // @input ElementTag
        // @description
        // Changes the wood type of the boat.
        // Valid wood types: GENERIC, REDWOOD, BIRCH, JUNGLE, ACACIA, DARK_OAK.
        // @tags
        // <EntityTag.boat_type>
        // -->
        if (mechanism.matches("boat_type") && mechanism.requireEnum(TreeSpecies.class)) {
            getBoat().setWoodType(TreeSpecies.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
