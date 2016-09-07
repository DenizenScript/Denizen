package net.aufdemrand.denizen.objects.properties.entity;

import net.aufdemrand.denizen.npc.traits.HealthTrait;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;

import java.util.List;

public class EntityHealth implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).isLivingEntity();
    }

    public static EntityHealth getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }

        else {
            return new EntityHealth((dEntity) entity);
        }
    }


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityHealth(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return String.valueOf(entity.getLivingEntity().getHealth() + "/" + entity.getLivingEntity().getMaxHealth());
    }

    @Override
    public String getPropertyId() {
        return "health_data";
    }

    public static String getHealthFormatted(dEntity entity, Attribute attribute) {
        double maxHealth = entity.getLivingEntity().getMaxHealth();
        if (attribute.hasContext(2)) {
            maxHealth = attribute.getIntContext(2);
        }
        if ((float) entity.getLivingEntity().getHealth() / maxHealth < .10) {
            return new Element("dying").getAttribute(attribute.fulfill(2));
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < .40) {
            return new Element("seriously wounded").getAttribute(attribute.fulfill(2));
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < .75) {
            return new Element("injured").getAttribute(attribute.fulfill(2));
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < 1) {
            return new Element("scraped").getAttribute(attribute.fulfill(2));
        }

        else {
            return new Element("healthy").getAttribute(attribute.fulfill(2));
        }
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
        // @attribute <e@entity.health.formatted>
        // @returns Element
        // @group attributes
        // @description
        // Returns a formatted value of the player's current health level.
        // May be 'dying', 'seriously wounded', 'injured', 'scraped', or 'healthy'.
        // -->
        if (attribute.startsWith("health.formatted")) {
            return getHealthFormatted(entity, attribute);
        }

        // <--[tag]
        // @attribute <e@entity.health.max>
        // @returns Element(Decimal)
        // @group attributes
        // @description
        // Returns the maximum health of the entity.
        // -->
        if (attribute.startsWith("health.max")) {
            return new Element(entity.getLivingEntity().getMaxHealth())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.health.percentage>
        // @returns Element(Decimal)
        // @group attributes
        // @description
        // Returns the entity's current health as a percentage.
        // -->
        if (attribute.startsWith("health.percentage")) {
            double maxHealth = entity.getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2)) {
                maxHealth = attribute.getIntContext(2);
            }
            return new Element((entity.getLivingEntity().getHealth() / maxHealth) * 100)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.health>
        // @returns Element(Decimal)
        // @group attributes
        // @description
        // Returns the current health of the entity.
        // -->
        if (attribute.startsWith("health")) {
            return new Element(entity.getLivingEntity().getHealth())
                    .getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name max_health
        // @input Element(Number)
        // @description
        // Sets the maximum health the entity may have.
        // The entity must be living.
        // @tags
        // <e@entity.health>
        // <e@entity.health.max>
        // -->
        if (mechanism.matches("max_health") && mechanism.requireDouble()) {
            if (entity.isCitizensNPC()) {
                if (entity.getDenizenNPC().getCitizen().hasTrait(HealthTrait.class)) {
                    entity.getDenizenNPC().getCitizen().getTrait(HealthTrait.class).setMaxhealth(mechanism.getValue().asInt());
                }
                else {
                    dB.echoError("NPC doesn't have health trait!");
                }
            }
            else if (entity.isLivingEntity()) {
                entity.getLivingEntity().setMaxHealth(mechanism.getValue().asDouble());
            }
            else {
                dB.echoError("Entity is not alive!");
            }
        }


        // <--[mechanism]
        // @object dEntity
        // @name health_data
        // @input Element(Decimal)/Element(Decimal)
        // @description
        // Sets the amount of health the entity has, and the maximum health it has.
        // The entity must be living.
        // @tags
        // <e@entity.health>
        // <e@entity.health.max>
        // -->
        if (mechanism.matches("health_data")) {
            if (entity.isLivingEntity()) {
                List<String> values = CoreUtilities.split(mechanism.getValue().asString(), '/');
                entity.getLivingEntity().setMaxHealth(Double.valueOf(values.get(1)));
                entity.getLivingEntity().setHealth(Double.valueOf(values.get(0)));
            }
            else {
                dB.echoError("Entity is not alive!");
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name health
        // @input Element(Decimal)
        // @description
        // Sets the amount of health the entity has.
        // The entity must be living.
        // @tags
        // <e@entity.health>
        // <e@entity.health.max>
        // -->
        if (mechanism.matches("health") && mechanism.requireDouble()) {
            if (entity.isLivingEntity()) {
                entity.getLivingEntity().setHealth(mechanism.getValue().asDouble());
            }
            else {
                dB.echoError("Entity is not alive!");
            }
        }
    }
}
