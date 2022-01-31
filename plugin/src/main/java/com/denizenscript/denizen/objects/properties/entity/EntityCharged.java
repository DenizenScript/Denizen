package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.entity.Vex;
import org.bukkit.entity.WitherSkull;

public class EntityCharged implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && (((EntityTag) entity).getBukkitEntity() instanceof WitherSkull
                || ((EntityTag) entity).getBukkitEntity() instanceof Vex);
    }

    public static EntityCharged getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityCharged((EntityTag) entity);
        }
    }

    public static final String[] handledMechs = new String[] {
            "charged"
    };

    private EntityCharged(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return isCharged() ? "true" : "false";
    }

    @Override
    public String getPropertyId() {
        return "charged";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.charged>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.charged
        // @group properties
        // @description
        // If the entity is wither_skull, returns whether the skull is charged. Charged skulls are blue.
        // If the entity is a vex, returns whether the vex is charging. Charging vexes have red lines.
        // -->
        PropertyParser.<EntityCharged, ElementTag>registerTag(ElementTag.class, "charged", (attribute, object) -> {
            return new ElementTag(object.isCharged());
        });
    }

    public boolean isWitherSkull() {
        return entity.getBukkitEntity() instanceof WitherSkull;
    }

    public boolean isVex() {
        return entity.getBukkitEntity() instanceof Vex;
    }

    public WitherSkull getWitherSkull() {
        return (WitherSkull) entity.getBukkitEntity();
    }

    public Vex getVex() {
        return (Vex) entity.getBukkitEntity();
    }

    public boolean isCharged() {
        if (isWitherSkull()) {
            return getWitherSkull().isCharged();
        }
        else if (isVex()) {
            return getVex().isCharging();
        }
        return false;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name charged
        // @input ElementTag(Boolean)
        // @description
        // If the entity is wither_skull, changes whether the skull is charged. Charged skulls are blue.
        // @tags
        // <EntityTag.charged>
        // -->
        if (mechanism.matches("charged") && mechanism.requireBoolean()) {
            if (isWitherSkull()) {
                getWitherSkull().setCharged(mechanism.getValue().asBoolean());
            }
            else if (isVex()) {
                getVex().setCharging(mechanism.getValue().asBoolean());
            }
        }
    }
}
