package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Entity;
import org.bukkit.entity.PigZombie;

public class EntityAnger implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        Entity bukkitEntity = ((EntityTag) entity).getBukkitEntity();
        return bukkitEntity instanceof PigZombie
                || bukkitEntity instanceof Bee;
    }

    public static EntityAnger getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAnger((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "anger"
    };

    public EntityAnger(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return new DurationTag((long) getAnger()).identify();
    }

    @Override
    public String getPropertyId() {
        return "anger";
    }

    public int getAnger() {
        if (isPigZombie()) {
            return getPigZombie().getAnger();
        }
        else {
            return getBee().getAnger();
        }
    }

    public boolean isPigZombie() {
        return entity.getBukkitEntity() instanceof PigZombie;
    }

    public PigZombie getPigZombie() {
        return (PigZombie) entity.getBukkitEntity();
    }

    public Bee getBee() {
        return (Bee) entity.getBukkitEntity();
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.anger>
        // @returns DurationTag
        // @mechanism EntityTag.anger
        // @group properties
        // @description
        // Returns the remaining anger time of a PigZombie or Bee.
        // -->
        PropertyParser.registerTag(EntityAnger.class, DurationTag.class, "anger", (attribute, object) -> {
            return new DurationTag((long) object.getAnger());
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name anger
        // @input DurationTag
        // @description
        // Changes the remaining anger time of a PigZombie or Bee.
        // @tags
        // <EntityTag.anger>
        // -->
        if (mechanism.matches("anger") && mechanism.requireObject(DurationTag.class)) {
            DurationTag duration;
            if (mechanism.getValue().isInt()) { // Soft-deprecated - backwards compatibility, as this used to use a tick count
                duration = new DurationTag(mechanism.getValue().asLong());
            }
            else {
                duration = mechanism.valueAsType(DurationTag.class);
            }
            if (isPigZombie()) {
                getPigZombie().setAnger(duration.getTicksAsInt());
            }
            else {
                getBee().setAnger(duration.getTicksAsInt());
            }
        }
    }
}
