package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.AbstractHorse;

public class EntityDomestication extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name domestication
    // @input ElementTag(Number)
    // @description
    // Controls how close to being fully tamed a horse-type entity is.
    // Value must be an integer between 1 and 2,147,483,647 inclusive.
    // To automatically tame an entity, see <@link mechanism EntityTag.tame>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof AbstractHorse;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(AbstractHorse.class).getDomestication());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            as(AbstractHorse.class).setDomestication(param.asInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "domestication";
    }

    public static void register() {
        autoRegister("domestication", EntityDomestication.class, ElementTag.class, false);
    }
}
