package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Panda;

public class EntityRolling extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name rolling
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a panda is rolling on the ground.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Panda;
    }

    @Override
    public boolean isDefaultValue(ElementTag val) {
        return !val.asBoolean();
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
        return "rolling";
    }

    public static void register() {
        autoRegister("rolling", EntityRolling.class, ElementTag.class, false);
    }
}
