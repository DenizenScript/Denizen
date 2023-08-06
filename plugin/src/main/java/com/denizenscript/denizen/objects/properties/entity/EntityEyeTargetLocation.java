package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.EnderSignal;

public class EntityEyeTargetLocation implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof EnderSignal;
    }

    public static EntityEyeTargetLocation getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityEyeTargetLocation((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "ender_eye_target_location"
    };

    public EntityEyeTargetLocation(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.ender_eye_target_location>
        // @returns LocationTag
        // @mechanism EntityTag.ender_eye_target_location
        // @group properties
        // @description
        // Returns a thrown eye of ender's target location - the location it's moving towards, which in vanilla is a stronghold location.
        // -->
        PropertyParser.registerTag(EntityEyeTargetLocation.class, LocationTag.class, "ender_eye_target_location", (attribute, entity) -> {
            return new LocationTag(((EnderSignal) entity.entity.getBukkitEntity()).getTargetLocation());
        });
    }

    @Override
    public String getPropertyString() {
        return new LocationTag(((EnderSignal) entity.getBukkitEntity()).getTargetLocation()).identify();
    }

    @Override
    public String getPropertyId() {
        return "ender_eye_target_location";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name ender_eye_target_location
        // @input LocationTag
        // @description
        // Sets a thrown eye of ender's target location - the location it's moving towards.
        // @tags
        // <EntityTag.ender_eye_target_location>
        // -->
        if (mechanism.matches("ender_eye_target_location") && mechanism.requireObject(LocationTag.class)) {
            ((EnderSignal) entity.getBukkitEntity()).setTargetLocation(mechanism.valueAsType(LocationTag.class));
        }
    }
}
