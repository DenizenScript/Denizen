package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.tags.TagContext;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class ItemPotion implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && (((ItemTag) item).getBukkitMaterial() == Material.POTION
                || ((ItemTag) item).getBukkitMaterial() == Material.SPLASH_POTION
                || ((ItemTag) item).getBukkitMaterial() == Material.LINGERING_POTION
                || ((ItemTag) item).getBukkitMaterial() == Material.TIPPED_ARROW);
    }

    public static ItemPotion getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemPotion((ItemTag) _item);
        }
    }

    public static final String[] handledTags = new String[] {
            "potion_base_type", "potion_base", "has_potion_effect", "potion_effect", "potion_effects"
    };

    public static final String[] handledMechs = new String[] {
            "potion_effects"
    };

    private ItemPotion(ItemTag item) {
        this.item = item;
    }

    ItemTag item;

    public static String stringifyEffect(PotionEffect effect) {
        StringBuilder sb = new StringBuilder();
        sb.append(effect.getType().getName()).append(",")
                .append(effect.getAmplifier()).append(",")
                .append(effect.getDuration()).append(",")
                .append(effect.isAmbient()).append(",")
                .append(effect.hasParticles()).append(",")
                .append(effect.hasIcon());
        return sb.toString();
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
        return NMSHandler.getItemHelper().getPotionEffect(type, duration, amplifier, ambient, particles, icon);
    }

    @Override
    public String getPropertyString() {
        if (!(item.getItemMeta() instanceof PotionMeta)) {
            return null;
        }
        PotionMeta meta = (PotionMeta) item.getItemMeta();
        ListTag effects = new ListTag();
        effects.add(meta.getBasePotionData().getType()
                + "," + meta.getBasePotionData().isUpgraded()
                + "," + meta.getBasePotionData().isExtended()
                + (meta.hasColor() ? "," + new ColorTag(meta.getColor()).identify().replace(",", "&comma") : "")
        );
        for (PotionEffect pot : meta.getCustomEffects()) {
            effects.add(stringifyEffect(pot));
        }
        return effects.identify();
    }

    @Override
    public String getPropertyId() {
        return "potion_effects";
    }

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        boolean has = item.getItemMeta() instanceof PotionMeta
                && ((PotionMeta) item.getItemMeta()).hasCustomEffects();

        // <--[tag]
        // @attribute <ItemTag.potion_base_type>
        // @returns ElementTag
        // @mechanism ItemTag.potion_effects
        // @group properties
        // @description
        // Returns the base potion type name for this potion item.
        // The type will be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html>.
        // -->
        if (attribute.startsWith("potion_base_type") && item.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = ((PotionMeta) item.getItemMeta());
            return new ElementTag(meta.getBasePotionData().getType().name()).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.potion_base>
        // @returns ElementTag
        // @mechanism ItemTag.potion_effects
        // @group properties
        // @description
        // Returns the potion type details for this potion item.
        // In the format Type,Level,Extended,Splash,Color
        // The type will be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html>.
        // -->
        if (attribute.startsWith("potion_base") && item.getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = ((PotionMeta) item.getItemMeta());
            return new ElementTag(meta.getBasePotionData().getType().name() + "," + (meta.getBasePotionData().isUpgraded() ? 2 : 1)
                    + "," + meta.getBasePotionData().isExtended() + "," + (item.getBukkitMaterial() == Material.SPLASH_POTION)
                    + (meta.hasColor() ? "," + new ColorTag(meta.getColor()).identify() : "")
                ).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.potion_effects>
        // @returns ListTag
        // @mechanism ItemTag.potion_effects
        // @group properties
        // @description
        // Returns the list of potion effects on this item.
        // The effect type will be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
        // -->
        if (attribute.startsWith("potion_effects") && item.getItemMeta() instanceof PotionMeta) {
            ListTag result = new ListTag();
            PotionMeta meta = ((PotionMeta) item.getItemMeta());
            for (PotionEffect pot : meta.getCustomEffects()) {
                result.add(stringifyEffect(pot));
            }
            return result.getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.has_potion_effect>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.potion_effects
        // @description
        // Returns whether the potion has a potion effect.
        // -->
        if (attribute.startsWith("has_potion_effect")) {
            return new ElementTag(has)
                    .getObjectAttribute(attribute.fulfill(1));
        }

        if (has) {
            if (attribute.startsWith("potion_effect")) {
                PotionMeta meta = ((PotionMeta) item.getItemMeta());

                int potN = attribute.hasContext(1) ? attribute.getIntContext(1) - 1 : 0;
                if (potN < 0 || potN > meta.getCustomEffects().size()) {
                    return null;
                }

                attribute = attribute.fulfill(1);

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].is_splash>
                // @returns ElementTag(Boolean)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns whether the potion is a splash potion.
                // -->
                if (attribute.startsWith("is_splash")) {
                    return new ElementTag(item.getBukkitMaterial() == Material.SPLASH_POTION)
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].is_extended>
                // @returns ElementTag(Boolean)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns whether the potion effect is extended.
                // -->
                if (attribute.startsWith("is_extended")) {
                    return new ElementTag(meta.getBasePotionData().isExtended())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].level>
                // @returns ElementTag(Number)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns the potion effect's level.
                // -->
                if (attribute.startsWith("level")) {
                    return new ElementTag(meta.getBasePotionData().isUpgraded() ? 2 : 1)
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].is_ambient>
                // @returns ElementTag(Boolean)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns whether the potion effect is ambient.
                // "Ambient" effects in vanilla came from a beacon, while non-ambient came from a potion.
                // -->
                if (attribute.startsWith("is_ambient")) {
                    return new ElementTag(meta.getCustomEffects().get(potN).isAmbient())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].icon>
                // @returns ElementTag(Boolean)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns whether the potion effect shows an icon.
                // -->
                if (attribute.startsWith("icon")) {
                    return new ElementTag(meta.getCustomEffects().get(potN).hasIcon()).getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].has_particles>
                // @returns ElementTag(Boolean)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns whether the potion effect has particles.
                // -->
                if (attribute.startsWith("has_particles")) {
                    return new ElementTag(meta.getCustomEffects().get(potN).hasParticles())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].duration>
                // @returns ElementTag(Number)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns the duration in ticks of the potion.
                // -->
                if (attribute.startsWith("duration")) {
                    return new ElementTag(meta.getCustomEffects().get(potN).getDuration())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].amplifier>
                // @returns ElementTag(Number)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns the amplifier level of the potion effect.
                // -->
                if (attribute.startsWith("amplifier")) {
                    return new ElementTag(meta.getCustomEffects().get(potN).getAmplifier())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].type>
                // @returns ElementTag
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns the type of the potion effect.
                // The effect type will be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
                // -->
                if (attribute.startsWith("type")) {
                    return new ElementTag(meta.getCustomEffects().get(potN).getType().getName())
                            .getObjectAttribute(attribute.fulfill(1));
                }

                if (attribute.startsWith("data")) {
                    return new ElementTag(0)
                            .getObjectAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>]>
                // @returns ElementTag
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @warning Don't use this directly, use its sub-tags!
                // @description
                // Returns the potion effect on this item.
                // In the format Effect,Level,Extended,Splash
                // -->
                return new ElementTag(meta.getBasePotionData().getType().name() + "," + (meta.getBasePotionData().isUpgraded() ? 2 : 1)
                        + "," + meta.getBasePotionData().isExtended() + "," + (item.getBukkitMaterial() == Material.SPLASH_POTION))
                        .getObjectAttribute(attribute);
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name potion_effects
        // @input ListTag
        // @description
        // Sets the potion's potion effect(s).
        // Input is a formed like: Type,Upgraded,Extended(,Color)|Effect,Amplifier,Duration,Ambient,Particles,Icon|...
        // For example: SPEED,true,false|SPEED,2,200,false,true,true
        // Second example: REGEN,false,true,RED|REGENERATION,1,500,true,false,false
        // Color can also be used like "255&comma128&comma0" (r,g,b but replace ',' with '&comma').
        // The primary type must be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionType.html>.
        // The effect type must be from <@link url https://hub.spigotmc.org/javadocs/spigot/org/bukkit/potion/PotionEffectType.html>.
        // @tags
        // <ItemTag.potion_effect[<#>]>
        // <ItemTag.potion_effect[<#>].type>
        // <ItemTag.potion_effect[<#>].duration>
        // <ItemTag.potion_effect[<#>].amplifier>
        // <ItemTag.potion_effect[<#>].is_ambient>
        // <ItemTag.potion_effect[<#>].has_particles>
        // <ItemTag.potion_effect[<#>].icon>
        // <server.potion_types>
        // <server.potion_effect_types>
        // -->
        if (mechanism.matches("potion_effects")) {
            ListTag data = mechanism.valueAsType(ListTag.class);
            String[] d1 = data.get(0).split(",");
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            PotionType type;
            try {
                type = PotionType.valueOf(d1[0].toUpperCase());
            }
            catch (IllegalArgumentException ex) {
                mechanism.echoError("Invalid potion type name '" + d1[0] + "'");
                return;
            }
            boolean upgraded = CoreUtilities.equalsIgnoreCase(d1[1], "true");
            boolean extended = CoreUtilities.equalsIgnoreCase(d1[2], "true");
            if (upgraded && !type.isUpgradeable()) {
                mechanism.echoError("Cannot upgrade potion of type '" + type.name() + "'");
                upgraded = false;
            }
            if (extended && !type.isExtendable()) {
                mechanism.echoError("Cannot extend potion of type '" + type.name() + "'");
                upgraded = false;
            }
            if (upgraded && extended) {
                mechanism.echoError("Cannot both upgrade and extend a potion");
                extended = false;
            }
            meta.setBasePotionData(new PotionData(type, extended, upgraded));
            if (d1.length > 3) {
                ColorTag color = ColorTag.valueOf(d1[3].replace("&comma", ","), mechanism.context);
                if (color == null) {
                    mechanism.echoError("Invalid ColorTag input '" + d1[3] + "'");
                }
                meta.setColor(color.getColor());
            }
            meta.clearCustomEffects();
            for (int i = 1; i < data.size(); i++) {
                PotionEffect effect = parseEffect(data.get(i), mechanism.context);
                if (effect == null) {
                    mechanism.echoError("Invalid potion effect '" + data.get(i) + "'");
                    continue;
                }
                meta.addCustomEffect(effect, false);
            }
            item.setItemMeta(meta);
        }
    }
}
