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
    // - Charged wither skulls are blue.
    // - Charged vexes have red lines.
    // Note that this is a visual effect, vexes will not actually charge at players.
    // - Charged guardians have their laser active.
    // Note that guardians also require a target to use their laser, see <@link command attack>.
    // - Charged ghasts have a red mouth and eyes.
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
