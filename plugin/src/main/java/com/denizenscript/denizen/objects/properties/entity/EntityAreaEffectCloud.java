package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.utilities.entity.AreaEffectCloudHelper;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.objects.dColor;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public class EntityAreaEffectCloud implements Property {

    public static boolean describes(dObject entity) {
        return entity instanceof dEntity &&
                ((dEntity) entity).getBukkitEntityType() == EntityType.AREA_EFFECT_CLOUD;
    }

    public static EntityAreaEffectCloud getFrom(dObject entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAreaEffectCloud((dEntity) entity);
        }
    }

    public static final String[] handledTags = new String[] {
            "base_potion", "particle", "duration", "radius", "reapplication_delay", "wait_time",
            "has_custom_effect", "source", "custom_effects"
    };

    public static final String[] handledMechs = new String[] {
            "clear_custom_effects", "remove_custom_effect", "custom_effects", "particle_color",
            "base_potion", "duration", "duration_on_use", "particle", "radius", "radius_on_use",
            "radius_per_tick", "reapplication_delay", "source", "wait_time"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityAreaEffectCloud(dEntity ent) {
        entity = ent;
    }

    dEntity entity;

    /////////
    // Property Methods
    ///////

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "area_effect_cloud";
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
        // @attribute <e@entity.base_potion>
        // @returns Element
        // @group properties
        // @description
        // Returns the Area Effect Cloud's base potion data.
        // In the format Type,Upgraded,Extended
        // -->
        if (attribute.startsWith("base_potion")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <e@entity.base_potion.type>
            // @returns Element
            // @group properties
            // @description
            // Returns the Area Effect Cloud's base potion type.
            // -->
            if (attribute.startsWith("type")) {
                return new Element(getHelper().getBPName())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <e@entity.base_potion.is_upgraded>
            // @returns Element(Boolean)
            // @group properties
            // @description
            // Returns whether the Area Effect Cloud's base potion is upgraded.
            // -->
            if (attribute.startsWith("is_upgraded")) {
                return new Element(getHelper().getBPUpgraded())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <e@entity.base_potion.is_extended>
            // @returns Element(Boolean)
            // @group properties
            // @description
            // Returns whether the Area Effect Cloud's base potion is extended.
            // -->
            if (attribute.startsWith("is_extended")) {
                return new Element(getHelper().getBPExtended())
                        .getAttribute(attribute.fulfill(1));
            }

            return new Element(getHelper().getBPName() + "," + getHelper().getBPUpgraded() + "," + getHelper().getBPExtended())
                    .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <e@entity.particle>
        // @returns Element
        // @group properties
        // @description
        // Returns the Area Effect Cloud's particle.
        // -->
        if (attribute.startsWith("particle")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <e@entity.particle.color>
            // @returns dColor
            // @group properties
            // @description
            // Returns the Area Effect Cloud's particle color.
            // -->
            if (attribute.startsWith("color")) {
                return new dColor(getHelper().getColor())
                        .getAttribute(attribute.fulfill(1));
            }

            return new Element(getHelper().getParticle())
                    .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <e@entity.duration>
        // @returns Duration
        // @group properties
        // @description
        // Returns the Area Effect Cloud's duration.
        // -->
        if (attribute.startsWith("duration")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <e@entity.duration.on_use>
            // @returns Duration
            // @group properties
            // @description
            // Returns the duration the Area Effect Cloud
            // will increase by when it applies an effect to an entity.
            // -->
            if (attribute.startsWith("on_use")) {
                return new Duration(getHelper().getDurationOnUse())
                        .getAttribute(attribute.fulfill(1));
            }

            return new Duration(getHelper().getDuration())
                    .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <e@entity.radius>
        // @returns Element(Decimal)
        // @group properties
        // @description
        // Returns the Area Effect Cloud's radius.
        // -->
        if (attribute.startsWith("radius")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <e@entity.radius.on_use>
            // @returns Element(Decimal)
            // @group properties
            // @description
            // Returns the amount the Area Effect Cloud's radius
            // will increase by when it applies an effect to an entity.
            // -->
            if (attribute.startsWith("on_use")) {
                return new Element(getHelper().getRadiusOnUse())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <e@entity.radius.per_tick>
            // @returns Element(Decimal)
            // @group properties
            // @description
            // Returns the amount the Area Effect Cloud's radius
            // will increase by every tick.
            // -->
            if (attribute.startsWith("per_tick")) {
                return new Element(getHelper().getRadiusPerTick())
                        .getAttribute(attribute.fulfill(1));
            }

            return new Element(getHelper().getRadius())
                    .getAttribute(attribute);
        }

        // <--[tag]
        // @attribute <e@entity.reapplication_delay>
        // @returns Duration
        // @group properties
        // @description
        // Returns the duration an entity will be immune
        // from the Area Effect Cloud's subsequent exposure.
        // -->
        if (attribute.startsWith("reapplication_delay")) {
            return new Duration(getHelper().getReappDelay())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.wait_time>
        // @returns Duration
        // @group properties
        // @description
        // Returns the duration an entity must be exposed to
        // the Area Effect Cloud before its effect is applied.
        // -->
        if (attribute.startsWith("wait_time")) {
            return new Duration(getHelper().getWaitTime())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.has_custom_effect[<effect>]>
        // @returns Element(Boolean)
        // @group properties
        // @description
        // Returns whether the Area Effect Cloud has a specified effect.
        // If no effect is specified, returns whether it has any custom effect.
        // -->
        if (attribute.startsWith("has_custom_effect")) {

            if (attribute.hasContext(1)) {
                PotionEffectType effectType = PotionEffectType.getByName(attribute.getContext(1));
                for (PotionEffect effect : getHelper().getCustomEffects()) {
                    if (effect.getType().equals(effectType)) {
                        return new Element(true).getAttribute(attribute.fulfill(1));
                    }
                }
                return new Element(false).getAttribute(attribute.fulfill(1));
            }

            return new Element(getHelper().hasCustomEffects())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.source>
        // @returns dEntity
        // @group properties
        // @description
        // Returns the source of the Area Effect Cloud.
        // -->
        if (attribute.startsWith("source")) {
            ProjectileSource shooter = getHelper().getSource();
            if (shooter != null && shooter instanceof LivingEntity) {
                return new dEntity((LivingEntity) shooter)
                        .getAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <e@entity.custom_effects>
        // @returns dList
        // @group properties
        // @description
        // Returns a dList of the Area Effect Cloud's custom effects
        // In the form Type,Amplifier,Duration,Ambient,Particles|...
        // -->
        if (attribute.startsWith("custom_effects")) {
            List<PotionEffect> effects = getHelper().getCustomEffects();

            if (!attribute.hasContext(1)) {
                dList list = new dList();
                for (PotionEffect effect : effects) {
                    list.add(effect.getType().getName() + "," +
                            effect.getAmplifier() + "," +
                            new Duration((long) effect.getDuration()).identify() + "," +
                            effect.isAmbient() + "," +
                            effect.hasParticles());
                }
                return list.getAttribute(attribute.fulfill(1));
            }

            int val = attribute.getIntContext(1) - 1;
            if (val < 0 || val >= effects.size()) {
                return null;
            }

            attribute = attribute.fulfill(1);
            PotionEffect effect = effects.get(val);

            // <--[tag]
            // @attribute <e@entity.custom_effects[<#>].type>
            // @returns Element
            // @group properties
            // @description
            // Returns the specified Area Effect Cloud potion effect type.
            // -->
            if (attribute.startsWith("type")) {
                return new Element(effect.getType().getName())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <e@entity.custom_effects[<#>].amplifier>
            // @returns Element(Number)
            // @group properties
            // @description
            // Returns the specified Area Effect Cloud potion effect amplifier.
            // -->
            if (attribute.startsWith("amplifier")) {
                return new Element(effect.getAmplifier())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <e@entity.custom_effects[<#>].duration>
            // @returns Duration
            // @group properties
            // @description
            // Returns the specified Area Effect Cloud potion effect duration.
            // -->
            if (attribute.startsWith("duration")) {
                return new Duration((long) effect.getDuration())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <e@entity.custom_effects[<#>].has_particles>
            // @returns Element(Boolean)
            // @group properties
            // @description
            // Returns whether the specified Area Effect Cloud potion effect has particles.
            // -->
            if (attribute.startsWith("has_particles")) {
                return new Element(effect.hasParticles())
                        .getAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <e@entity.custom_effects[<#>].is_ambient>
            // @returns Element(Boolean)
            // @group properties
            // @description
            // Returns whether the specified Area Effect Cloud potion effect is ambient.
            // -->
            if (attribute.startsWith("is_ambient")) {
                return new Element(effect.isAmbient())
                        .getAttribute(attribute.fulfill(1));
            }

            return new Element(effect.getType().getName() + "," +
                    effect.getAmplifier() + "," +
                    new Duration((long) effect.getDuration()).identify() + "," +
                    effect.isAmbient() + "," +
                    effect.hasParticles()).getAttribute(attribute);
        }

        return null;
    }

    public AreaEffectCloudHelper getHelper() {
        return new AreaEffectCloudHelper(entity.getBukkitEntity());
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name clear_custom_effects
        // @input None
        // @description
        // Clears all custom effects from the Area Effect Cloud
        // @tags
        // <e@entity.custom_effects>
        // -->
        if (mechanism.matches("clear_custom_effects")) {
            getHelper().clearEffects();
        }

        // <--[mechanism]
        // @object dEntity
        // @name remove_custom_effect
        // @input Element
        // @description
        // Removes the specified custom effect from the Area Effect Cloud
        // @tags
        // <e@entity.custom_effects>
        // -->
        if (mechanism.matches("remove_custom_effect")) {
            PotionEffectType type = PotionEffectType.getByName(mechanism.getValue().asString().toUpperCase());
            if (type != null) {
                getHelper().removeEffect(type);
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name custom_effects
        // @input dList
        // @description
        // Adds a list of custom potion effects to the Area Effect Cloud
        // In the form Type,Amplifier,Duration(,Ambient,Particles)|...
        // @tags
        // <e@entity.custom_effects>
        // -->
        if (mechanism.matches("custom_effects")) {
            dList list = mechanism.valueAsType(dList.class);
            getHelper().clearEffects();

            for (String item : list) {
                List<String> potionData = CoreUtilities.split(item, ',', 5);
                if (potionData.size() >= 3) {
                    PotionEffectType type = PotionEffectType.getByName(potionData.get(0));
                    Element amplifier = new Element(potionData.get(1));
                    Duration duration = Duration.valueOf(potionData.get(2));
                    Element ambient = new Element((potionData.size() > 3) ? potionData.get(3) : "false");
                    Element particles = new Element((potionData.size() > 4) ? potionData.get(4) : "true");

                    if (type == null || duration == null || !amplifier.isInt() || !ambient.isBoolean() || !particles.isBoolean()) {
                        dB.echoError(item + " is not a valid potion effect!");
                    }
                    else {
                        getHelper().addEffect(
                                new PotionEffect(type, duration.getTicksAsInt(), amplifier.asInt(),
                                        ambient.asBoolean(), particles.asBoolean()), true);
                    }
                }
                else {
                    dB.echoError(item + " is not a valid potion effect!");
                }
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name particle_color
        // @input dColor
        // @description
        // Sets the Area Effect Cloud's particle color.
        // @tags
        // <e@entity.particle.color>
        // -->
        if (mechanism.matches("particle_color") && mechanism.requireObject(dColor.class)) {
            getHelper().setColor(dColor.valueOf(mechanism.getValue().asString()).getColor());
        }

        // <--[mechanism]
        // @object dEntity
        // @name base_potion
        // @input Element
        // @description
        // Sets the Area Effect Cloud's base potion.
        // In the form: Type,Upgraded,Extended
        // NOTE: Potion cannot be both upgraded and extended
        // @tags
        // <e@entity.base_potion>
        // <e@entity.base_potion.type>
        // <e@entity.base_potion.is_upgraded>
        // <e@entity.base_potion.is_extended>
        // <server.list_potion_types>
        // -->
        if (mechanism.matches("base_potion")) {
            List<String> data = CoreUtilities.split(mechanism.getValue().asString().toUpperCase(), ',');
            if (data.size() != 3) {
                dB.echoError(mechanism.getValue().asString() + " is not a valid base potion!");
            }
            else {
                try {
                    PotionType type = PotionType.valueOf(data.get(0));
                    boolean extended = type.isExtendable() && CoreUtilities.toLowerCase(data.get(1)).equals("true");
                    boolean upgraded = type.isUpgradeable() && CoreUtilities.toLowerCase(data.get(2)).equals("true");
                    if (extended && upgraded) {
                        dB.echoError("Potion cannot be both upgraded and extended");
                    }
                    else {
                        getHelper().setBP(type, extended, upgraded);
                    }
                }
                catch (Exception e) {
                    dB.echoError(mechanism.getValue().asString() + " is not a valid base potion!");
                }
            }
        }

        // <--[mechanism]
        // @object dEntity
        // @name duration
        // @input Duration
        // @description
        // Sets the Area Effect Cloud's duration.
        // @tags
        // <e@entity.duration>
        // -->
        if (mechanism.matches("duration") && mechanism.requireObject(Duration.class)) {
            getHelper().setDuration(Duration.valueOf(mechanism.getValue().asString()).getTicksAsInt());
        }

        // <--[mechanism]
        // @object dEntity
        // @name duration_on_use
        // @input Duration
        // @description
        // Sets the duration the Area Effect Cloud
        // will increase by when it applies an effect to an entity.
        // @tags
        // <e@entity.duration.on_use>
        // -->
        if (mechanism.matches("duration_on_use") && mechanism.requireObject(Duration.class)) {
            getHelper().setDurationOnUse(Duration.valueOf(mechanism.getValue().asString()).getTicksAsInt());
        }

        // <--[mechanism]
        // @object dEntity
        // @name particle
        // @input Element
        // @description
        // Sets the particle of the Area Effect Cloud
        // @tags
        // <e@entity.particle>
        // -->
        if (mechanism.matches("particle") && mechanism.hasValue()) {
            getHelper().setParticle(mechanism.getValue().asString().toUpperCase());
        }

        // <--[mechanism]
        // @object dEntity
        // @name radius
        // @input Element(Decimal)
        // @description
        // Sets the radius of the Area Effect Cloud
        // @tags
        // <e@entity.radius>
        // -->
        if (mechanism.matches("radius") && mechanism.requireFloat()) {
            getHelper().setRadius(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dEntity
        // @name radius_on_use
        // @input Element(Decimal)
        // @description
        // Sets the radius the Area Effect Cloud
        // will increase by when it applies an effect to an entity.
        // @tags
        // <e@entity.radius.on_use>
        // -->
        if (mechanism.matches("radius_on_use") && mechanism.requireFloat()) {
            getHelper().setRadiusOnUse(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dEntity
        // @name radius_per_tick
        // @input Element(Decimal)
        // @description
        // Sets the radius the Area Effect Cloud
        // will increase by every tick.
        // @tags
        // <e@entity.radius.per_tick>
        // -->
        if (mechanism.matches("radius_per_tick") && mechanism.requireFloat()) {
            getHelper().setRadiusPerTick(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object dEntity
        // @name reapplication_delay
        // @input Duration
        // @description
        // Sets the duration an entity will be immune
        // from the Area Effect Cloud's subsequent exposure.
        // @tags
        // <e@entity.reapplication_delay>
        // -->
        if (mechanism.matches("reapplication_delay") && mechanism.requireObject(Duration.class)) {
            getHelper().setReappDelay(Duration.valueOf(mechanism.getValue().asString()).getTicksAsInt());
        }

        // <--[mechanism]
        // @object dEntity
        // @name source
        // @input dEntity
        // @description
        // Sets the source of the Area Effect Cloud
        // @tags
        // <e@entity.source>
        // -->
        if (mechanism.matches("source") && mechanism.requireObject(dEntity.class)) {
            getHelper().setSource((ProjectileSource) dEntity.valueOf(mechanism.getValue().asString()).getBukkitEntity());
        }

        // <--[mechanism]
        // @object dEntity
        // @name wait_time
        // @input Duration
        // @description
        // Sets the duration an entity must be exposed to
        // the Area Effect Cloud before its effect is applied.
        // @tags
        // <e@entity.wait_time>
        // -->
        if (mechanism.matches("wait_time") && mechanism.requireObject(Duration.class)) {
            getHelper().setWaitTime(Duration.valueOf(mechanism.getValue().asString()).getTicksAsInt());
        }
    }
}
