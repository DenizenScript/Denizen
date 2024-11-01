package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.ArmorStand;

public class EntityArms implements Property {
    
    // <--[property]
    // @object EntityTag
    // @name arms
    // @input ElementTag(Boolean)
    // @description
    // If the entity is an armor stand, controls its arms.
    // -->

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).getBukkitEntity() instanceof ArmorStand;
    }

    public static EntityArms getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityArms((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "arms"
    };

    public EntityArms(EntityTag entity) {
        dentity = entity;
    }

    EntityTag dentity;

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        getStand().setArms(mechanism.getValue().asBoolean());
    }

    @Override
    public String getPropertyString() {
        return getStand().hasArms() ? "true" : null;
    }

    @Override
    public String getPropertyId() {
        return "arms";
    }

    public ArmorStand getStand() {
        return (ArmorStand) dentity.getBukkitEntity();
    }

    public static void register() {
        autoRegister("arms", EntityArms.class, ElementTag.class, false)
    }
}
