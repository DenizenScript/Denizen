package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Vex;

public class EntityCharging implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof Vex;
    }

    public static EntityCharging getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCharging((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
        "charging"
    };

    private EntityCharging(EntityTag _entity) {
        entity = _entity;
    }

    EntityTag entity;

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.charging>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.charging
        // @group properties
        // @description
        // If the entity is a Vex, returns whether the entity is charging.
        // -->
        PropertyParser.<EntityCharging, ElementTag>registerTag(ElementTag.class, "charging", (attribute, entity) -> {
            return new ElementTag(((Vex) entity.entity.getBukkitEntity()).isCharging());
        });
    }

    @Override
    public String getPropertyString() {
        return ((Vex) entity.getBukkitEntity()).isCharging() ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "EntityCharging";
    }


    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name charging
        // @input ElementTag(Boolean)
        // @description
        // If the entity is a Vex, sets whether the entity is charging.
        // @tags
        // <EntityTag.charging>
        // -->
        if (mechanism.matches("charging") && mechanism.requireBoolean()) {
            ((Vex) entity.getBukkitEntity()).setCharging(mechanism.getValue().asBoolean());
        }
    }
}
