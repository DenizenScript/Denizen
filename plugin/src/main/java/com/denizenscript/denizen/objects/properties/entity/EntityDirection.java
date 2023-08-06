package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Fireball;

public class EntityDirection implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Fireball;
    }

    public static EntityDirection getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityDirection((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "direction"
    };

    public EntityDirection(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new LocationTag(getFireball().getDirection()).identify();
    }

    @Override
    public String getPropertyId() {
        return "direction";
    }

    public Fireball getFireball() {
        return (Fireball) entity.getBukkitEntity();
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.direction>
        // @returns LocationTag
        // @mechanism EntityTag.direction
        // @group attributes
        // @description
        // Returns the movement/acceleration direction of a fireball entity, as a LocationTag vector.
        // -->
        PropertyParser.registerTag(EntityDirection.class, LocationTag.class, "direction", (attribute, object) -> {
            return new LocationTag(object.getFireball().getDirection());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name direction
        // @input LocationTag
        // @description
        // Sets the movement/acceleration direction of a fireball entity, as a LocationTag vector.
        // @tags
        // <EntityTag.direction>
        // -->
        if (mechanism.matches("direction") && mechanism.requireObject(LocationTag.class)) {
            getFireball().setDirection(mechanism.valueAsType(LocationTag.class).toVector());
        }
    }
}
