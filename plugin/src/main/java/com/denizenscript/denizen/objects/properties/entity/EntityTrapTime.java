package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.SkeletonHorse;

public class EntityTrapTime implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag ent &&
                (ent.getBukkitEntity() instanceof SkeletonHorse);
    }

    public static EntityTrapTime getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityTrapTime((EntityTag) entity);
        }
    }

    public EntityTrapTime(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new DurationTag((long) getSkeletonHorse().getTrapTime()).identify();
    }

    @Override
    public String getPropertyId() {
        return "trap_time";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.trap_time>
        // @returns DurationTag
        // @mechanism EntityTag.trap_time
        // @group properties
        // @description
        // Returns the skeleton horse's trap time in ticks.
        // Trap time will go up every tick for as long as the horse is trapped (see <@link tag EntityTag.trapped>).
        // A trapped horse will despawn after it reaches 18000 ticks (15 minutes).
        // -->
        PropertyParser.registerTag(EntityTrapTime.class, DurationTag.class, "trap_time", (attribute, object) -> {
            return new DurationTag((long) object.getSkeletonHorse().getTrapTime());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name trap_time
        // @input DurationTag
        // @description
        // Sets the skeleton horse's trap time.
        // Trap time will go up every tick for as long as the horse is trapped (see <@link tag EntityTag.trapped>).
        // A trap time greater than 18000 ticks (15 minutes) will despawn the horse on the next tick.
        // @tags
        // <EntityTag.trapped>
        // -->
        PropertyParser.registerMechanism(EntityTrapTime.class, DurationTag.class, "trap_time", (object, mechanism, duration) -> {
            object.getSkeletonHorse().setTrapTime(duration.getTicksAsInt());
        });
    }

    public SkeletonHorse getSkeletonHorse() {
        return (SkeletonHorse) entity.getBukkitEntity();
    }
}
