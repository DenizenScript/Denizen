package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Panda;

public class EntityOnBack extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name on_back
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a panda is on its back.
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
        return new ElementTag(as(Panda.class).isOnBack());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(Panda.class).setOnBack(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "on_back";
    }

    public static void register() {
        autoRegister("on_back", EntityOnBack.class, ElementTag.class, false);
    }
}
