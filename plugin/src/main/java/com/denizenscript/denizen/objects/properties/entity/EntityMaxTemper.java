package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.AbstractHorse;

public class EntityMaxTemper extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name max_temper
    // @input ElementTag(Number)
    // @description
    // Controls the temper at which a horse-type entity has to reach in order to be fully tamed.
    // For information on how this temper is increased, see <@link tag EntityTag.temper>.
    //
    // Because an entity must have a level to reach before it can be domesticated, value must be 1 or higher.
    // Default value for llamas and trader llamas is 30.
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
        return "max_temper";
    }

    public static void register() {
        autoRegister("max_temper", EntityMaxTemper.class, ElementTag.class, false);
    }
}
