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
    // Controls the upper-bound for a horse-type entity's chance to be tamed.
    //
    // When a player mounts an entity, a number between 0 and the entity's max temper is generated.
    // The entity becomes tamed if this value is less than the entity's temper value.
    // Otherwise, the player gets bucked and increases the entity's temper by 5.
    //
    // Because an entity must have a level to reach before it can be tamed, value must be 1 or higher.
    // Default value for llamas and trader llamas is 30.
    // Default value for all other entities is 100.
    //
    // To control the entity's current temper, see <@link mechanism EntityTag.temper>.
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
