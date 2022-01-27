package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import org.bukkit.entity.Goat;

public class EntityGoatRam implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Goat;
    }

    public static EntityGoatRam getFrom(ObjectTag _entity) {
        if (!describes(_entity)) {
            return null;
        }
        else {
            return new EntityGoatRam((EntityTag) _entity);
        }
    }

    public static final String[] handledMechs = new String[]{
            "goat_ram"
    };

    private EntityGoatRam(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void registerTags() {
        // None
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "goat_ram";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name goat_ram
        // @input EntityTag
        // @Plugin Paper
        // @description
        // Causes a goat to ram the specified entity.
        // -->
        if (mechanism.matches("goat_ram") && mechanism.requireObject(EntityTag.class)) {
            ((Goat) entity.getBukkitEntity()).ram(mechanism.valueAsType(EntityTag.class).getLivingEntity());
        }
    }
}