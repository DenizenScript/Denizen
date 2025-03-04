package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.AbstractHorse;

public class EntityTemper extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name temper
    // @input ElementTag(Number)
    // @description
    // Controls the temper of a horse-type entity.
    // A value of "0" indicates that no action has been done to try and domesticate this entity.
    //
    // When a player mounts an entity, a number between 0 and the entity's max temper minus one is generated.
    // If this number is greater than the entity's current temper, than the entity is tamed.
    // If this number is less than or equal to the entity's current temper, the entity bucks the player, and its temper is increased by 5.
    // Temper can also be increased by feeding the entity.
    // - Apples, sugar, and wheat increase temper by 3.
    // - Golden carrots increase temper by 5.
    // - Golden apples increase temper by 10.
    //
    // To control the entity's max temper, see <@link mechanism EntityTag.max_temper>.
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
        return "temper";
    }

    public static void register() {
        autoRegister("temper", EntityTemper.class, ElementTag.class, false);
    }
}
