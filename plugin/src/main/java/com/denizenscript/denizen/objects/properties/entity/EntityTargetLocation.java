package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.EnderSignal;

public class EntityTargetLocation implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof EnderSignal;
    }

    public static EntityTargetLocation getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityTargetLocation((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "target_location"
    };

    private EntityTargetLocation(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.target_location>
        // @returns LocationTag
        // @mechanism EntityTag.target_location
        // @group properties
        // @description
        // Returns a thrown eye of ender's target location.
        // -->
        PropertyParser.<EntityTargetLocation, LocationTag>registerTag(LocationTag.class, "target_location", (attribute, entity) -> {
            return new LocationTag(((EnderSignal) entity.entity.getBukkitEntity()).getTargetLocation());
        });
    }

    @Override
    public String getPropertyString() {
        return new LocationTag(((EnderSignal) entity.getBukkitEntity()).getTargetLocation()).identify();
    }

    @Override
    public String getPropertyId() {
        return "target_location";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name target_location
        // @input LocationTag
        // @description
        // Sets a thrown eye of ender's target location.
        // @tags
        // <EntityTag.target_location>
        // -->
        if (mechanism.matches("target_location") && mechanism.requireObject(LocationTag.class)) {
            ((EnderSignal) entity.getBukkitEntity()).setTargetLocation(mechanism.valueAsType(LocationTag.class));
        }
    }
}
