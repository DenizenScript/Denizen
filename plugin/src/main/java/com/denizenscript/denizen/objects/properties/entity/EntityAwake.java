package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Bat;

public class EntityAwake extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name awake
    // @input ElementTag(Boolean)
    // @description
    // Controls whether a bat is flying (awake/true) or hanging (asleep/false).

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Bat;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Bat.class).isAwake());
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            as(Bat.class).setAwake(param.asBoolean());
        }
    }

    @Override
    public String getPropertyId() {
        return "awake";
    }

    public static void register() {
        autoRegister("awake", EntityAwake.class, ElementTag.class, false);
    }
}
