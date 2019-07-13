package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.dEntity;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.TippedArrow;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class EntityPotionEffects implements Property {

    public static boolean describes(ObjectTag object) {
        return object instanceof dEntity &&
                (((dEntity) object).isLivingEntity()
                        || ((dEntity) object).getBukkitEntity() instanceof TippedArrow);
    }

    public static EntityPotionEffects getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityPotionEffects((dEntity) object);
        }
    }

    public static final String[] handledTags = new String[] {
            "list_effects", "has_effect"
    };

    public static final String[] handledMechs = new String[] {
            "potion_effects"
    };


    ///////////////////
    // Instance Fields and Methods
    /////////////

    private EntityPotionEffects(dEntity entity) {
        this.entity = entity;
    }

    dEntity entity;

    public Collection<PotionEffect> getEffectsList() {
        if (entity.isLivingEntity()) {
            return entity.getLivingEntity().getActivePotionEffects();
        }
        else if (entity.getBukkitEntity() instanceof TippedArrow) {
            return ((TippedArrow) entity.getBukkitEntity()).getCustomEffects();
        }
        return new ArrayList<>();
    }

    /////////
    // Property Methods
    ///////

    public String getPropertyString() {
        Collection<PotionEffect> effects = getEffectsList();
        if (effects.isEmpty()) {
            return null;
        }
        ListTag returnable = new ListTag();
        for (PotionEffect effect : effects) {
            returnable.add(effect.getType().getName() + "," + effect.getAmplifier() + "," + effect.getDuration());
        }
        return returnable.identify().substring(3);
    }

    public String getPropertyId() {
        return "potion_effects";
    }

    public static String stringify(PotionEffect effect) {
        return effect.getType().getName() + "," + effect.getAmplifier() + "," + effect.getDuration()
                + "," + effect.isAmbient() + "," + effect.hasParticles()
                + (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2) ? "," + effect.hasIcon() : "");
    }

    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return "null";
        }

        // <--[tag]
        // @attribute <e@entity.list_effects>
        // @returns ListTag
        // @group attribute
        // @mechanism dEntity.potion_effects
        // @description
        // Returns the list of active potion effects on the entity, in the format: li@TYPE,AMPLIFIER,DURATION,IS_AMBIENT,HAS_PARTICLES,HAS_ICON|...
        // Note that AMPLIFIER is a number representing the level, and DURATION is a number representing the time, in ticks, it will last for.
        // IS_AMBIENT, HAS_PARTICLES, and HAS_ICON are booleans.
        // -->
        if (attribute.startsWith("list_effects")) {
            ListTag effects = new ListTag();
            for (PotionEffect effect : getEffectsList()) {
                effects.add(stringify(effect));
            }
            return effects.getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <e@entity.has_effect[<effect>]>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @description
        // Returns whether the entity has a specified effect.
        // If no effect is specified, returns whether the entity has any effect.
        // -->
        if (attribute.startsWith("has_effect")) {
            boolean returnElement = false;
            if (attribute.hasContext(1)) {
                PotionEffectType effectType = PotionEffectType.getByName(attribute.getContext(1));
                for (org.bukkit.potion.PotionEffect effect : getEffectsList()) {
                    if (effect.getType().equals(effectType)) {
                        returnElement = true;
                    }
                }
            }
            else if (!getEffectsList().isEmpty()) {
                returnElement = true;
            }
            return new ElementTag(returnElement).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dEntity
        // @name potion_effects
        // @input ListTag
        // @description
        // Set the entity's active potion effects.
        // Each item in the list is formatted as: TYPE,AMPLIFIER,DURATION,IS_AMBIENT,HAS_PARTICLES,HAS_ICON
        // Note that AMPLIFIER is a number representing the level, and DURATION is a number representing the time, in ticks, it will last for.
        // IS_AMBIENT, HAS_PARTICLES, and HAS_ICON are booleans.
        // For example: SPEED,0,120,false,true,true would give the entity a swiftness potion for 120 ticks.
        // @tags
        // <e@entity.list_effects>
        // -->
        if (mechanism.matches("potion_effects")) {
            ListTag effects = ListTag.valueOf(mechanism.getValue().asString());
            for (String effect : effects) {
                List<String> split = CoreUtilities.split(effect, ',');
                if (split.size() < 3) {
                    // Maybe error message?
                    continue;
                }
                PotionEffectType effectType = PotionEffectType.getByName(split.get(0));
                if (effectType == null) {
                    Debug.echoError("Cannot apply potion effect '" + split.get(0) + "': unknown effect type.");
                    continue;
                }
                try {
                    PotionEffect actualEffect;
                    if (split.size() >= 6) {
                        actualEffect = new PotionEffect(effectType, Integer.valueOf(split.get(2)),
                                Integer.valueOf(split.get(1)), split.get(3).equalsIgnoreCase("true"), split.get(4).equalsIgnoreCase("true"),
                                split.get(5).equalsIgnoreCase("true"));
                    }
                    else if (split.size() >= 5) {
                        actualEffect = new PotionEffect(effectType, Integer.valueOf(split.get(2)),
                                Integer.valueOf(split.get(1)), split.get(3).equalsIgnoreCase("true"), split.get(4).equalsIgnoreCase("true"));
                    }
                    else {
                         actualEffect = new PotionEffect(effectType, Integer.valueOf(split.get(2)),
                                Integer.valueOf(split.get(1)));
                    }
                    if (entity.isLivingEntity()) {
                        entity.getLivingEntity().addPotionEffect(actualEffect);
                    }
                    else if (entity.getBukkitEntity() instanceof TippedArrow) {
                        ((TippedArrow) entity.getBukkitEntity()).addCustomEffect(actualEffect, true);
                    }
                }
                catch (NumberFormatException ex) {
                    Debug.echoError("Cannot apply potion effect '" + effect + "': invalid amplifier or duration number.");
                    continue;
                }
            }
        }
    }
}
