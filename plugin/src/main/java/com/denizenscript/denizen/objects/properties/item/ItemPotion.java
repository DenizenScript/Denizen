package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class ItemPotion implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((ItemTag) item).getItemMeta() instanceof PotionMeta;
    }

    public static ItemPotion getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemPotion((ItemTag) _item);
        }
    }

    public static final String[] handledMechs = new String[] {
            "potion_effects"
    };

    private ItemPotion(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    public static String stringifyEffect(PotionEffect effect) {
        return effect.getType().getName() + "," +
                effect.getAmplifier() + "," +
                effect.getDuration() + "," +
                effect.isAmbient() + "," +
                effect.hasParticles() + "," +
                effect.hasIcon();
    }

    public static MapTag effectToMap(PotionEffect effect) {
        MapTag map = new MapTag();
        map.putObject("type", new ElementTag(effect.getType().getName()));
        map.putObject("amplifier", new ElementTag(effect.getAmplifier()));
        map.putObject("duration", new DurationTag((long) effect.getDuration()));
        map.putObject("ambient", new ElementTag(effect.isAmbient()));
        map.putObject("particles", new ElementTag(effect.hasParticles()));
        map.putObject("icon", new ElementTag(effect.hasIcon()));
        return map;
    }

    public ListTag getMapTagData() {
        ListTag result = new ListTag();
        MapTag base = new MapTag();
        PotionMeta meta = getMeta();
        base.putObject("type", new ElementTag(meta.getBasePotionData().getType()));
        base.putObject("upgraded", new ElementTag(meta.getBasePotionData().isUpgraded()));
        base.putObject("extended", new ElementTag(meta.getBasePotionData().isExtended()));
        if (meta.hasColor()) {
            base.putObject("color", new ColorTag(meta.getColor()));
        }
        result.addObject(base);
        for (PotionEffect effect : meta.getCustomEffects()) {
            result.addObject(effectToMap(effect));
        }
        return result;
    }

    public static PotionEffect parseEffect(String str, TagContext context) {
        String[] d2 = str.split(",");
        PotionEffectType type;
        try {
            type = PotionEffectType.getByName(d2[0].toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            if (context.showErrors()) {
                Debug.echoError("Invalid potion effect type '" + d2[0] + "'");
            }
            return null;
        }
        if (d2.length < 3) {
            return null;
        }
        // NOTE: amplifier and duration are swapped around in the input format
        // as compared to the PotionEffect constructor!
        int duration = new ElementTag(d2[2]).asInt();
        int amplifier = new ElementTag(d2[1]).asInt();
        boolean ambient = true;
        boolean particles = true;
        if (d2.length > 3) {
            ambient = new ElementTag(d2[3]).asBoolean();
            particles = new ElementTag(d2[4]).asBoolean();
        }
        boolean icon = false;
        if (d2.length > 5) {
            ElementTag check = new ElementTag(d2[5]);
            if (check.isBoolean()) {
                icon = check.asBoolean();
            }
        }
        return new PotionEffect(type, duration, amplifier, ambient, particles, icon);
    }

    public static PotionEffect parseEffect(MapTag effectMap, TagContext context) {
        PotionEffectType type;
        DurationTag duration = new DurationTag(0);
        int amplifier = 0;
        boolean ambient = true;
        boolean particles = true;
        boolean icon = false;
        if (effectMap.getObject("type") != null) {
            String typeString = effectMap.getObject("type").toString();
            type = PotionEffectType.getByName(typeString);
            if (type == null) {
                if (context.showErrors()) {
                    Debug.echoError("Invalid potion effect type '" + typeString + "': effect type is required.");
                }
                return null;
            }
        }
        else {
            if (context.showErrors()) {
                Debug.echoError("Invalid potion effect type: effect type is required.");
            }
            return null;
        }
        if (effectMap.getObject("amplifier") != null) {
            ElementTag amplifierElement = effectMap.getElement("amplifier");
            if (amplifierElement.isInt()) {
                amplifier = amplifierElement.asInt();
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid amplifier '" + amplifierElement + "': must be an integer.");
            }
        }
        if (effectMap.getObject("duration") != null) {
            ObjectTag durationObj = effectMap.getObject("duration");
            if (durationObj.canBeType(DurationTag.class)) {
                duration = durationObj.asType(DurationTag.class, context);
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid duration '" + durationObj + "': must be a valid DurationTag");
            }
        }
        if (effectMap.getObject("ambient") != null) {
            ElementTag ambientElement = effectMap.getElement("ambient");
            if (ambientElement.isBoolean()) {
                ambient = ambientElement.asBoolean();
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid ambient state '" + ambientElement + "': must be a boolean.");
            }
        }
        if (effectMap.getObject("particles") != null) {
            ElementTag particlesElement = effectMap.getElement("particles");
            if (particlesElement.isBoolean()) {
                particles = particlesElement.asBoolean();
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid particles state '" + particlesElement + "': must be a boolean.");
            }
        }
        if (effectMap.getObject("icon") != null) {
            ElementTag iconElement = effectMap.getElement("icon");
            if (iconElement.isBoolean()) {
                icon = iconElement.asBoolean();
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid icon state '" + iconElement + "': must be a boolean.");
            }
        }
        return new PotionEffect(type, duration.getTicksAsInt(), amplifier, ambient, particles, icon);
    }

    public PotionMeta getMeta() {
        return (PotionMeta) item.getItemMeta();
    }

    @Override
    public String getPropertyString() {
        return getMapTagData().identify();
    }

    @Override
    public String getPropertyId() {
        return "potion_effects";
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <ItemTag.potion_base_type>
        // @returns ElementTag
        // @mechanism ItemTag.potion_effects
        // @group properties
        // @description
        // Returns the base potion type name for this potion item.
        // The type will be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html>.
        // -->
        PropertyParser.registerTag(ItemPotion.class, ElementTag.class, "potion_base_type", (attribute, object) -> {
            return new ElementTag(object.getMeta().getBasePotionData().getType());
        });

        // <--[tag]
        // @attribute <ItemTag.potion_base>
        // @returns ElementTag
        // @group attribute
        // @mechanism ItemTag.potion_effects
        // @deprecated use 'effects_data' instead
        // @description
        // Deprecated in favor of <@link tag ItemTag.effects_data>
        // -->
        PropertyParser.registerTag(ItemPotion.class, ElementTag.class, "potion_base", (attribute, object) -> {
            BukkitImplDeprecations.oldPotionEffects.warn(attribute.context);
            PotionMeta meta = object.getMeta();
            return new ElementTag(meta.getBasePotionData().getType().name() + "," + (meta.getBasePotionData().isUpgraded() ? 2 : 1)
                    + "," + meta.getBasePotionData().isExtended() + "," + (object.item.getBukkitMaterial() == Material.SPLASH_POTION)
                    + (meta.hasColor() ? "," + new ColorTag(meta.getColor()).identify() : ""));
        });

        // <--[tag]
        // @attribute <ItemTag.potion_effects>
        // @returns ListTag
        // @group attribute
        // @mechanism ItemTag.potion_effects
        // @deprecated use 'effects_data' instead
        // @description
        // Deprecated in favor of <@link tag ItemTag.effects_data>
        // -->
        PropertyParser.registerTag(ItemPotion.class, ListTag.class, "potion_effects", (attribute, object) -> {
            BukkitImplDeprecations.oldPotionEffects.warn(attribute.context);
            ListTag result = new ListTag();
            for (PotionEffect pot : object.getMeta().getCustomEffects()) {
                result.add(stringifyEffect(pot));
            }
            return result;
        });

        // <--[tag]
        // @attribute <ItemTag.has_potion_effect>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.potion_effects
        // @description
        // Returns whether the potion has a potion effect.
        // -->
        PropertyParser.registerTag(ItemPotion.class, ElementTag.class, "has_potion_effect", (attribute, object) -> {
            return new ElementTag(object.getMeta().hasCustomEffects());
        });

        // <--[tag]
        // @attribute <ItemTag.potion_effect[<#>]>
        // @returns ElementTag
        // @group attribute
        // @mechanism ItemTag.potion_effects
        // @deprecated use 'effects_data' instead
        // @description
        // Deprecated in favor of <@link tag ItemTag.effects_data>
        // -->
        PropertyParser.registerTag(ItemPotion.class, ElementTag.class, "potion_effect", (attribute, object) -> {
            BukkitImplDeprecations.oldPotionEffects.warn(attribute.context);
            PotionMeta meta = object.getMeta();
            int potN = attribute.hasParam() ? attribute.getIntParam() - 1 : 0;
            if (potN < 0 || potN > meta.getCustomEffects().size()) {
                return null;
            }
            if (attribute.startsWith("is_splash", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.item.getBukkitMaterial() == Material.SPLASH_POTION);
            }
            if (attribute.startsWith("is_extended", 2)) {
                attribute.fulfill(1);
                return new ElementTag(meta.getBasePotionData().isExtended());
            }
            if (attribute.startsWith("level", 2)) {
                attribute.fulfill(1);
                return new ElementTag(meta.getBasePotionData().isUpgraded() ? 2 : 1);
            }
            if (attribute.startsWith("is_ambient", 2)) {
                attribute.fulfill(1);
                return new ElementTag(meta.getCustomEffects().get(potN).isAmbient());
            }
            if (attribute.startsWith("icon", 2)) {
                attribute.fulfill(1);
                return new ElementTag(meta.getCustomEffects().get(potN).hasIcon());
            }
            if (attribute.startsWith("has_particles", 2)) {
                attribute.fulfill(1);
                return new ElementTag(meta.getCustomEffects().get(potN).hasParticles());
            }
            if (attribute.startsWith("duration", 2)) {
                attribute.fulfill(1);
                return new ElementTag(meta.getCustomEffects().get(potN).getDuration());
            }
            if (attribute.startsWith("amplifier", 2)) {
                attribute.fulfill(1);
                return new ElementTag(meta.getCustomEffects().get(potN).getAmplifier());
            }
            if (attribute.startsWith("type", 2)) {
                attribute.fulfill(1);
                return new ElementTag(meta.getCustomEffects().get(potN).getType().getName());
            }
            if (attribute.startsWith("data", 2)) {
                attribute.fulfill(1);
                return new ElementTag(0);
            }
            PotionData data = meta.getBasePotionData();
            return new ElementTag(data.getType().name() + "," + (data.isUpgraded() ? 2 : 1)
                    + "," + data.isExtended() + "," + (object.item.getBukkitMaterial() == Material.SPLASH_POTION));

        });

        // <--[tag]
        // @attribute <ItemTag.effects_data>
        // @returns ListTag(MapTag)
        // @mechanism ItemTag.potion_effects
        // @group properties
        // @description
        // Returns a list of all potion effects on this item, in the same format as the MapTag input to the mechanism.
        // Note that the first value in the list is the potion's base type, and all subsequent entries are effect data.
        // -->
        PropertyParser.registerTag(ItemPotion.class, ListTag.class, "effects_data", (attribute, object) -> {
            return object.getMapTagData();
        });
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name potion_effects
        // @input ListTag
        // @description
        // Sets the potion's potion effect(s).
        // The first item in the list must be a MapTag with keys:
        // "type" - from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html>
        // "upgraded" - boolean, true means level 2 and false means level 1
        // "extended" - true means longer, false means shorter
        // "color" - optional, ColorTag
        //
        // For example: [type=SPEED;upgraded=true;extended=false;color=RED]
        // This example produces an item labeled as "Potion of Swiftness - Speed II (1:30)"
        //
        //
        // Each following item in the list are potion effects, which must be a MapTag with keys:
        // "type" - from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>
        // "amplifier" - number to increase the level by (0 for default level 1)
        // "duration" - DurationTag, how long it lasts
        // "ambient", "particles", "icon" - booleans
        //
        // For example: [type=SPEED;amplifier=2;duration=200t;ambient=false;particles=true;icon=true]
        // This example would be a level 3 swiftness potion that lasts 200 ticks.
        // @tags
        // <ItemTag.effects_data>
        // <server.potion_types>
        // <server.potion_effect_types>
        // -->
        if (mechanism.matches("potion_effects")) {
            List<ObjectTag> data = new ArrayList<>(CoreUtilities.objectToList(mechanism.value, mechanism.context));
            ObjectTag firstObj = data.remove(0);
            PotionMeta meta = getMeta();
            PotionType type;
            boolean upgraded = false;
            boolean extended = false;
            ColorTag color = null;
            if (firstObj.canBeType(MapTag.class)) {
                MapTag baseEffect = firstObj.asType(MapTag.class, mechanism.context);
                if (baseEffect.getObject("type") != null) {
                    ElementTag typeElement = baseEffect.getElement("type");
                    if (!typeElement.matchesEnum(PotionType.class)) {
                        mechanism.echoError("Invalid base potion type '" + typeElement + "': type is required");
                        return;
                    }
                    type = PotionType.valueOf(typeElement.asString().toUpperCase());
                }
                else {
                    mechanism.echoError("No base potion type specified: type is required");
                    return;
                }
                if (baseEffect.getObject("upgraded") != null) {
                    ElementTag upgradedElement = baseEffect.getElement("upgraded");
                    if (upgradedElement.isBoolean()) {
                        upgraded = upgradedElement.asBoolean();
                    }
                    else {
                        mechanism.echoError("Invalid upgraded state '" + upgradedElement + "': must be a boolean");
                    }
                }
                if (baseEffect.getObject("extended") != null) {
                    ElementTag extendedElement = baseEffect.getElement("extended");
                    if (extendedElement.isBoolean()) {
                        extended = extendedElement.asBoolean();
                    }
                    else {
                        mechanism.echoError("Invalid extended state '" + extendedElement + "': must be a boolean");
                    }
                }
                if (baseEffect.getObject("color") != null) {
                    ObjectTag colorObj = baseEffect.getObject("color");
                    if (colorObj.canBeType(ColorTag.class)) {
                        color = colorObj.asType(ColorTag.class, mechanism.context);
                    }
                    else {
                        mechanism.echoError("Invalid color '" + colorObj + "': must be a valid ColorTag");
                    }
                }
            }
            else {
                String[] d1 = firstObj.toString().split(",");
                try {
                    type = PotionType.valueOf(d1[0].toUpperCase());
                }
                catch (IllegalArgumentException ex) {
                    mechanism.echoError("Invalid base potion type '" + d1[0] + "': type is required");
                    return;
                }
                upgraded = CoreUtilities.equalsIgnoreCase(d1[1], "true");
                extended = CoreUtilities.equalsIgnoreCase(d1[2], "true");
                if (d1.length > 3) {
                    ColorTag temp = ColorTag.valueOf(d1[3].replace("&comma", ","), mechanism.context);
                    if (temp == null) {
                        mechanism.echoError("Invalid color '" + d1[3] + "': must be a valid ColorTag");
                    }
                    else {
                        color = temp;
                    }
                }
            }
            if (upgraded && !type.isUpgradeable()) {
                mechanism.echoError("Cannot upgrade potion of type '" + type.name() + "'");
                upgraded = false;
            }
            if (extended && !type.isExtendable()) {
                mechanism.echoError("Cannot extend potion of type '" + type.name() + "'");
                extended = false;
            }
            if (upgraded && extended) {
                mechanism.echoError("Cannot both upgrade and extend a potion");
                extended = false;
            }
            if (color != null) {
                meta.setColor(color.getColor());
            }
            meta.setBasePotionData(new PotionData(type, extended, upgraded));
            meta.clearCustomEffects();
            for (ObjectTag effectObj : data) {
                PotionEffect effect;
                if (effectObj.canBeType(MapTag.class)) {
                    effect = parseEffect(effectObj.asType(MapTag.class, mechanism.context), mechanism.context);
                }
                else {
                    effect = parseEffect(effectObj.toString(), mechanism.context);
                }
                if (effect != null) {
                    meta.addCustomEffect(effect, false);
                }
                else {
                    mechanism.echoError("Invalid potion effect '" + effectObj + "'");
                }
            }
            item.setItemMeta(meta);
        }
    }
}
