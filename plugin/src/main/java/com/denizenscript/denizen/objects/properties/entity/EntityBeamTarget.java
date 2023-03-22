package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;

public class EntityBeamTarget implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof EnderCrystal;
    }

    public static EntityBeamTarget getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityBeamTarget((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "beam_target"
    };

    public EntityBeamTarget(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public String getPropertyString() {
        Location beamTarget = getCrystal().getBeamTarget();
        return beamTarget != null ? new LocationTag(beamTarget).identify() : null;
    }

    @Override
    public String getPropertyId() {
        return "beam_target";
    }

    public EnderCrystal getCrystal() {
        return (EnderCrystal) dentity.getBukkitEntity();
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.beam_target>
        // @returns LocationTag
        // @mechanism EntityTag.beam_target
        // @group properties
        // @description
        // Returns the target location of the ender crystal's beam, if any.
        // -->
        PropertyParser.registerTag(EntityBeamTarget.class, LocationTag.class, "beam_target", (attribute, object) -> {
            Location beamTarget = object.getCrystal().getBeamTarget();
            if (beamTarget != null) {
                return new LocationTag(beamTarget);
            }
            return null;
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name beam_target
        // @input LocationTag
        // @description
        // Sets a new target location for the ender crystal's beam.
        // Provide no input to remove the beam.
        // @tags
        // <EntityTag.beam_target>
        // -->
        if (mechanism.matches("beam_target")) {
            if (mechanism.hasValue()) {
                if (mechanism.requireObject(LocationTag.class)) {
                    getCrystal().setBeamTarget(mechanism.valueAsType(LocationTag.class));
                }
            }
            else {
                getCrystal().setBeamTarget(null);
            }
        }
    }
}
