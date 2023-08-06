package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.ArmorStand;

public class EntityBasePlate implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntityBasePlate getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityBasePlate((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "base_plate"
    };

    public EntityBasePlate(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    public ArmorStand getStand() {
        return (ArmorStand) dentity.getBukkitEntity();
    }

    @Override
    public String getPropertyString() {
        return getStand().hasBasePlate() ? null : "false";
    }

    @Override
    public String getPropertyId() {
        return "base_plate";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.base_plate>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.base_plate
        // @group properties
        // @description
        // If the entity is an armor stand, returns whether the armor stand has a base plate.
        // -->
        PropertyParser.registerTag(EntityBasePlate.class, ElementTag.class, "base_plate", (attribute, object) -> {
            return new ElementTag(object.getStand().hasBasePlate());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name base_plate
        // @input ElementTag(Boolean)
        // @description
        // Changes the base plate state of an armor stand.
        // @tags
        // <EntityTag.base_plate>
        // -->
        if (mechanism.matches("base_plate") && mechanism.requireBoolean()) {
            getStand().setBasePlate(mechanism.getValue().asBoolean());
        }
    }
}
