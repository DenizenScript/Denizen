package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.GlowSquid;

public class EntityDarkDuration implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag && ((EntityTag) entity).getBukkitEntity() instanceof GlowSquid;
    }

    public static EntityDarkDuration getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityDarkDuration((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "dark_duration"
    };

    public static final String[] handledMechs = new String[] {
            "dark_duration"
    };

    private EntityDarkDuration(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new DurationTag((long) ((GlowSquid) entity.getBukkitEntity()).getDarkTicksRemaining()).identify();
    }

    @Override
    public String getPropertyId() {
        return "dark_time_remaining";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.dark_duration>
        // @returns DurationTag
        // @mechanism EntityTag.direction
        // @group attributes
        // @description
        // Returns the duration remaining before a glow squid starts glowing.
        // -->
        if (attribute.startsWith("dark_duration")) {
            return new DurationTag((long) ((GlowSquid) entity.getBukkitEntity()).getDarkTicksRemaining())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name dark_duration
        // @input DurationTag
        // @description
        // Sets the duration remaining before a glow squid starts glowing.
        // @tags
        // <EntityTag.dark_duration>
        // -->
        if (mechanism.matches("dark_duration") && mechanism.requireObject(DurationTag.class)) {
            ((GlowSquid) entity.getBukkitEntity()).setDarkTicksRemaining(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}
