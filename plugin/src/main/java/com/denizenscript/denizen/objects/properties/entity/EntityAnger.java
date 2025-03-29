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
        return entity.getBukkitEntity() instanceof PigZombie
                || entity.getBukkitEntity() instanceof Bee;
    }

    @Override
    public DurationTag getPropertyValue() {
        if (getEntity() instanceof PigZombie entity) {
            return new DurationTag((long) entity.getAnger());
        }
        else if (getEntity() instanceof Bee entity) {
            return new DurationTag((long) entity.getAnger());
        }
        else {
            return null;
        }
    }

    @Override
    public void setPropertyValue(DurationTag param, Mechanism mechanism) {
        if (mechanism.getValue().isInt()) { // Soft-deprecated - backwards compatibility, as this used to use a tick count
            param = new DurationTag(mechanism.getValue().asLong());
        }
        if (getEntity() instanceof PigZombie entity) {
            entity.setAnger(param.getTicksAsInt());
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
