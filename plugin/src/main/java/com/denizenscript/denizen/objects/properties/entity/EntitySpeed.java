package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
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
        else {
            EntityType entityType = ent.getBukkitEntityType();
            return entityType == EntityType.BOAT
                    || entityType == EntityType.MINECART
                    || entityType == EntityType.MINECART_CHEST
                    || entityType == EntityType.MINECART_COMMAND
                    || entityType == EntityType.MINECART_FURNACE
                    || entityType == EntityType.MINECART_HOPPER
                    || entityType == EntityType.MINECART_MOB_SPAWNER
                    || entityType == EntityType.MINECART_TNT;
        }
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


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySpeed(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    /////////
    // Property Methods
    ///////

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
            return new ElementTag(NMSHandler.getInstance().getEntityHelper().getSpeed(entity.getBukkitEntity()));
        }
        else {
            EntityType entityType = entity.getBukkitEntityType();
            if (entityType == EntityType.BOAT) {
                return new ElementTag(((Boat) entity.getBukkitEntity()).getMaxSpeed());
            }
            else if (entityType == EntityType.MINECART
                    || entityType == EntityType.MINECART_CHEST
                    || entityType == EntityType.MINECART_COMMAND
                    || entityType == EntityType.MINECART_FURNACE
                    || entityType == EntityType.MINECART_HOPPER
                    || entityType == EntityType.MINECART_MOB_SPAWNER
                    || entityType == EntityType.MINECART_TNT) {
                return new ElementTag(((Minecart) entity.getBukkitEntity()).getMaxSpeed());
            }
        }
        return new ElementTag(0.0);
    }


    ///////////
    // ObjectTag Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

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
        // -->
        if (attribute.startsWith("speed")) {
            return getSpeed().getAttribute(attribute.fulfill(1));
        }


        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name speed
        // @input Element(Decimal)
        // @description
        // Sets how fast the entity can move.
        // @tags
        // <EntityTag.speed>
        // -->
        if (mechanism.matches("speed") && mechanism.requireDouble()) {
            double value = mechanism.getValue().asDouble();
            if (entity.isLivingEntity()) {
                NMSHandler.getInstance().getEntityHelper().setSpeed(entity.getBukkitEntity(), value);
            }
            else {
                EntityType entityType = entity.getBukkitEntityType();
                if (entityType == EntityType.BOAT) {
                    ((Boat) entity.getBukkitEntity()).setMaxSpeed(value);
                }
                else if (entityType == EntityType.MINECART
                        || entityType == EntityType.MINECART_CHEST
                        || entityType == EntityType.MINECART_COMMAND
                        || entityType == EntityType.MINECART_FURNACE
                        || entityType == EntityType.MINECART_HOPPER
                        || entityType == EntityType.MINECART_MOB_SPAWNER
                        || entityType == EntityType.MINECART_TNT) {
                    ((Minecart) entity.getBukkitEntity()).setMaxSpeed(value);
                }
            }
        }

    }
}
