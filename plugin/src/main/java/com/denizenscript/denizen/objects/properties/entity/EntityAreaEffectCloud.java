package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizen.utilities.entity.AreaEffectCloudHelper;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
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

// TODO: 1.20.6: PotionData API
public class EntityAreaEffectCloud implements Property {

    public static boolean describes(ObjectTag entity) {
        return entity instanceof EntityTag &&
                ((EntityTag) entity).getBukkitEntityType() == EntityType.AREA_EFFECT_CLOUD;
    }

    public static EntityAreaEffectCloud getFrom(ObjectTag entity) {
        if (!describes(entity)) {
            return null;
        }
        else {
            return new EntityAreaEffectCloud((EntityTag) entity);
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

    public EntityAreaEffectCloud(EntityTag ent) {
        entity = ent;
    }

    EntityTag entity;

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "area_effect_cloud";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <EntityTag.base_potion>
        // @returns ElementTag
        // @mechanism EntityTag.base_potion
        // @group properties
        // @description
        // Returns the Area Effect Cloud's base potion data.
        // In the format Type,Upgraded,Extended
        // -->
        if (attribute.startsWith("base_potion")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <EntityTag.base_potion.type>
            // @returns ElementTag
            // @group properties
            // @description
            // Returns the Area Effect Cloud's base potion type.
            // -->
            if (attribute.startsWith("type")) {
                return new ElementTag(getHelper().getBPName())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.base_potion.is_upgraded>
            // @returns ElementTag(Boolean)
            // @group properties
            // @description
            // Returns whether the Area Effect Cloud's base potion is upgraded.
            // -->
            if (attribute.startsWith("is_upgraded")) {
                return new ElementTag(getHelper().getBPUpgraded())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.base_potion.is_extended>
            // @returns ElementTag(Boolean)
            // @group properties
            // @description
            // Returns whether the Area Effect Cloud's base potion is extended.
            // -->
            if (attribute.startsWith("is_extended")) {
                return new ElementTag(getHelper().getBPExtended())
                        .getObjectAttribute(attribute.fulfill(1));
            }
            return new ElementTag(getHelper().getBPName() + "," + getHelper().getBPUpgraded() + "," + getHelper().getBPExtended())
                    .getObjectAttribute(attribute);
        }

        // <--[tag]
        // @attribute <EntityTag.particle>
        // @returns ElementTag
        // @mechanism EntityTag.particle
        // @group properties
        // @description
        // Returns the Area Effect Cloud's particle.
        // -->
        if (attribute.startsWith("particle")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <EntityTag.particle.color>
            // @returns ColorTag
            // @group properties
            // @description
            // Returns the Area Effect Cloud's particle color.
            // -->
            if (attribute.startsWith("color")) {
                return BukkitColorExtensions.fromColor(getHelper().getColor())
                        .getObjectAttribute(attribute.fulfill(1));
            }
            return new ElementTag(getHelper().getParticle())
                    .getObjectAttribute(attribute);
        }

        // <--[tag]
        // @attribute <EntityTag.duration>
        // @returns DurationTag
        // @mechanism EntityTag.duration
        // @group properties
        // @description
        // Returns the Area Effect Cloud's duration.
        // -->
        if (attribute.startsWith("duration")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <EntityTag.duration.on_use>
            // @returns DurationTag
            // @group properties
            // @description
            // Returns the duration the Area Effect Cloud
            // will increase by when it applies an effect to an entity.
            // -->
            if (attribute.startsWith("on_use")) {
                return new DurationTag(getHelper().getDurationOnUse())
                        .getObjectAttribute(attribute.fulfill(1));
            }
            return new DurationTag(getHelper().getDuration())
                    .getObjectAttribute(attribute);
        }

        // <--[tag]
        // @attribute <EntityTag.radius>
        // @returns ElementTag(Decimal)
        // @mechanism EntityTag.radius
        // @group properties
        // @description
        // Returns the Area Effect Cloud's radius.
        // -->
        if (attribute.startsWith("radius")) {
            attribute = attribute.fulfill(1);

            // <--[tag]
            // @attribute <EntityTag.radius.on_use>
            // @returns ElementTag(Decimal)
            // @group properties
            // @description
            // Returns the amount the Area Effect Cloud's radius
            // will increase by when it applies an effect to an entity.
            // -->
            if (attribute.startsWith("on_use")) {
                return new ElementTag(getHelper().getRadiusOnUse())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.radius.per_tick>
            // @returns ElementTag(Decimal)
            // @group properties
            // @description
            // Returns the amount the Area Effect Cloud's radius
            // will increase by every tick.
            // -->
            if (attribute.startsWith("per_tick")) {
                return new ElementTag(getHelper().getRadiusPerTick())
                        .getObjectAttribute(attribute.fulfill(1));
            }
            return new ElementTag(getHelper().getRadius())
                    .getObjectAttribute(attribute);
        }

        // <--[tag]
        // @attribute <EntityTag.reapplication_delay>
        // @returns DurationTag
        // @mechanism EntityTag.reapplication_delay
        // @group properties
        // @description
        // Returns the duration an entity will be immune
        // from the Area Effect Cloud's subsequent exposure.
        // -->
        if (attribute.startsWith("reapplication_delay")) {
            return new DurationTag(getHelper().getReappDelay())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.wait_time>
        // @returns DurationTag
        // @mechanism EntityTag.wait_time
        // @group properties
        // @description
        // Returns the duration before the Area Effect Cloud starts applying potion effects.
        // -->
        if (attribute.startsWith("wait_time")) {
            return new DurationTag(getHelper().getWaitTime())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.has_custom_effect[(<effect>)]>
        // @returns ElementTag(Boolean)
        // @mechanism EntityTag.custom_effects
        // @group properties
        // @description
        // Returns whether the Area Effect Cloud has a specified effect.
        // If no effect is specified, returns whether it has any custom effect.
        // -->
        if (attribute.startsWith("has_custom_effect")) {
            if (attribute.hasParam()) {
                PotionEffectType effectType = PotionEffectType.getByName(attribute.getParam());
                for (PotionEffect effect : getHelper().getCustomEffects()) {
                    if (effect.getType().equals(effectType)) {
                        return new ElementTag(true).getObjectAttribute(attribute.fulfill(1));
                    }
                }
                return new ElementTag(false).getObjectAttribute(attribute.fulfill(1));
            }
            return new ElementTag(getHelper().hasCustomEffects())
                    .getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <EntityTag.source>
        // @returns EntityTag
        // @mechanism EntityTag.source
        // @group properties
        // @description
        // Returns the source of the Area Effect Cloud.
        // -->
        if (attribute.startsWith("source")) {
            ProjectileSource shooter = getHelper().getSource();
            if (shooter instanceof LivingEntity) {
                return new EntityTag((LivingEntity) shooter).getDenizenObject()
                        .getObjectAttribute(attribute.fulfill(1));
            }
        }

        // <--[tag]
        // @attribute <EntityTag.custom_effects>
        // @returns ListTag
        // @mechanism EntityTag.custom_effects
        // @group properties
        // @description
        // Returns a ListTag of the Area Effect Cloud's custom effects
        // In the form Type,Amplifier,Duration,Ambient,Particles|...
        // -->
        if (attribute.startsWith("custom_effects")) {
            List<PotionEffect> effects = getHelper().getCustomEffects();
            if (!attribute.hasParam()) {
                ListTag list = new ListTag();
                for (PotionEffect effect : effects) {
                    list.add(effect.getType().getName() + "," +
                            effect.getAmplifier() + "," +
                            new DurationTag((long) effect.getDuration()).identify() + "," +
                            effect.isAmbient() + "," +
                            effect.hasParticles());
                }
                return list.getObjectAttribute(attribute.fulfill(1));
            }
            int val = attribute.getIntParam() - 1;
            if (val < 0 || val >= effects.size()) {
                return null;
            }
            attribute = attribute.fulfill(1);
            PotionEffect effect = effects.get(val);

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].type>
            // @returns ElementTag
            // @group properties
            // @description
            // Returns the specified Area Effect Cloud potion effect type.
            // -->
            if (attribute.startsWith("type")) {
                return new ElementTag(effect.getType().getName())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].amplifier>
            // @returns ElementTag(Number)
            // @group properties
            // @description
            // Returns the specified Area Effect Cloud potion effect amplifier.
            // -->
            if (attribute.startsWith("amplifier")) {
                return new ElementTag(effect.getAmplifier())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].duration>
            // @returns DurationTag
            // @group properties
            // @description
            // Returns the specified Area Effect Cloud potion effect duration.
            // -->
            if (attribute.startsWith("duration")) {
                return new DurationTag((long) effect.getDuration())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].has_particles>
            // @returns ElementTag(Boolean)
            // @group properties
            // @description
            // Returns whether the specified Area Effect Cloud potion effect has particles.
            // -->
            if (attribute.startsWith("has_particles")) {
                return new ElementTag(effect.hasParticles())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].is_ambient>
            // @returns ElementTag(Boolean)
            // @group properties
            // @description
            // Returns whether the specified Area Effect Cloud potion effect is ambient.
            // -->
            if (attribute.startsWith("is_ambient")) {
                return new ElementTag(effect.isAmbient())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            return new ElementTag(effect.getType().getName() + "," +
                    effect.getAmplifier() + "," +
                    new DurationTag((long) effect.getDuration()).identify() + "," +
                    effect.isAmbient() + "," +
                    effect.hasParticles()).getObjectAttribute(attribute);
        }

        return null;
    }

    public AreaEffectCloudHelper getHelper() {
        return new AreaEffectCloudHelper(entity.getBukkitEntity());
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name clear_custom_effects
        // @input None
        // @description
        // Clears all custom effects from the Area Effect Cloud
        // @tags
        // <EntityTag.custom_effects>
        // -->
        if (mechanism.matches("clear_custom_effects")) {
            getHelper().clearEffects();
        }

        // <--[mechanism]
        // @object EntityTag
        // @name remove_custom_effect
        // @input ElementTag
        // @description
        // Removes the specified custom effect from the Area Effect Cloud
        // @tags
        // <EntityTag.custom_effects>
        // -->
        if (mechanism.matches("remove_custom_effect")) {
            PotionEffectType type = PotionEffectType.getByName(mechanism.getValue().asString().toUpperCase());
            if (type != null) {
                getHelper().removeEffect(type);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name custom_effects
        // @input ListTag
        // @description
        // Adds a list of custom potion effects to the Area Effect Cloud
        // In the form Type,Amplifier,Duration(,Ambient,Particles)|...
        // @tags
        // <EntityTag.custom_effects>
        // -->
        if (mechanism.matches("custom_effects")) {
            ListTag list = mechanism.valueAsType(ListTag.class);
            getHelper().clearEffects();

            for (String item : list) {
                List<String> potionData = CoreUtilities.split(item, ',', 5);
                if (potionData.size() >= 3) {
                    PotionEffectType type = PotionEffectType.getByName(potionData.get(0));
                    ElementTag amplifier = new ElementTag(potionData.get(1));
                    DurationTag duration = DurationTag.valueOf(potionData.get(2), mechanism.context);
                    ElementTag ambient = new ElementTag((potionData.size() > 3) ? potionData.get(3) : "false");
                    ElementTag particles = new ElementTag((potionData.size() > 4) ? potionData.get(4) : "true");

                    if (type == null || duration == null || !amplifier.isInt() || !ambient.isBoolean() || !particles.isBoolean()) {
                        mechanism.echoError(item + " is not a valid potion effect!");
                    }
                    else {
                        getHelper().addEffect(
                                new PotionEffect(type, duration.getTicksAsInt(), amplifier.asInt(),
                                        ambient.asBoolean(), particles.asBoolean()), true);
                    }
                }
                else {
                    mechanism.echoError(item + " is not a valid potion effect!");
                }
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name particle_color
        // @input ColorTag
        // @description
        // Sets the Area Effect Cloud's particle color.
        // @tags
        // <EntityTag.particle.color>
        // -->
        if (mechanism.matches("particle_color") && mechanism.requireObject(ColorTag.class)) {
            getHelper().setColor(BukkitColorExtensions.getColor(mechanism.valueAsType(ColorTag.class)));
        }

        // <--[mechanism]
        // @object EntityTag
        // @name base_potion
        // @input ElementTag
        // @description
        // Sets the Area Effect Cloud's base potion.
        // In the form: Type,Upgraded,Extended
        // NOTE: Potion cannot be both upgraded and extended
        // @tags
        // <EntityTag.base_potion>
        // <EntityTag.base_potion.type>
        // <EntityTag.base_potion.is_upgraded>
        // <EntityTag.base_potion.is_extended>
        // <server.potion_types>
        // -->
        if (mechanism.matches("base_potion")) {
            List<String> data = CoreUtilities.split(mechanism.getValue().asString().toUpperCase(), ',');
            if (data.size() != 3) {
                mechanism.echoError(mechanism.getValue().asString() + " is not a valid base potion!");
            }
            else {
                try {
                    PotionType type = PotionType.valueOf(data.get(0));
                    boolean upgraded = type.isUpgradeable() && CoreUtilities.equalsIgnoreCase(data.get(1), "true");
                    boolean extended = type.isExtendable() && CoreUtilities.equalsIgnoreCase(data.get(2), "true");
                    if (extended && upgraded) {
                        mechanism.echoError("Potion cannot be both upgraded and extended");
                    }
                    else {
                        getHelper().setBP(type, extended, upgraded);
                    }
                }
                catch (Exception e) {
                    mechanism.echoError(mechanism.getValue().asString() + " is not a valid base potion!");
                }
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name duration
        // @input DurationTag
        // @description
        // Sets the Area Effect Cloud's duration.
        // @tags
        // <EntityTag.duration>
        // -->
        if (mechanism.matches("duration") && mechanism.requireObject(DurationTag.class)) {
            getHelper().setDuration(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name duration_on_use
        // @input DurationTag
        // @description
        // Sets the duration the Area Effect Cloud
        // will increase by when it applies an effect to an entity.
        // @tags
        // <EntityTag.duration.on_use>
        // -->
        if (mechanism.matches("duration_on_use") && mechanism.requireObject(DurationTag.class)) {
            getHelper().setDurationOnUse(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name particle
        // @input ElementTag
        // @description
        // Sets the particle of the Area Effect Cloud
        // @tags
        // <EntityTag.particle>
        // -->
        if (mechanism.matches("particle") && mechanism.hasValue()) {
            getHelper().setParticle(mechanism.getValue().asString().toUpperCase());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name radius
        // @input ElementTag(Decimal)
        // @description
        // Sets the radius of the Area Effect Cloud
        // @tags
        // <EntityTag.radius>
        // -->
        if (mechanism.matches("radius") && mechanism.requireFloat()) {
            getHelper().setRadius(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name radius_on_use
        // @input ElementTag(Decimal)
        // @description
        // Sets the radius the Area Effect Cloud
        // will increase by when it applies an effect to an entity.
        // @tags
        // <EntityTag.radius.on_use>
        // -->
        if (mechanism.matches("radius_on_use") && mechanism.requireFloat()) {
            getHelper().setRadiusOnUse(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name radius_per_tick
        // @input ElementTag(Decimal)
        // @description
        // Sets the radius the Area Effect Cloud
        // will increase by every tick.
        // @tags
        // <EntityTag.radius.per_tick>
        // -->
        if (mechanism.matches("radius_per_tick") && mechanism.requireFloat()) {
            getHelper().setRadiusPerTick(mechanism.getValue().asFloat());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name reapplication_delay
        // @input DurationTag
        // @description
        // Sets the duration an entity will be immune
        // from the Area Effect Cloud's subsequent exposure.
        // @tags
        // <EntityTag.reapplication_delay>
        // -->
        if (mechanism.matches("reapplication_delay") && mechanism.requireObject(DurationTag.class)) {
            getHelper().setReappDelay(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name source
        // @input EntityTag
        // @description
        // Sets the source of the Area Effect Cloud
        // @tags
        // <EntityTag.source>
        // -->
        if (mechanism.matches("source") && mechanism.requireObject(EntityTag.class)) {
            getHelper().setSource((ProjectileSource) mechanism.valueAsType(EntityTag.class).getBukkitEntity());
        }

        // <--[mechanism]
        // @object EntityTag
        // @name wait_time
        // @input DurationTag
        // @description
        // Sets the duration before the Area Effect Cloud starts applying potion effects.
        // @tags
        // <EntityTag.wait_time>
        // -->
        if (mechanism.matches("wait_time") && mechanism.requireObject(DurationTag.class)) {
            getHelper().setWaitTime(mechanism.valueAsType(DurationTag.class).getTicksAsInt());
        }
    }
}
