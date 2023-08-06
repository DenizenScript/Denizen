package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.npc.traits.HealthTrait;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;

import java.util.List;

public class EntityHealth implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag
                && ((EntityTag) entity).isLivingEntity();
    }

    public static EntityHealth getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityHealth((EntityTag) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "health", "formatted_health", "health_max", "health_percentage", "health_data"
    };

    public static final String[] handledMechs = new String[] {
            "max_health", "health_data", "health"
    };

    public EntityHealth(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return CoreUtilities.doubleToString(entity.getLivingEntity().getHealth()) + "/" + CoreUtilities.doubleToString(entity.getLivingEntity().getMaxHealth());
    }

    @Override
    public String getPropertyId() {
        return "health_data";
    }

    public static ElementTag getHealthFormatted(EntityTag entity, Double maxHealth) {
        if (maxHealth == null) {
            maxHealth = entity.getLivingEntity().getMaxHealth();
        }
        if ((float) entity.getLivingEntity().getHealth() / maxHealth < .10) {
            return new ElementTag("dying");
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < .40) {
            return new ElementTag("seriously wounded");
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < .75) {
            return new ElementTag("injured");
        }
        else if ((float) entity.getLivingEntity().getHealth() / maxHealth < 1) {
            return new ElementTag("scraped");
        }
        else {
            return new ElementTag("healthy");
        }
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.formatted_health>
        // @returns ElementTag
        // @mechanism EntityTag.health_data
        // @group attributes
        // @description
        // Returns a formatted value of the player's current health level.
        // May be 'dying', 'seriously wounded', 'injured', 'scraped', or 'healthy'.
        // -->
        if (attribute.startsWith("formatted_health")) {
            return getHealthFormatted(entity, attribute.hasParam() ? attribute.getDoubleParam() : null);
        }
        if (attribute.startsWith("health.formatted")) {
            BukkitImplDeprecations.entityHealthTags.warn(attribute.context);
            return getHealthFormatted(entity, attribute.hasContext(2) ? attribute.getDoubleContext(2) : null);
        }

        // <--[tag]
        // @attribute <EntityTag.health_max>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.max_health
        // @group attributes
        // @description
        // Returns the maximum health of the entity.
        // -->
        if (attribute.startsWith("health_max")) {
            return new ElementTag(entity.getLivingEntity().getMaxHealth())
                    .getObjectAttribute(attribute.fulfill(1));
        }
        if (attribute.startsWith("health.max")) {
            BukkitImplDeprecations.entityHealthTags.warn(attribute.context);
            return new ElementTag(entity.getLivingEntity().getMaxHealth())
                    .getObjectAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <EntityTag.health_percentage>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.health
        // @group attributes
        // @description
        // Returns the entity's current health as a percentage.
        // -->
        if (attribute.startsWith("health_percentage")) {
            double maxHealth = entity.getLivingEntity().getMaxHealth();
            if (attribute.hasParam()) {
                maxHealth = attribute.getIntParam();
            }
            return new ElementTag((entity.getLivingEntity().getHealth() / maxHealth) * 100)
                    .getObjectAttribute(attribute.fulfill(1));
        }
        if (attribute.startsWith("health.percentage")) {
            BukkitImplDeprecations.entityHealthTags.warn(attribute.context);
            double maxHealth = entity.getLivingEntity().getMaxHealth();
            if (attribute.hasContext(2)) {
                maxHealth = attribute.getIntContext(2);
            }
            return new ElementTag((entity.getLivingEntity().getHealth() / maxHealth) * 100)
                    .getObjectAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <EntityTag.health_data>
        // @returns ElementTag
        // @mechanism EntityTag.health
        // @group attributes
        // @description
        // Returns the current health data of the entity, in the format of current/max.
        // -->
        if (attribute.startsWith("health_data")) {
            return new ElementTag(getPropertyString())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.health>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.health
        // @group attributes
        // @description
        // Returns the current health of the entity.
        // -->
        if (attribute.startsWith("health")) {
            return new ElementTag(entity.getLivingEntity().getHealth())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name max_health
        // @input ElementTag(Decimal)
        // @description
        // Sets the maximum health the entity may have.
        // The entity must be living.
        // Note to change the current health at the same time as max_health (might be needed when setting max health higher rather than lower),
        // use <@link mechanism EntityTag.health_data>.
        // @tags
        // <EntityTag.health>
        // <EntityTag.health_max>
        // -->
        if (mechanism.matches("max_health") && mechanism.requireDouble()) {
            if (entity.isCitizensNPC()) {
                if (entity.getDenizenNPC().getCitizen().hasTrait(HealthTrait.class)) {
                    entity.getDenizenNPC().getCitizen().getOrAddTrait(HealthTrait.class).setMaxhealth(mechanism.getValue().asInt());
                }
                else {
                    mechanism.echoError("NPC doesn't have health trait!");
                }
            }
            else if (entity.isLivingEntity()) {
                entity.getLivingEntity().setMaxHealth(mechanism.getValue().asDouble());
            }
            else {
                mechanism.echoError("Entity is not alive!");
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name health_data
        // @input ElementTag(Decimal)/ElementTag(Decimal)
        // @description
        // Sets the amount of health the entity has, and the maximum health it has.
        // The entity must be living.
        // @tags
        // <EntityTag.health>
        // <EntityTag.health_max>
        // -->
        if (mechanism.matches("health_data")) {
            if (entity.isLivingEntity()) {
                List<String> values = CoreUtilities.split(mechanism.getValue().asString(), '/');
                entity.getLivingEntity().setMaxHealth(Double.parseDouble(values.get(1)));
                entity.getLivingEntity().setHealth(Double.parseDouble(values.get(0)));
            }
            else {
                mechanism.echoError("Entity is not alive!");
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name health
        // @input ElementTag(Decimal)
        // @description
        // Sets the amount of health the entity has.
        // The entity must be living.
        // @tags
        // <EntityTag.health>
        // <EntityTag.health_max>
        // -->
        if (mechanism.matches("health") && mechanism.requireDouble()) {
            if (entity.isLivingEntity()) {
                entity.getLivingEntity().setHealth(mechanism.getValue().asDouble());
            }
            else {
                mechanism.echoError("Entity is not alive!");
            }
        }
    }
}
