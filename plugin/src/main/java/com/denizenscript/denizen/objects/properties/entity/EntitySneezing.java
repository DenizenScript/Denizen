package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Panda;

public class EntitySneezing extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name sneezing
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a panda is sneezing.
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
        return new ElementTag(as(Panda.class).isSneezing());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(Panda.class).setSneezing(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "sneezing";
    }

    public static void register() {
        autoRegister("sneezing", EntitySneezing.class, ElementTag.class, false);
    }
}
