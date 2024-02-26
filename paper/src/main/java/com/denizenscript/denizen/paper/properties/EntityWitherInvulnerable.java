package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Wither;

public class EntityWitherInvulnerable implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Wither;
    }

    public static EntityWitherInvulnerable getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityWitherInvulnerable((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[]{
            "invulnerable_duration"
    };

    public EntityWitherInvulnerable(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.invulnerable_duration>
        // @returns DurationTag
        // @mechanism EntityTag.invulnerable_duration
        // @group properties
        // @Plugin Paper
        // @description
        // Returns the duration remaining until the wither becomes vulnerable.
        // -->
        PropertyParser.registerTag(EntityWitherInvulnerable.class, DurationTag.class, "invulnerable_duration", (attribute, object) -> {
            return new DurationTag((long) object.getWither().getInvulnerableTicks());
        });
    }

    public Wither getWither() {
        return (Wither) entity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        int ticks = getWither().getInvulnerableTicks();
        if (ticks == 0) {
            return null;
        }
        return new DurationTag((long) ticks).identify();
    }

    @Override
    public String getPropertyId() {
        return "invulnerable_duration";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name invulnerable_duration
        // @input DurationTag
        // @Plugin Paper
        // @group properties
        // @description
        // Sets the duration remaining until the wither becomes vulnerable.
        // @tags
        // <EntityTag.invulnerable_duration>
        // -->
        if (mechanism.matches("invulnerable_duration") && mechanism.requireObject(DurationTag.class)) {
            getWither().setInvulnerableTicks(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}
