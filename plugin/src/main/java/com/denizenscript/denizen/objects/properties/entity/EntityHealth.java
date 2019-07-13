package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.npc.traits.HealthTrait;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;

import java.util.List;

public class EntityHealth implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof dEntity
                && ((dEntity) entity).isLivingEntity();
    }

    public static EntityHealth getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityHealth((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "health"
    };

    public static final String[] handledMechs = new String[] {
            "max_health", "health_data", "health"
    };


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
        return entity.getLivingEntity().getHealth() + "/" + entity.getLivingEntity().getMaxHealth();
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
            return new ElementTag("dying").getAttribute(attribute.fulfill(2));
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < .40) {
            return new ElementTag("seriously wounded").getAttribute(attribute.fulfill(2));
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < .75) {
            return new ElementTag("injured").getAttribute(attribute.fulfill(2));
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < 1) {
            return new ElementTag("scraped").getAttribute(attribute.fulfill(2));
        }
        else {
            return new ElementTag("healthy").getAttribute(attribute.fulfill(2));
        }
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
        // @attribute <e@entity.health.formatted>
        // @returns ElementTag
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
        // @returns ElementTag(Decimal)
        // @group attributes
        // @description
        // Returns the maximum health of the entity.
        // -->
        if (attribute.startsWith("health.max")) {
            return new ElementTag(entity.getLivingEntity().getMaxHealth())
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.health.percentage>
        // @returns ElementTag(Decimal)
        // @group attributes
        // @description
        // Returns the entity's current health as a percentage.
        // -->
        if (attribute.startsWith("health.percentage")) {
            double maxHealth = entity.getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2)) {
                maxHealth = attribute.getIntContext(2);
            }
            return new ElementTag((entity.getLivingEntity().getHealth() / maxHealth) * 100)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.health>
        // @returns ElementTag(Decimal)
        // @group attributes
        // @description
        // Returns the current health of the entity.
        // -->
        if (attribute.startsWith("health")) {
            return new ElementTag(entity.getLivingEntity().getHealth())
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
                    Debug.echoError("NPC doesn't have health trait!");
                }
            }
            else if (entity.isLivingEntity()) {
                entity.getLivingEntity().setMaxHealth(mechanism.getValue().asDouble());
            }
            else {
                Debug.echoError("Entity is not alive!");
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
                Debug.echoError("Entity is not alive!");
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
                Debug.echoError("Entity is not alive!");
            }
        }
    }
}
