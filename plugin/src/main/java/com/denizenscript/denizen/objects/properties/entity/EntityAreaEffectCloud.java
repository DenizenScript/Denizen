package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.entity.AreaEffectCloudHelper;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ColorTag;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Particle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

// TODO: most of the tags and mechs here need to become properties/be merged into existing properties
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
        // @deprecated use 'EntityTag.potion_type' on MC 1.20+.
        // @description
        // Deprecated in favor of <@link property EntityTag.potion_type> on MC 1.20+.
        // -->
        if (attribute.startsWith("base_potion")) {
            attribute = attribute.fulfill(1);
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
                BukkitImplDeprecations.areaEffectCloudControls.warn(attribute.context);
            }

            // <--[tag]
            // @attribute <EntityTag.base_potion.type>
            // @returns ElementTag
            // @deprecated use 'EntityTag.potion_type' on MC 1.20+.
            // @description
            // Deprecated in favor of <@link property EntityTag.potion_type> on MC 1.20+.
            // -->
            if (attribute.startsWith("type")) {
                return new ElementTag(getHelper().getBPName())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.base_potion.is_upgraded>
            // @returns ElementTag(Boolean)
            // @deprecated use 'EntityTag.potion_type' on MC 1.20+.
            // @description
            // Deprecated in favor of <@link property EntityTag.potion_type> on MC 1.20+.
            // -->
            if (attribute.startsWith("is_upgraded")) {
                return new ElementTag(getHelper().getBPUpgraded())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.base_potion.is_extended>
            // @returns ElementTag(Boolean)
            // @deprecated use 'EntityTag.potion_type' on MC 1.20+.
            // @description
            // Deprecated in favor of <@link property EntityTag.potion_type> on MC 1.20+.
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
            // @deprecated use 'EntityTag.color'.
            // @description
            // Deprecated in favor of <@link property EntityTag.color>.
            // -->
            if (attribute.startsWith("color")) {
                BukkitImplDeprecations.areaEffectCloudControls.warn(attribute.context);
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
        // @deprecated use 'EntityTag.has_effect'.
        // @description
        // Deprecated in favor of <@link tag EntityTag.has_effect>.
        // -->
        if (attribute.startsWith("has_custom_effect")) {
            BukkitImplDeprecations.areaEffectCloudControls.warn(attribute.context);
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
        // @deprecated use 'EntityTag.effects_data'.
        // @description
        // Deprecated in favor of <@link tag EntityTag.effects_data>.
        // -->
        if (attribute.startsWith("custom_effects")) {
            BukkitImplDeprecations.areaEffectCloudControls.warn(attribute.context);
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
            // @deprecated use 'EntityTag.effects_data'.
            // @description
            // Deprecated in favor of <@link tag EntityTag.effects_data>.
            // -->
            if (attribute.startsWith("type")) {
                return new ElementTag(effect.getType().getName())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].amplifier>
            // @returns ElementTag(Number)
            // @deprecated use 'EntityTag.effects_data'.
            // @description
            // Deprecated in favor of <@link tag EntityTag.effects_data>.
            // -->
            if (attribute.startsWith("amplifier")) {
                return new ElementTag(effect.getAmplifier())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].duration>
            // @returns DurationTag
            // @deprecated use 'EntityTag.effects_data'.
            // @description
            // Deprecated in favor of <@link tag EntityTag.effects_data>.
            // -->
            if (attribute.startsWith("duration")) {
                return new DurationTag((long) effect.getDuration())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].has_particles>
            // @returns ElementTag(Boolean)
            // @deprecated use 'EntityTag.effects_data'.
            // @description
            // Deprecated in favor of <@link tag EntityTag.effects_data>.
            // -->
            if (attribute.startsWith("has_particles")) {
                return new ElementTag(effect.hasParticles())
                        .getObjectAttribute(attribute.fulfill(1));
            }

            // <--[tag]
            // @attribute <EntityTag.custom_effects[<#>].is_ambient>
            // @returns ElementTag(Boolean)
            // @deprecated use 'EntityTag.effects_data'.
            // @description
            // Deprecated in favor of <@link tag EntityTag.effects_data>.
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
        // @deprecated use 'EntityTag.potion_effects'.
        // @description
        // Deprecated in favor of <@link mechanism EntityTag.potion_effects>.
        // @tags
        // <EntityTag.custom_effects>
        // -->
        if (mechanism.matches("clear_custom_effects")) {
            BukkitImplDeprecations.areaEffectCloudControls.warn(mechanism.context);
            getHelper().clearEffects();
        }

        // <--[mechanism]
        // @object EntityTag
        // @name remove_custom_effect
        // @input ElementTag
        // @deprecated use 'EntityTag.potion_effects'.
        // @description
        // Deprecated in favor of <@link mechanism EntityTag.potion_effects>.
        // @tags
        // <EntityTag.custom_effects>
        // -->
        if (mechanism.matches("remove_custom_effect")) {
            BukkitImplDeprecations.areaEffectCloudControls.warn(mechanism.context);
            PotionEffectType type = PotionEffectType.getByName(mechanism.getValue().asString().toUpperCase());
            if (type != null) {
                getHelper().removeEffect(type);
            }
        }

        // <--[mechanism]
        // @object EntityTag
        // @name custom_effects
        // @input ListTag
        // @deprecated use 'EntityTag.potion_effects'.
        // @description
        // Deprecated in favor of <@link mechanism EntityTag.potion_effects>.
        // @tags
        // <EntityTag.custom_effects>
        // -->
        if (mechanism.matches("custom_effects")) {
            BukkitImplDeprecations.areaEffectCloudControls.warn(mechanism.context);
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
        // @deprecated use 'EntityTag.color'.
        // @description
        // Deprecated in favor of <@link property EntityTag.color>.
        // @tags
        // <EntityTag.particle.color>
        // -->
        if (mechanism.matches("particle_color") && mechanism.requireObject(ColorTag.class)) {
            BukkitImplDeprecations.areaEffectCloudControls.warn(mechanism.context);
            getHelper().setColor(BukkitColorExtensions.getColor(mechanism.valueAsType(ColorTag.class)));
        }

        // <--[mechanism]
        // @object EntityTag
        // @name base_potion
        // @input ElementTag
        // @deprecated use 'EntityTag.potion_type' on MC 1.20+.
        // @description
        // Deprecated in favor of <@link property EntityTag.potion_type> on MC 1.20+.
        // @tags
        // <EntityTag.base_potion>
        // -->
        if (mechanism.matches("base_potion")) {
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
                BukkitImplDeprecations.areaEffectCloudControls.warn(mechanism.context);
            }
            List<String> data = CoreUtilities.split(mechanism.getValue().asString().toUpperCase(), ',');
            if (data.size() != 3) {
                mechanism.echoError(mechanism.getValue() + " is not a valid base potion!");
                return;
            }
            PotionType type = Utilities.elementToEnumlike(new ElementTag(data.get(0), true), PotionType.class);
            if (type == null) {
                mechanism.echoError(mechanism.getValue() + " is not a valid base potion!");
                return;
            }
            boolean upgraded = type.isUpgradeable() && CoreUtilities.equalsIgnoreCase(data.get(1), "true");
            boolean extended = type.isExtendable() && CoreUtilities.equalsIgnoreCase(data.get(2), "true");
            if (extended && upgraded) {
                mechanism.echoError("Potion cannot be both upgraded and extended");
            }
            else {
                getHelper().setBP(type, extended, upgraded);
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
        // TODO: some particles require additional data - need a new property that supports playeffect's special_data input
        if (mechanism.matches("particle") && Utilities.requireEnumlike(mechanism, Particle.class)) {
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
