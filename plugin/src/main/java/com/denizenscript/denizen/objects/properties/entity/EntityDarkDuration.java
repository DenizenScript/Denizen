package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.GlowSquid;

public class EntityDarkDuration implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof GlowSquid;
    }

    public static EntityDarkDuration getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityDarkDuration((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "dark_duration"
    };

    public EntityDarkDuration(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new DurationTag((long) getGlowSquid().getDarkTicksRemaining()).identify();
    }

    @Override
    public String getPropertyId() {
        return "dark_duration";
    }

    public GlowSquid getGlowSquid() {
        return (GlowSquid) entity.getBukkitEntity();
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.dark_duration>
        // @returns DurationTag
        // @mechanism EntityTag.dark_duration
        // @group attributes
        // @description
        // Returns the duration remaining before a glow squid starts glowing.
        // -->
        PropertyParser.registerTag(EntityDarkDuration.class, DurationTag.class, "dark_duration", (attribute, object) -> {
            return new DurationTag((long) object.getGlowSquid().getDarkTicksRemaining());
        });
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
            getGlowSquid().setDarkTicksRemaining(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}
