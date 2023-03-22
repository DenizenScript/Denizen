package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Minecart;

public class EntitySpeed implements Property {

    public static boolean describes(ObjectTag entity) {
        if (!(entity instanceof EntityTag)) {
            return false;
        }
        EntityTag ent = (EntityTag) entity;
        if (ent.isLivingEntity()) {
            return true;
        }
        return ent.getBukkitEntity() instanceof Boat || ent.getBukkitEntity() instanceof Minecart;
    }

    public static EntitySpeed getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntitySpeed((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "speed"
    };

    public static final String[] handledMechs = new String[] {
            "speed"
    };

    public EntitySpeed(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return getSpeed().asString();
    }

    @Override
    public String getPropertyId() {
        return "speed";
    }

    public ElementTag getSpeed() {
        if (entity.isLivingEntity()) {
            return new ElementTag(entity.getLivingEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue());
        }
        else {
            if (entity.getBukkitEntity() instanceof Boat) {
                return new ElementTag(((Boat) entity.getBukkitEntity()).getMaxSpeed());
            }
            else if (entity.getBukkitEntity() instanceof Minecart) {
                return new ElementTag(((Minecart) entity.getBukkitEntity()).getMaxSpeed());
            }
        }
        return new ElementTag(0.0);
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.speed>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.speed
        // @group attributes
        // @description
        // Returns how fast the entity can move.
        // Compatible with minecarts, boats, and living entities.
        // -->
        if (attribute.startsWith("speed")) {
            return getSpeed().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name speed
        // @input ElementTag(Decimal)
        // @description
        // Sets how fast the entity can move.
        // Compatible with minecarts, boats, and living entities.
        // @tags
        // <EntityTag.speed>
        // -->
        if (mechanism.matches("speed") && mechanism.requireDouble()) {
            double value = mechanism.getValue().asDouble();
            if (entity.isLivingEntity()) {
                entity.getLivingEntity().getAttribute(org.bukkit.attribute.Attribute.GENERIC_MOVEMENT_SPEED).setBaseValue(value);
            }
            else {
                if (entity.getBukkitEntity() instanceof Boat) {
                    ((Boat) entity.getBukkitEntity()).setMaxSpeed(value);
                }
                else if (entity.getBukkitEntity() instanceof Minecart) {
                    ((Minecart) entity.getBukkitEntity()).setMaxSpeed(value);
                }
            }
        }

    }
}
