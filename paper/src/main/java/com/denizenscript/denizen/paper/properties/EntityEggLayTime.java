package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.Chicken;

public class EntityEggLayTime extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name egg_lay_time
    // @input ElementTag(Number)
    // @plugin Paper
    // @description
    // If the entity is a chicken, controls the number of ticks until it lays an egg.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Chicken;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(as(Chicken.class).getEggLayTime());
    }

    @Override
    public String getPropertyId() {
        return "egg_lay_time";
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            as(Chicken.class).setEggLayTime(param.asInt());
        }
    }

    public static void register() {
        autoRegister("egg_lay_time", EntityEggLayTime.class, ElementTag.class, false);
    }
}
