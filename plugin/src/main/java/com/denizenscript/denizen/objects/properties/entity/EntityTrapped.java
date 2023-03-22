package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.SkeletonHorse;

public class EntityTrapped implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag ent &&
                (ent.getBukkitEntity() instanceof SkeletonHorse);
    }

    public static EntityTrapped getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityTrapped((EntityTag) entity);
        }
    }

    private EntityTrapped(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(((SkeletonHorse) entity.getBukkitEntity()).isTrapped());
    }

    @Override
    public String getPropertyId() {
        return "trapped";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.trapped>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.trapped
        // @group properties
        // @description
        // Returns whether the skeleton horse is trapped.
        // A trapped skeleton horse will trigger the skeleton horse trap when the player is within 10 blocks of it.
        // -->
        PropertyParser.registerTag(EntityTrapped.class, ElementTag.class, "trapped", (attribute, object) -> {
            return new ElementTag(object.isTrapped());
        });

        // <--[mechanism]
        // @object EntityTag
        // @name trapped
        // @input ElementTag(Boolean)
        // @description
        // Sets whether the skeleton horse is trapped.
        // A trapped skeleton horse will trigger the skeleton horse trap when the player is within 10 blocks of it.
        // @tags
        // <EntityTag.trapped>
        // -->
        PropertyParser.registerMechanism(EntityTrapped.class, ElementTag.class, "trapped", (object, mechanism, input) -> {
            if (!mechanism.requireBoolean()) {
                return;
            }
            if (object.isSkeletonHorse()) {
                object.getSkeletonHorse().setTrapped(input.asBoolean());
            }
        });

        // <--[tag]
        // @attribute <EntityTag.trap_time>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.trap_time
        // @group properties
        // @description
        // Returns the skeleton horse's trap time in ticks.
        // Trap time will go up every tick for as long as the horse is trapped (see <@link tag EntityTag.trapped>).
        // A trapped horse will despawn after it reaches 18000 ticks (15 minutes).
        // -->
        PropertyParser.registerTag(EntityTrapped.class, DurationTag.class, "trap_time", (attribute, object) -> {
            return new DurationTag(object.getSkeletonHorse().getTrapTime());
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
        PropertyParser.registerMechanism(EntityTrapped.class, DurationTag.class, "trap_time", (object, mechanism, duration) -> {
            if (object.isSkeletonHorse()) {
                object.getSkeletonHorse().setTrapTime(duration.getTicksAsInt());
            }
        });
    }

    public boolean isSkeletonHorse() {
        return entity.getBukkitEntity() instanceof SkeletonHorse;
    }

    public SkeletonHorse getSkeletonHorse() {
        return (SkeletonHorse) entity.getBukkitEntity();
    }

    public boolean isTrapped() {
        if (isSkeletonHorse()) {
            return getSkeletonHorse().isTrapped();
        }
        return false;
    }
}
