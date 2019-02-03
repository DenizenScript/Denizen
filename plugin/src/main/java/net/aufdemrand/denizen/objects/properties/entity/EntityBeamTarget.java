package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;

public class EntityBeamTarget implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity && ((dEntity) entity).getBukkitEntityType() == EntityType.ENDER_CRYSTAL;
    }

    public static EntityBeamTarget getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityBeamTarget((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "beam_target"
    };

    public static final String[] handledMechs = new String[] {
            "beam_target"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityBeamTarget(dEntity entity) {
        dentity = entity;
    }

    dEntity dentity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return new dLocation(((EnderCrystal) dentity.getBukkitEntity()).getBeamTarget()).identify();
    }

    @Override
    public String getPropertyId() {
        return "beam_target";
    }

    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <e@entity.beam_target>
        // @returns dLocation
        // @mechanism dEntity.beam_target
        // @group properties
        // @description
        // Returns the target location of the ender crystal's beam, if any.
        // -->
        if (attribute.startsWith("beam_target")) {
            Location beamTarget = ((EnderCrystal) dentity.getBukkitEntity()).getBeamTarget();
            if (beamTarget != null) {
                return new dLocation(beamTarget).getAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name beam_target
        // @input dLocation
        // @description
        // Sets a new target location for the ender crystal's beam.
        // Provide no input to remove the beam.
        // @tags
        // <e@entity.beam_target>
        // -->

        if (mechanism.matches("beam_target")) {
            if (mechanism.hasValue()) {
                if (mechanism.requireObject(dLocation.class)) {
                    ((EnderCrystal) dentity.getBukkitEntity()).setBeamTarget(mechanism.getValue().asType(dLocation.class));
                }
            }
            else {
                ((EnderCrystal) dentity.getBukkitEntity()).setBeamTarget(null);
            }
        }
    }
}
