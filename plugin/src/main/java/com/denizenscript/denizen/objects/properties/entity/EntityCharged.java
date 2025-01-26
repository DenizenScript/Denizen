package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.entity.*;

public class EntityCharged extends EntityProperty<ElementTag> {

    // <--[property]
    // @object EntityTag
    // @name charged
    // @input ElementTag(Boolean)
    // @description
    // If the entity is a wither skull, controls whether the skull is charged. Charged skulls are blue.
    // If the entity is a vex, controls whether the vex is charging. Charging vexes have red lines.
    // If the entity is a guardian, controls whether the guardian's laser is active.
    // If the entity is a ghast, controls whether the ghast is charging. Charging ghasts have red eyes and a red mouth.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof WitherSkull
                || entity.getBukkitEntity() instanceof Vex
                || entity.getBukkitEntity() instanceof Guardian
                || entity.getBukkitEntity() instanceof Ghast;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof WitherSkull) {
            return new ElementTag(as(WitherSkull.class).isCharged());
        }
        else if (getEntity() instanceof Vex) {
            return new ElementTag(as(Vex.class).isCharging());
        }
        else if (getEntity() instanceof Guardian) {
            return new ElementTag(as(Guardian.class).hasLaser());
        }
        else if (getEntity() instanceof Ghast) {
            return new ElementTag(as(Ghast.class).isCharging());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            if (getEntity() instanceof WitherSkull) {
                as(WitherSkull.class).setCharged(param.asBoolean());
            }
            else if (getEntity() instanceof Vex) {
                as(Vex.class).setCharging(param.asBoolean());
            }
            else if (getEntity() instanceof Guardian) {
                as(Guardian.class).setLaser(param.asBoolean());
            }
            else if (getEntity() instanceof Ghast) {
                as(Ghast.class).setCharging(param.asBoolean());
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "charged";
    }

    public static void register() {
        autoRegister("charged", EntityCharged.class, ElementTag.class, false);
    }
}
