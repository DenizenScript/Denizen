package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.DurationTag;
import org.bukkit.entity.Bee;

public class EntityCannotEnterHive extends EntityProperty<DurationTag> {

    // <--[property]
    // @object EntityTag
    // @name cannot_enter_hive
    // @input DurationTag
    // @description
    // Controls the minimum duration until a Bee is able to enter a hive.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Bee;
    }

    @Override
    public DurationTag getPropertyValue() {
        return new DurationTag((long) as(Bee.class).getCannotEnterHiveTicks());
    }

    @Override
    public void setPropertyValue(DurationTag param, Mechanism mechanism) {
        as(Bee.class).setCannotEnterHiveTicks(param.getTicksAsInt());
    }

    @Override
    public String getPropertyId() {
        return "cannot_enter_hive";
    }

    public static void register() {
        autoRegister("cannot_enter_hive", EntityCannotEnterHive.class, DurationTag.class, false);
    }
}
