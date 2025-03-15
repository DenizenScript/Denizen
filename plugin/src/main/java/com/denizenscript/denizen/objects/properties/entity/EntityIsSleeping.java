package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Fox;
import org.bukkit.entity.LivingEntity;

public class EntityIsSleeping extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name is_sleeping
    // @input ElementTag(Boolean)
    // @description
    // Controls whether an entity is currently sleeping.
    // The tag will always return 'false' on entities that are unable to sleep. Entities that could receive other values are foxes, players, and villagers.
    // The mechanism is only valid for fox entities.
    // The entity may wake up immediately after setting this to true. If this is not desired, disable <@link mechanism has_ai>.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof LivingEntity;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(LivingEntity.class).isSleeping());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (getEntity() instanceof Fox fox) {
            fox.setSleeping(param.asBoolean());
        }
        else {
            mechanism.echoError("'is_sleeping' mechanism is only valid for Fox entities.");
        }
    }

    @Override
    public String getPropertyId() {
        return "is_sleeping";
    }

    public static void register() {
        autoRegister("is_sleeping", EntityIsSleeping.class, ElementTag.class, false);
    }
}
