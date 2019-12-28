package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Location;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;

public class EntityBeamTarget implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntityType() == EntityType.ENDER_CRYSTAL;
    }

    public static EntityBeamTarget getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityBeamTarget((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "beam_target"
    };

    public static final String[] handledMechs = new String[] {
            "beam_target"
    };

    private EntityBeamTarget(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public String getPropertyString() {
        return new LocationTag(((EnderCrystal) dentity.getBukkitEntity()).getBeamTarget()).identify();
    }

    @Override
    public String getPropertyId() {
        return "beam_target";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.beam_target>
        // @returns LocationTag
        // @mechanism EntityTag.beam_target
        // @group properties
        // @description
        // Returns the target location of the ender crystal's beam, if any.
        // -->
        if (attribute.startsWith("beam_target")) {
            Location beamTarget = ((EnderCrystal) dentity.getBukkitEntity()).getBeamTarget();
            if (beamTarget != null) {
                return new LocationTag(beamTarget).getObjectAttribute(attribute.fulfill(1));
            }
        }

        return null;
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
                    ((EnderCrystal) dentity.getBukkitEntity()).setBeamTarget(mechanism.valueAsType(LocationTag.class));
                }
            }
            else {
                ((EnderCrystal) dentity.getBukkitEntity()).setBeamTarget(null);
            }
        }
    }
}
