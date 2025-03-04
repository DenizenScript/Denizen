package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.AbstractHorse;

public class EntityMaxDomestication extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name max_domestication
    // @input ElementTag(Number)
    // @description
    // Controls a horse-type entity's maximum domestication level.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof AbstractHorse;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(AbstractHorse.class).getMaxDomestication());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            as(AbstractHorse.class).setMaxDomestication(param.asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "max_domestication";
    }

    public static void register() {
        autoRegister("max_domestication", EntityMaxDomestication.class, ElementTag.class, false);
    }
}
