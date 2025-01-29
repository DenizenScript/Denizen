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
    // Controls whether an entity is charged:
    //- For a wither skull, this is whether it's blue.
    //- For a vex, this is whether it has red lines.
    //- For a guardian, this is whether the laser is active.
    //- For a ghast, this is whether it has a red mouth and eyes.
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
