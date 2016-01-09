package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.entity.EntityMovement;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.entity.Boat;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;

public class EntitySpeed implements Property {

    public static boolean describes(dObject entity) {
        if (!(entity instanceof dEntity)) {
            return false;
        }
        dEntity ent = (dEntity) entity;
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

    public static EntitySpeed getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }

        else {
            return new EntitySpeed((dEntity) entity);
        }
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntitySpeed(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

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

    public Element getSpeed() {
        if (entity.isLivingEntity()) {
            return new Element(EntityMovement.getSpeed(entity.getBukkitEntity()));
        }
        else {
            EntityType entityType = entity.getBukkitEntityType();
            if (entityType == EntityType.BOAT) {
                return new Element(((Boat) entity.getBukkitEntity()).getMaxSpeed());
            }
            else if (entityType == EntityType.MINECART
                    || entityType == EntityType.MINECART_CHEST
                    || entityType == EntityType.MINECART_COMMAND
                    || entityType == EntityType.MINECART_FURNACE
                    || entityType == EntityType.MINECART_HOPPER
                    || entityType == EntityType.MINECART_MOB_SPAWNER
                    || entityType == EntityType.MINECART_TNT) {
                return new Element(((Minecart) entity.getBukkitEntity()).getMaxSpeed());
            }
        }
        return new Element(0.0);
    }


    ///////////
    // dObject Attributes
    ////////

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <e@entity.speed>
        // @returns Element(Decimal)
        // @mechanism dEntity.speed
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
        // @object dEntity
        // @name speed
        // @input Element(Decimal)
        // @description
        // Sets how fast the entity can move.
        // @tags
        // <e@entity.speed>
        // -->
        if (mechanism.matches("speed") && mechanism.requireDouble()) {
            double value = mechanism.getValue().asDouble();
            if (entity.isLivingEntity()) {
                EntityMovement.setSpeed(entity.getBukkitEntity(), value);
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
