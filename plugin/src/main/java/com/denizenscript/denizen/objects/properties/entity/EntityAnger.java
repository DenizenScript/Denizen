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

    EntityTag entity;

    @Override
    public DurationTag getPropertyValue() {
        if (entity.getBukkitEntity() instanceof Bee) {
            return new DurationTag((long) getBee().getAnger());
        }
        else if (isPigZombie()) {
            return new DurationTag((long) getPigZombie().getAnger());
        }
        return null;
    }

    @Override
    public void setPropertyValue(DurationTag param, Mechanism mechanism) {
        if (mechanism.requireObject(DurationTag.class)) {
            DurationTag duration;
            if (mechanism.getValue().isInt()) { // Soft-deprecated - backwards compatibility, as this used to use a tick count
                duration = new DurationTag(mechanism.getValue().asLong());
            }
            else {
                duration = mechanism.valueAsType(DurationTag.class);
            }
            if (isBee()) {
                getBee().setAnger(duration.getTicksAsInt());
            }
            else if (isPigZombie()) {
                getPigZombie().setAnger(duration.getTicksAsInt());
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "anger";
    }

    public static void register() {
        autoRegister("anger", EntityAnger.class, DurationTag.class, false);
    }

    public boolean isBee() {
        return entity.getBukkitEntity() instanceof Bee;
    }

    public boolean isPigZombie() {
        return entity.getBukkitEntity() instanceof PigZombie;
    }

    public Bee getBee() {
        return (Bee) entity.getBukkitEntity();
    }

    public PigZombie getPigZombie() {
        return (PigZombie) entity.getBukkitEntity();
    }
}
