package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;

public class EntityFreezeDuration implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag;
    }

    public static EntityFreezeDuration getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityFreezeDuration((EntityTag) entity);
    }

    public static final String[] handledMechs = new String[] {
            "freeze_duration"
    };

    public EntityFreezeDuration(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new DurationTag((long) entity.getBukkitEntity().getFreezeTicks()).identify();
    }

    @Override
    public String getPropertyId() {
        return "freeze_duration";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.max_freeze_duration>
        // @returns DurationTag
        // @mechanism EntityTag.freeze_duration
        // @group attributes
        // @description
        // Returns the maximum duration an entity can be freezing for (from powdered snow). (When reached, an entity is fully frozen over).
        // -->
        PropertyParser.registerTag(EntityFreezeDuration.class, DurationTag.class, "max_freeze_duration", (attribute, entity) -> {
            return new DurationTag((long) entity.entity.getBukkitEntity().getMaxFreezeTicks());
        });

        // <--[tag]
        // @attribute <EntityTag.freeze_duration>
        // @returns DurationTag
        // @mechanism EntityTag.freeze_duration
        // @group attributes
        // @description
        // Returns the duration an entity has been freezing for (from powdered snow).
        // -->
        PropertyParser.registerTag(EntityFreezeDuration.class, DurationTag.class, "freeze_duration", (attribute, entity) -> {
            return new DurationTag((long) entity.entity.getBukkitEntity().getFreezeTicks());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name freeze_duration
        // @input DurationTag
        // @description
        // Sets the duration an entity has been freezing for (from powdered snow).
        // @tags
        // <EntityTag.freeze_duration>
        // -->
        if (mechanism.matches("freeze_duration") && mechanism.requireObject(DurationTag.class)) {
            entity.getBukkitEntity().setFreezeTicks(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}
