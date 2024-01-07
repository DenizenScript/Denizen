package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Turtle;

public class EntityCarryingEgg implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Turtle;
    }

    public static EntityCarryingEgg getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCarryingEgg((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "carrying_egg"
    };

    public EntityCarryingEgg(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.carrying_egg>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.carrying_egg
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is a turtle, returns whether it is carrying an egg. A turtle that is carrying an egg isn't visually different, but can't breed and will eventually lay the egg.
        // -->
        PropertyParser.registerTag(EntityCarryingEgg.class, ElementTag.class, "carrying_egg", (attribute, entity) -> {
            return new ElementTag(((Turtle) entity.entity.getBukkitEntity()).hasEgg());
        });
    }

    @Override
    public String getPropertyString() {
        return ((Turtle) entity.getBukkitEntity()).hasEgg() ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "carrying_egg";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name carrying_egg
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @group properties
        // @description
        // If the entity is a turtle, sets whether it is carrying an egg.
        // @tags
        // <EntityTag.carrying_egg>
        // -->
        if (mechanism.matches("carrying_egg") && mechanism.requireBoolean()) {
            ((Turtle) entity.getBukkitEntity()).setHasEgg(mechanism.getValue().asBoolean());
        }
    }
}
