package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
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

    public EntityTrapped(EntityTag ent) {
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
            return new ElementTag(object.getSkeletonHorse().isTrapped());
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
            object.getSkeletonHorse().setTrapped(input.asBoolean());
        });
    }

    public SkeletonHorse getSkeletonHorse() {
        return (SkeletonHorse) entity.getBukkitEntity();
    }
}
