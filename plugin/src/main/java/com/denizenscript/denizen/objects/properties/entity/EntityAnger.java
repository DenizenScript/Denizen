package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import org.bukkit.entity.Bee;
import org.bukkit.entity.PigZombie;

public class EntityAnger extends EntityProperty<DurationTag> {

    // <--[property]
    // @object EntityTag
    // @name anger
    // @input DurationTag
    // @description
    // Controls the remaining anger time of a PigZombie or Bee.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof Bee
                || entity.getBukkitEntity() instanceof PigZombie;
    }

    @Override
    public DurationTag getPropertyValue() {
        if (getEntity() instanceof PigZombie pigZombie) {
            return new DurationTag((long) pigZombie.getAnger());
        }
        else {
            return new DurationTag((long) as(Bee.class).getAnger());
        }
    }

    @Override
    public void setPropertyValue(DurationTag param, Mechanism mechanism) {
        if (getEntity() instanceof PigZombie pigZombie) {
            pigZombie.setAnger(param.getTicksAsInt());
        }
        else {
            as(Bee.class).setAnger(param.getTicksAsInt());
        }
    }

    @Override
    public String getPropertyId() {
        return "anger";
    }

    public static void register() {
        autoRegister("anger", EntityAnger.class, DurationTag.class, false);
    }
}
