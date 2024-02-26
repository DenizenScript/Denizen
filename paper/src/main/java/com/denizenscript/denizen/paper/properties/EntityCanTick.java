package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.ArmorStand;

public class EntityCanTick implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntityCanTick getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        return new EntityCanTick((EntityTag) entity);
    }

    public static final String[] handledMechs = new String[] {
            "can_tick"
    };

    public EntityCanTick(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return String.valueOf(((ArmorStand) entity.getBukkitEntity()).canTick());
    }

    @Override
    public String getPropertyId() {
        return "can_tick";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.can_tick>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.can_tick
        // @group properties
        // @Plugin Paper
        // @description
        // If the entity is an armor stand, returns whether the armor stand can tick.
        // -->
        PropertyParser.registerTag(EntityCanTick.class, ElementTag.class, "can_tick", (attribute, entity) -> {
            return new ElementTag(((ArmorStand) entity.entity.getBukkitEntity()).canTick());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name can_tick
        // @input ElementTag(Boolean)
        // @Plugin Paper
        // @group properties
        // @description
        // Changes whether an armor stand can tick.
        // @tags
        // <EntityTag.can_tick>
        // -->
        if (mechanism.matches("can_tick") && mechanism.requireBoolean()) {
            ((ArmorStand) entity.getBukkitEntity()).setCanTick(mechanism.getValue().asBoolean());
        }
    }
}
