package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.ArmorStand;

public class EntityBasePlate extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name base_plate
    // @input ElementTag(Boolean)
    // @description
    // Controls whether an armor stand has a base plate.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof ArmorStand;
    }

    @Override
    public boolean isDefaultValue(ElementTag val) {
        return val.asBoolean();
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(ArmorStand.class).hasBasePlate());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(ArmorStand.class).setBasePlate(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "base_plate";
    }

    public static void register() {
        autoRegister("base_plate", EntityBasePlate.class, ElementTag.class, false);
    }
}
