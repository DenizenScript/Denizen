package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.ArmorStand;

public class EntityBasePlate implements Property {
    
    // <--[property]
    // @object EntityTag
    // @name base_plate
    // @input ElementTag(Boolean)
    // @description
    // If the entity is an armor stand, controls its base plate.
    // -->

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
    
    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        getStand().setBasePlate(mechanism.getValue().asBoolean());
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
        autoRegister("base_plate", EntityBasePlate.class, ElementTag.class, false)
    }
}
