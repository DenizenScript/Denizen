package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.objects.properties.bukkit.BukkitColorExtensions;
import com.denizenscript.denizen.utilities.BukkitImplDeprecations;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.*;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

// TODO: 1.20.6: Spigot removed the PotionData API, can either remove relevant features on 1.20.6, or try and backsupport somehow (probably either hard-coding our own data or hacking it based on the name)
public class ItemPotion extends ItemProperty<ObjectTag> {

    public static boolean describes(ItemTag item) {
        return item.getItemMeta() instanceof PotionMeta || item.getItemMeta() instanceof SuspiciousStewMeta;
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

    public ListTag getMapTagData(boolean includeExtras) {
        List<PotionEffect> potionEffects = getCustomEffects();
        ListTag result = new ListTag(potionEffects.size() + 1);
        if (getItemMeta() instanceof PotionMeta potionMeta) {
            MapTag base = new MapTag();
            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20)) {
                base.putObject("base_type", new ElementTag(Utilities.namespacedKeyToString(potionMeta.getBasePotionType().getKey()), true));
            }
            else {
                includeExtras = true;
            }
            if (includeExtras) { // TODO: Eventually remove these 4
                base.putObject("type", new ElementTag(potionMeta.getBasePotionData().getType()));
                base.putObject("upgraded", new ElementTag(potionMeta.getBasePotionData().isUpgraded()));
                base.putObject("extended", new ElementTag(potionMeta.getBasePotionData().isExtended()));
                if (potionMeta.hasColor()) {
                    base.putObject("color", BukkitColorExtensions.fromColor(potionMeta.getColor()));
                }
            }
            result.addObject(base);
        }
        for (PotionEffect potionEffect : potionEffects) {
            result.addObject(effectToMap(potionEffect));
        }
        return result;
    }

    public static PotionEffect parseEffect(MapTag effectMap, TagContext context) {
        PotionEffectType type;
        DurationTag duration = new DurationTag(0);
        int amplifier = 0;
        boolean ambient = true;
        boolean particles = true;
        boolean icon = false;
        if (effectMap.containsKey("type")) {
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
        if (effectMap.containsKey("amplifier")) {
            ElementTag amplifierElement = effectMap.getElement("amplifier");
            if (amplifierElement.isInt()) {
                amplifier = amplifierElement.asInt();
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid amplifier '" + amplifierElement + "': must be an integer.");
            }
        }
        if (effectMap.containsKey("duration")) {
            ObjectTag durationObj = effectMap.getObject("duration");
            if (durationObj.canBeType(DurationTag.class)) {
                duration = durationObj.asType(DurationTag.class, context);
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid duration '" + durationObj + "': must be a valid DurationTag");
            }
        }
        if (effectMap.containsKey("ambient")) {
            ElementTag ambientElement = effectMap.getElement("ambient");
            if (ambientElement.isBoolean()) {
                ambient = ambientElement.asBoolean();
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid ambient state '" + ambientElement + "': must be a boolean.");
            }
        }
        if (effectMap.containsKey("particles")) {
            ElementTag particlesElement = effectMap.getElement("particles");
            if (particlesElement.isBoolean()) {
                particles = particlesElement.asBoolean();
            }
            else if (context.showErrors()) {
                Debug.echoError("Invalid particles state '" + particlesElement + "': must be a boolean.");
            }
        }
        if (effectMap.containsKey("icon")) {
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

    public List<PotionEffect> getCustomEffects() {
        if (getItemMeta() instanceof SuspiciousStewMeta suspiciousStewMeta) {
            return suspiciousStewMeta.getCustomEffects();
        }
        return as(PotionMeta.class).getCustomEffects();
    }

    @Override
    public ListTag getPropertyValue() {
        return getMapTagData(false);
    }

    @Override
    public void setPropertyValue(ObjectTag value, Mechanism mechanism) {
        List<ObjectTag> data = new ArrayList<>(CoreUtilities.objectToList(value, mechanism.context));
        ItemMeta meta = getItemMeta();
        if (meta instanceof PotionMeta potionMeta) {
            ObjectTag firstObj = data.remove(0);
            if (firstObj.canBeType(MapTag.class)) {
                MapTag baseEffect = firstObj.asType(MapTag.class, mechanism.context);
                if (baseEffect.containsKey("base_type")) {
                    ElementTag baseTypeElement = baseEffect.getElement("base_type");
                    PotionType type = Registry.POTION.get(Utilities.parseNamespacedKey(baseTypeElement.asString()));
                    if (type == null && baseTypeElement.matchesEnum(PotionType.class)) {
                        type = baseTypeElement.asEnum(PotionType.class);
                    }
                    if (type == null) {
                        mechanism.echoError("Invalid base potion type '" + baseTypeElement + "': valid base potion_type is required");
                        return;
                    }
                    potionMeta.setBasePotionType(type);
                }
                else {
                    applyLegacyMapBasePotionData(baseEffect, potionMeta, mechanism);
                }
            }
            else {
                applyLegacyStringBasePotionData(firstObj.toString(), potionMeta, mechanism);
            }
            potionMeta.clearCustomEffects();
        }
        else {
            ((SuspiciousStewMeta) meta).clearCustomEffects();
        }
        for (ObjectTag effectObj : data) {
            PotionEffect effect;
            if (effectObj.canBeType(MapTag.class)) {
                effect = parseEffect(effectObj.asType(MapTag.class, mechanism.context), mechanism.context);
            }
            else {
                effect = parseLegacyEffectString(effectObj.toString(), mechanism.context);
            }
            if (effect == null) {
                mechanism.echoError("Invalid potion effect '" + effectObj + "'");
                continue;
            }
            if (meta instanceof PotionMeta potionMeta) {
                potionMeta.addCustomEffect(effect, false);
            }
            else {
                ((SuspiciousStewMeta) meta).addCustomEffect(effect, false);
            }
        }
        setItemMeta(meta);
    }

    @Override
    public String getPropertyId() {
        return "potion_effects";
    }

    public static void register() {

        // <--[tag]
        // @attribute <ItemTag.effects_data>
        // @returns ListTag(MapTag)
        // @mechanism ItemTag.potion_effects
        // @group properties
        // @description
        // Returns a list of all potion effects on this item, in the same format as the MapTag input to the mechanism.
        // This applies to Potion items, Tipped Arrow items, and Suspicious Stews.
        // Note that for potions or tipped arrows (not suspicious stew) the first value in the list is the potion's base type.
        // All subsequent entries are effect data.
        // -->
        PropertyParser.registerTag(ItemPotion.class, ListTag.class, "effects_data", (attribute, prop) -> {
            return prop.getMapTagData(true);
        });

        // <--[mechanism]
        // @object ItemTag
        // @name potion_effects
        // @input ListTag
        // @description
        // Sets the item's potion effect(s).
        // This applies to Potion items, Tipped Arrow items, and Suspicious Stews.
        //
        // For potions or tipped arrows (not suspicious stew), the first item in the list must be a MapTag with keys:
        // "base_type" - from <@link url https://minecraft.wiki/w/Potion#Item_data>
        //
        // For example: [base_type=strong_swiftness]
        // This example produces an item labeled as "Potion of Swiftness - Speed II (1:30)"
        //
        // Each following item in the list are potion effects, which must be a MapTag with keys:
        // "type" - from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>
        // "amplifier" - number to increase the level by (0 for default level 1) (optional, default 0)
        // "duration" - DurationTag, how long it lasts (optional, default 0s)
        // "ambient", "particles", "icon" - booleans (optional, default true, true, false)
        //
        // For example: [type=SPEED;amplifier=2;duration=10s;ambient=false;particles=true;icon=true]
        // This example would be a level 3 swiftness potion that lasts 10 seconds.
        //
        // A very short full default potion item would be: potion[potion_effects=[base_type=regeneration]
        // A (relatively) short full potion item would be: potion[potion_effects=<list[[base_type=regeneration]|[type=speed;duration=10s]]>]
        // (Note the list constructor to force data format interpretation, as potion formats can be given multiple ways and the system will get confused without a constructor)
        //
        // @tags
        // <ItemTag.effects_data>
        // <server.potion_types>
        // <server.potion_effect_types>
        // -->
        PropertyParser.registerMechanism(ItemPotion.class, ObjectTag.class, "potion_effects", (prop, mechanism, input) -> {
            prop.setPropertyValue(input, mechanism);
        });

        // <--[tag]
        // @attribute <ItemTag.has_potion_effect>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.potion_effects
        // @description
        // Returns whether the item (potion, tipped arrow, or suspicious stew) has a potion effect.
        // -->
        PropertyParser.registerTag(ItemPotion.class, ElementTag.class, "has_potion_effect", (attribute, object) -> {
            return new ElementTag(object.getItemMeta() instanceof SuspiciousStewMeta suspiciousStewMeta ? suspiciousStewMeta.hasCustomEffects() : object.as(PotionMeta.class).hasCustomEffects());
        });

        /*
        ==============================
        = Deprecated/legacy features =
        ==============================
        */

        // <--[tag]
        // @attribute <ItemTag.potion_base_type>
        // @returns ElementTag
        // @mechanism ItemTag.potion_effects
        // @group properties
        // @deprecated use 'effects_data.first.get[base_type]' instead
        // @description
        // Deprecated in favor of <@link tag ItemTag.effects_data>
        // -->
        PropertyParser.registerTag(ItemPotion.class, ElementTag.class, "potion_base_type", (attribute, object) -> {
            if (!(object.getItemMeta() instanceof PotionMeta potionMeta)) {
                attribute.echoError("This item does not have a base potion type.");
                return null;
            }
            BukkitImplDeprecations.oldPotionEffects.warn(attribute.context);
            return new ElementTag(potionMeta.getBasePotionData().getType());
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
            if (!(object.getItemMeta() instanceof PotionMeta potionMeta)) {
                attribute.echoError("This item does not have a base potion type.");
                return null;
            }
            BukkitImplDeprecations.oldPotionEffects.warn(attribute.context);
            return new ElementTag(potionMeta.getBasePotionData().getType().name() + "," + (potionMeta.getBasePotionData().isUpgraded() ? 2 : 1)
                    + "," + potionMeta.getBasePotionData().isExtended() + "," + (object.getMaterial() == Material.SPLASH_POTION)
                    + (potionMeta.hasColor() ? "," + BukkitColorExtensions.fromColor(potionMeta.getColor()).identify() : ""));
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
            ListTag result = new ListTag();
            for (PotionEffect pot : object.getCustomEffects()) {
                result.add(effectToLegacyString(pot, attribute.context));
            }
            return result;
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
            int potN = attribute.hasParam() ? attribute.getIntParam() - 1 : 0;
            if (potN < 0 || potN > object.getCustomEffects().size()) {
                return null;
            }
            if (attribute.startsWith("is_splash", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getMaterial() == Material.SPLASH_POTION);
            }
            if (attribute.startsWith("is_extended", 2)) {
                attribute.fulfill(1);
                if (!(object.getItemMeta() instanceof PotionMeta potionMeta)) {
                    return null;
                }
                return new ElementTag(potionMeta.getBasePotionData().isExtended());
            }
            if (attribute.startsWith("level", 2)) {
                attribute.fulfill(1);
                if (!(object.getItemMeta() instanceof PotionMeta potionMeta)) {
                    return null;
                }
                return new ElementTag(potionMeta.getBasePotionData().isUpgraded() ? 2 : 1);
            }
            if (attribute.startsWith("is_ambient", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getCustomEffects().get(potN).isAmbient());
            }
            if (attribute.startsWith("icon", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getCustomEffects().get(potN).hasIcon());
            }
            if (attribute.startsWith("has_particles", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getCustomEffects().get(potN).hasParticles());
            }
            if (attribute.startsWith("duration", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getCustomEffects().get(potN).getDuration());
            }
            if (attribute.startsWith("amplifier", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getCustomEffects().get(potN).getAmplifier());
            }
            if (attribute.startsWith("type", 2)) {
                attribute.fulfill(1);
                return new ElementTag(object.getCustomEffects().get(potN).getType().getName());
            }
            if (attribute.startsWith("data", 2)) {
                attribute.fulfill(1);
                return new ElementTag(0);
            }
            if (!(object.getItemMeta() instanceof PotionMeta potionMeta)) {
                return null;
            }
            PotionData data = potionMeta.getBasePotionData();
            return new ElementTag(data.getType().name() + "," + (data.isUpgraded() ? 2 : 1)
                    + "," + data.isExtended() + "," + (object.getMaterial() == Material.SPLASH_POTION));

        });
    }

    private static void applyLegacyMapBasePotionData(MapTag input, PotionMeta potionMeta, Mechanism mechanism) {
        if (!input.containsKey("type")) {
            mechanism.echoError("Must specify a base potion type.");
            return;
        }
        ElementTag typeElement = input.getElement("type");
        if (!typeElement.matchesEnum(PotionType.class)) {
            mechanism.echoError("Invalid base potion type '" + typeElement + "': type is required");
            return;
        }
        PotionType type = PotionType.valueOf(typeElement.asString().toUpperCase());
        boolean upgraded = false;
        boolean extended = false;
        if (input.containsKey("upgraded")) {
            ElementTag upgradedElement = input.getElement("upgraded");
            if (upgradedElement.isBoolean()) {
                upgraded = upgradedElement.asBoolean();
            }
            else {
                mechanism.echoError("Invalid upgraded state '" + upgradedElement + "': must be a boolean");
            }
        }
        if (input.containsKey("extended")) {
            ElementTag extendedElement = input.getElement("extended");
            if (extendedElement.isBoolean()) {
                extended = extendedElement.asBoolean();
            }
            else {
                mechanism.echoError("Invalid extended state '" + extendedElement + "': must be a boolean");
            }
        }
        ColorTag color = null;
        if (input.containsKey("color")) {
            ObjectTag colorObj = input.getObject("color");
            if (colorObj.canBeType(ColorTag.class)) {
                color = colorObj.asType(ColorTag.class, mechanism.context);
            }
            else {
                mechanism.echoError("Invalid color '" + colorObj + "': must be a valid ColorTag");
            }
        }
        applyLegacyBasePotionData(potionMeta, type, upgraded, extended, color, mechanism);
    }

    private static void applyLegacyStringBasePotionData(String input, PotionMeta potionMeta, Mechanism mechanism) {
        String[] d1 = input.split(",");
        PotionType type;
        try {
            type = PotionType.valueOf(d1[0].toUpperCase());
        }
        catch (IllegalArgumentException ex) {
            mechanism.echoError("Invalid base potion type '" + d1[0] + "': type is required");
            return;
        }
        boolean upgraded = CoreUtilities.equalsIgnoreCase(d1[1], "true");
        boolean extended = CoreUtilities.equalsIgnoreCase(d1[2], "true");
        ColorTag color = null;
        if (d1.length > 3) {
            ColorTag temp = ColorTag.valueOf(d1[3].replace("&comma", ","), mechanism.context);
            if (temp == null) {
                mechanism.echoError("Invalid color '" + d1[3] + "': must be a valid ColorTag");
            }
            else {
                color = temp;
            }
        }
        applyLegacyBasePotionData(potionMeta, type, upgraded, extended, color, mechanism);
    }

    private static void applyLegacyBasePotionData(PotionMeta potionMeta, PotionType type, boolean upgraded, boolean extended, ColorTag color, Mechanism mechanism) {
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
        potionMeta.setBasePotionData(new PotionData(type, extended, upgraded));
        if (color != null) {
            potionMeta.setColor(BukkitColorExtensions.getColor(color));
        }
    }

    public static String effectToLegacyString(PotionEffect effect, TagContext context) {
        BukkitImplDeprecations.oldPotionEffects.warn(context);
        return effect.getType().getName() + "," +
                effect.getAmplifier() + "," +
                effect.getDuration() + "," +
                effect.isAmbient() + "," +
                effect.hasParticles() + "," +
                effect.hasIcon();
    }

    public static PotionEffect parseLegacyEffectString(String str, TagContext context) {
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
}
