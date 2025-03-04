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
    // Controls the level at which a horse-type entity has to reach in order to be fully tamed.
    // Value must be an integer between 0 and 2,147,483,647 inclusive.
    // Default value for llamas and trader llamas are 30.
    // Default value for all other entities is 100.
    // To automatically tame an entity, see <@link mechanism EntityTag.tame>.
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
