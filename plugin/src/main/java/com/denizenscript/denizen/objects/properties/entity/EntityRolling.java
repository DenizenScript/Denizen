package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Panda;

public class EntityRolling extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name is_rolling
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a panda is rolling on the ground.

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Panda;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Panda.class).isRolling());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(Panda.class).setRolling(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "is_rolling";
    }

    public static void register() {
        autoRegister("is_rolling", EntityRolling.class, ElementTag.class, false);
    }
}
