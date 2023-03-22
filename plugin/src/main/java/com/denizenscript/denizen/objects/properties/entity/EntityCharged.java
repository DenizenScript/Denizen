package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Guardian;
import org.bukkit.entity.Vex;
import org.bukkit.entity.WitherSkull;

public class EntityCharged implements Property {

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag)) {
            return false;
        }
        Entity entity = ((EntityTag) object).getBukkitEntity();
        return entity instanceof WitherSkull
                || entity instanceof Vex
                || entity instanceof Guardian;
    }

    public static EntityCharged getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCharged((EntityTag) entity);
        }
    }

    public EntityCharged(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        if (isGuardian()) {
            return getGuardian().hasLaser() ? "true" : null;
        }
        return String.valueOf(isCharged());
    }

    @Override
    public String getPropertyId() {
        return "charged";
    }

    public static void register() {

        // <--[tag]
        // @attribute <EntityTag.charged>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.charged
        // @group properties
        // @description
        // If the entity is wither_skull, returns whether the skull is charged. Charged skulls are blue.
        // If the entity is a vex, returns whether the vex is charging. Charging vexes have red lines.
        // If the entity is a guardian, returns whether the guardian's laser is active.
        // -->
        PropertyParser.registerTag(EntityCharged.class, ElementTag.class, "charged", (attribute, object) -> {
            return new ElementTag(object.isCharged());
        });


        // <--[mechanism]
        // @object EntityTag
        // @name charged
        // @input ElementTag(Boolean)
        // @description
        // If the entity is wither_skull, sets whether the skull is charged. Charged skulls are blue.
        // If the entity is a vex, sets whether the vex is charging. Charging vexes have red lines.
        // This is a visual effect, and does not cause the vex to actually charge at anyone.
        // If the entity is a guardian, sets whether the guardian's laser is active.
        // Note that guardians require a target to use their laser, see <@link command attack>.
        // @tags
        // <EntityTag.charged>
        // -->
        PropertyParser.registerMechanism(EntityCharged.class, ElementTag.class, "charged", (object, mechanism, input) -> {
            if (!mechanism.requireBoolean()) {
                return;
            }
            if (object.isWitherSkull()) {
                object.getWitherSkull().setCharged(input.asBoolean());
            }
            else if (object.isVex()) {
                object.getVex().setCharging(input.asBoolean());
            }
            else if (object.isGuardian()) {
                object.getGuardian().setLaser(input.asBoolean());
            }
        });
    }

    public boolean isWitherSkull() {
        return entity.getBukkitEntity() instanceof WitherSkull;
    }

    public boolean isVex() {
        return entity.getBukkitEntity() instanceof Vex;
    }

    public boolean isGuardian() {
        return entity.getBukkitEntity() instanceof Guardian;
    }

    public WitherSkull getWitherSkull() {
        return (WitherSkull) entity.getBukkitEntity();
    }

    public Vex getVex() {
        return (Vex) entity.getBukkitEntity();
    }

    public Guardian getGuardian() {
        return (Guardian) entity.getBukkitEntity();
    }

    public boolean isCharged() {
        if (isWitherSkull()) {
            return getWitherSkull().isCharged();
        }
        else if (isVex()) {
            return getVex().isCharging();
        }
        else if (isGuardian()) {
            return getGuardian().hasLaser();
        }
        return false;
    }
}
