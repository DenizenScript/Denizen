package com.denizenscript.denizen.paper.properties;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.entity.EntityProperty;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.entity.Chicken;

public class EntityEggLayTime extends EntityProperty<DurationTag> {

    // <--[property]
    // @object EntityTag
    // @name egg_lay_time
    // @input DurationTag
    // @plugin Paper
    // @description
    // If the entity is a chicken, controls the duration of time until it next lays an egg.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Chicken;
    }

    @Override
    public DurationTag getPropertyValue() {
        return new DurationTag((long) as(Chicken.class).getEggLayTime());
    }

    @Override
    public String getPropertyId() {
        return "egg_lay_time";
    }

    @Override
    public void setPropertyValue(DurationTag param, Mechanism mechanism) {
        as(Chicken.class).setEggLayTime(param.getTicksAsInt());
    }

    public static void register() {
        autoRegister("egg_lay_time", EntityEggLayTime.class, DurationTag.class, false);
    }
}
