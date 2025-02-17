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
    // If the entity is wither_skull, controls whether the skull is charged. Charged skulls are blue.
    // If the entity is a vex, controls whether the vex is charging. Charging vexes have red lines.
    // This is a visual effect, and does not cause the vex to actually charge at anyone.
    // If the entity is a guardian, controls whether the guardian's laser is active.
    // Note that guardians also require a target to use their laser, see <@link command attack>.
    // If the entity is a ghast, controls whether the ghast is charging. Charging ghasts have a red mouth and eyes.
    // This is a visual effect, and does not cause the ghast to actually shoot a fireball at anyone.
    // -->

    public static boolean describes(EntityTag entity) {
        return entity.getBukkitEntity() instanceof WitherSkull
                || entity.getBukkitEntity() instanceof Vex
                || entity.getBukkitEntity() instanceof Guardian
                || entity.getBukkitEntity() instanceof Ghast;
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getEntity() instanceof WitherSkull entity) {
            return new ElementTag(entity.isCharged());
        }
        else if (getEntity() instanceof Vex entity) {
            return new ElementTag(entity.isCharging());
        }
        else if (getEntity() instanceof Guardian entity) {
            return new ElementTag(entity.hasLaser());
        }
        else if (getEntity() instanceof Ghast entity) {
            return new ElementTag(entity.isCharging());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag param, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            if (getEntity() instanceof WitherSkull entity) {
                entity.setCharged(param.asBoolean());
            }
            else if (getEntity() instanceof Vex entity) {
                entity.setCharging(param.asBoolean());
            }
            else if (getEntity() instanceof Guardian entity) {
                entity.setLaser(param.asBoolean());
            }
            else if (getEntity() instanceof Ghast entity) {
                entity.setCharging(param.asBoolean());
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
