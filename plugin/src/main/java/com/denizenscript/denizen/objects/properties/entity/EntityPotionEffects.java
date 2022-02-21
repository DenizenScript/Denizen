package com.denizenscript.denizen.objects.properties.entity;

import com.denizenscript.denizen.objects.properties.item.ItemPotion;
import com.denizenscript.denizen.objects.EntityTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.entity.Arrow;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;

public class EntityPotionEffects implements Property {

    public static boolean describes(ObjectTag object) {
        if (!(object instanceof EntityTag)) {
            return false;
        }
        return ((EntityTag) object).isLivingEntity()
                || ((EntityTag) object).getBukkitEntity() instanceof Arrow;
    }

    public static EntityPotionEffects getFrom(ObjectTag object) {
        if (!describes(object)) {
            return null;
        }
        else {
            return new EntityPotionEffects((EntityTag) object);
        }
    }

    public static final String[] handledMechs = new String[] {
            "potion_effects"
    };

    private EntityPotionEffects(EntityTag entity) {
        this.entity = entity;
    }

    EntityTag entity;

    public Collection<PotionEffect> getEffectsList() {
        if (entity.isLivingEntity()) {
            return entity.getLivingEntity().getActivePotionEffects();
        }
        else if (isArrow()) {
            return getArrow().getCustomEffects();
        }
        return new ArrayList<>();
    }

    public ListTag getEffectsListTag() {
        ListTag result = new ListTag();
        for (PotionEffect effect : getEffectsList()) {
            result.add(ItemPotion.stringifyEffect(effect));
        }
        return result;
    }

    public ListTag getEffectsMapTag() {
        ListTag result = new ListTag();
        for (PotionEffect effect : getEffectsList()) {
            result.addObject(ItemPotion.effectToMap(effect));
        }
        return result;
    }

    public boolean isArrow() {
        return entity.getBukkitEntity() instanceof Arrow;
    }

    public Arrow getArrow() {
        return (Arrow) entity.getBukkitEntity();
    }

    public String getPropertyString() {
        ListTag effects = getEffectsMapTag();
        return effects.isEmpty() ? null : effects.identify();
    }

    public String getPropertyId() {
        return "potion_effects";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <EntityTag.list_effects>
        // @returns ListTag
        // @group attribute
        // @mechanism EntityTag.potion_effects
        // @description
        // Returns the list of active potion effects on the entity, in the format: TYPE,AMPLIFIER,DURATION,IS_AMBIENT,HAS_PARTICLES,HAS_ICON|...
        // Note that AMPLIFIER is a number representing the level, and DURATION is a number representing the time, in ticks, it will last for.
        // IS_AMBIENT, HAS_PARTICLES, and HAS_ICON are booleans.
        // The effect type will be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
        // -->
        PropertyParser.<EntityPotionEffects, ListTag>registerTag(ListTag.class, "list_effects", (attribute, object) -> {
            return object.getEffectsListTag();
        });

        // <--[tag]
        // @attribute <EntityTag.effects_data>
        // @returns ListTag(MapTag)
        // @group attribute
        // @mechanism EntityTag.potion_effects
        // @description
        // Returns the active potion effects on the entity, in the MapTag format of the mechanism.
        // -->
        PropertyParser.<EntityPotionEffects, ListTag>registerTag(ListTag.class, "effects_data", (attribute, object) -> {
            return object.getEffectsMapTag();
        });

        // <--[tag]
        // @attribute <EntityTag.has_effect[<effect>]>
        // @returns ElementTag(Boolean)
        // @group attributes
        // @mechanism EntityTag.potion_effects
        // @description
        // Returns whether the entity has a specified effect.
        // If no effect is specified, returns whether the entity has any effect.
        // The effect type must be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
        // -->
        PropertyParser.<EntityPotionEffects, ElementTag>registerTag(ElementTag.class, "has_effect", (attribute, object) -> {
            boolean returnElement = false;
            if (attribute.hasParam()) {
                PotionEffectType effectType = PotionEffectType.getByName(attribute.getParam());
                if (effectType == null) {
                    attribute.echoError("Invalid effect type specified: " + attribute.getParam());
                    return null;
                }
                if (object.entity.isLivingEntity()) {
                    returnElement = object.entity.getLivingEntity().hasPotionEffect(effectType);
                }
                else if (object.isArrow()) {
                    returnElement = object.getArrow().hasCustomEffect(effectType);
                }
            }
            else if (!object.getEffectsList().isEmpty()) {
                returnElement = true;
            }
            return new ElementTag(returnElement);
        });
    }

    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object EntityTag
        // @name potion_effects
        // @input ListTag
        // @description
        // Set the entity's active potion effects.
        // Each item in the list can be any of the following:
        // 1: Comma-separated potion effect data in the format: TYPE,AMPLIFIER,DURATION,IS_AMBIENT,HAS_PARTICLES,HAS_ICON
        // Note that AMPLIFIER is a number representing the level, and DURATION is a number representing the time, in ticks, it will last for.
        // IS_AMBIENT, HAS_PARTICLES, and HAS_ICON are booleans.
        // For example: SPEED,0,120,false,true,true would give the entity a swiftness potion for 120 ticks.
        // 2: A MapTag with "type", "amplifier", "duration", "ambient", "particles", and "icon" keys.
        // For example: [type=SPEED;amplifier=0;duration=120t;ambient=false;particles=true;icon=true]
        // The effect type must be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
        // @tags
        // <EntityTag.effects_data>
        // <EntityTag.list_effects>
        // <EntityTag.has_effect[<effect>]>
        // -->
        if (mechanism.matches("potion_effects")) {
            for (ObjectTag effectObj : CoreUtilities.objectToList(mechanism.value, mechanism.context)) {
                PotionEffect effect;
                if (effectObj.canBeType(MapTag.class)) {
                    MapTag effectMap = effectObj.asType(MapTag.class, mechanism.context);
                    effect = ItemPotion.parseEffect(effectMap, mechanism.context);
                }
                else {
                    String effectStr = effectObj.toString();
                    effect = ItemPotion.parseEffect(effectStr, mechanism.context);
                }
                if (effect == null) {
                    mechanism.echoError("Invalid potion effect '" + effectObj + "'");
                    continue;
                }
                if (entity.isLivingEntity()) {
                    entity.getLivingEntity().addPotionEffect(effect);
                }
                else if (isArrow()) {
                    getArrow().addCustomEffect(effect, true);
                }
            }
        }
    }
}
