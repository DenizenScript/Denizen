package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.interfaces.ItemHelper;
import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class ItemPotion implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && (((ItemTag) item).getItemStack().getType() == Material.POTION
                || ((ItemTag) item).getItemStack().getType() == Material.SPLASH_POTION
                || ((ItemTag) item).getItemStack().getType() == Material.LINGERING_POTION
                || ((ItemTag) item).getItemStack().getType() == Material.TIPPED_ARROW);
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
            "potion_base", "has_potion_effect", "potion_effect"
    };

    public static final String[] handledMechs = new String[] {
            "potion_effects"
    };


    private ItemPotion(ItemTag item) {
        this.item = item;
    }

    ItemTag item;


    @Override
    public String getPropertyString() {
        if (!item.getItemStack().hasItemMeta()) {
            return null;
        }
        if (!(item.getItemStack().getItemMeta() instanceof PotionMeta)) {
            return null;
        }
        PotionMeta meta = (PotionMeta) item.getItemStack().getItemMeta();
        ListTag effects = new ListTag();
        effects.add(meta.getBasePotionData().getType()
                + "," + meta.getBasePotionData().isUpgraded()
                + "," + meta.getBasePotionData().isExtended()
                + (meta.hasColor() ? "," + new ColorTag(meta.getColor()).identify().replace(",", "&comma") : "")
        );
        for (PotionEffect pot : meta.getCustomEffects()) {
            StringBuilder sb = new StringBuilder();
            sb.append(pot.getType().getName()).append(",")
                    .append(pot.getAmplifier()).append(",")
                    .append(pot.getDuration()).append(",")
                    .append(pot.isAmbient()).append(",")
                    .append(pot.hasParticles());
            if (pot.getColor() != null) {
                sb.append(",").append(new ColorTag(pot.getColor()).identify().replace(",", "&comma"));
            }
            effects.add(sb.toString());
        }
        return effects.identify();
    }

    @Override
    public String getPropertyId() {
        return "potion_effects";
    }

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        boolean has = item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta() instanceof PotionMeta
                && ((PotionMeta) item.getItemStack().getItemMeta()).hasCustomEffects();


        // <--[tag]
        // @attribute <ItemTag.potion_base>
        // @returns ElementTag
        // @mechanism ItemTag.potion_effects
        // @group properties
        // @description
        // Returns the potion effect on this item.
        // In the format Effect,Level,Extended,Splash,Color
        // -->
        if (attribute.startsWith("potion_base") && item.getItemStack().hasItemMeta() && item.getItemStack().getItemMeta() instanceof PotionMeta) {
            PotionMeta meta = ((PotionMeta) item.getItemStack().getItemMeta());
            return new ElementTag(meta.getBasePotionData().getType().name() + "," + (meta.getBasePotionData().isUpgraded() ? 2 : 1)
                    + "," + meta.getBasePotionData().isExtended() + "," + (item.getItemStack().getType() == Material.SPLASH_POTION)
                    + (meta.hasColor() ? "," + new ColorTag(meta.getColor()).identify() : "")
            ).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <ItemTag.has_potion_effect>
        // @returns ElementTag(Boolean)
        // @mechanism ItemTag.potion
        // @description
        // Returns whether the potion has a potion effect.
        // -->
        if (attribute.startsWith("has_potion_effect")) {
            return new ElementTag(has)
                    .getAttribute(attribute.fulfill(1));
        }

        if (has) {
            if (attribute.startsWith("potion_effect")) {
                PotionMeta meta = ((PotionMeta) item.getItemStack().getItemMeta());

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
                    return new ElementTag(item.getItemStack().getType() == Material.SPLASH_POTION)
                            .getAttribute(attribute.fulfill(1));
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
                            .getAttribute(attribute.fulfill(1));
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
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].is_ambient>
                // @returns ElementTag(Boolean)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns whether the potion effect is ambient.
                // -->
                if (attribute.startsWith("is_ambient")) {
                    return new ElementTag(meta.getCustomEffects().get(potN).isAmbient())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].color>
                // @returns ColorTag
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns the potion effect's color.
                // NOTE: Not supported as of Minecraft version 1.13.
                // -->
                if (attribute.startsWith("color")) {
                    if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                        Debug.echoError("Custom effects with the color option are not supported as of Minecraft version 1.13.");
                        return null;
                    }
                    return new ColorTag(meta.getCustomEffects().get(potN).getColor())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].icon>
                // @returns ElementTag(Boolean)
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns whether the potion effect shows an icon.
                // -->
                if (attribute.startsWith("icon") && NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                    return new ElementTag(meta.getCustomEffects().get(potN).hasIcon()).getAttribute(attribute.fulfill(1));
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
                            .getAttribute(attribute.fulfill(1));
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
                            .getAttribute(attribute.fulfill(1));
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
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <ItemTag.potion_effect[<#>].type>
                // @returns ElementTag
                // @mechanism ItemTag.potion_effects
                // @group properties
                // @description
                // Returns the type of the potion effect.
                // -->
                if (attribute.startsWith("type")) {
                    return new ElementTag(meta.getCustomEffects().get(potN).getType().getName())
                            .getAttribute(attribute.fulfill(1));
                }

                if (attribute.startsWith("data")) {
                    return new ElementTag(0)
                            .getAttribute(attribute.fulfill(1));
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
                        + "," + meta.getBasePotionData().isExtended() + "," + (item.getItemStack().getType() == Material.SPLASH_POTION))
                        .getAttribute(attribute);
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
        // Input is a formed like: Effect,Upgraded,Extended(,Color)|Type,Amplifier,Duration,Ambient,Particles(,Icon)|...
        // For example: SPEED,true,false|SPEED,2,200,false,true,true
        // NOTE: In pre-1.13 Minecraft versions, you could set a color in the custom effects list instead of "icon".
        // @tags
        // <ItemTag.potion_effect[<#>]>
        // <ItemTag.potion_effect[<#>].type>
        // <ItemTag.potion_effect[<#>].duration>
        // <ItemTag.potion_effect[<#>].amplifier>
        // <ItemTag.potion_effect[<#>].is_ambient>
        // <ItemTag.potion_effect[<#>].has_particles>
        // <ItemTag.potion_effect[<#>].color>
        // <ItemTag.potion_effect[<#>].icon>
        // <server.list_potion_types>
        // <server.list_potion_effects>
        // -->
        if (mechanism.matches("potion_effects")) {
            ListTag data = mechanism.valueAsType(ListTag.class);
            String[] d1 = data.get(0).split(",");
            PotionMeta meta = (PotionMeta) item.getItemStack().getItemMeta();
            meta.setBasePotionData(new PotionData(PotionType.valueOf(d1[0].toUpperCase()),
                    CoreUtilities.toLowerCase(d1[2]).equals("true"),
                    CoreUtilities.toLowerCase(d1[1]).equals("true")));
            if (d1.length > 3) {
                meta.setColor(ColorTag.valueOf(d1[3].replace("&comma", ",")).getColor());
            }
            meta.clearCustomEffects();
            ItemHelper itemHelper = NMSHandler.getItemHelper();
            for (int i = 1; i < data.size(); i++) {
                String[] d2 = data.get(i).split(",");
                PotionEffectType type = PotionEffectType.getByName(d2[0].toUpperCase());
                // NOTE: amplifier and duration are swapped around in the input format
                // as compared to the PotionEffect constructor!
                int duration = new ElementTag(d2[2]).asInt();
                int amplifier = new ElementTag(d2[1]).asInt();
                boolean ambient = new ElementTag(d2[3]).asBoolean();
                boolean particles = new ElementTag(d2[4]).asBoolean();
                Color color = null;
                boolean icon = false;
                if (d2.length > 5) {
                    if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13)) {
                        ElementTag check = new ElementTag(d2[5]);
                        if (check.isBoolean()) {
                            icon = check.asBoolean();
                        }
                        else {
                            Debug.echoError("Custom effects with the color option are not supported as of Minecraft version 1.13.");
                        }
                    }
                    else {
                        String check = d2[5].replace("&comma", ",");
                        if (ColorTag.matches(check)) {
                            color = ColorTag.valueOf(check).getColor();
                        }
                    }
                }
                meta.addCustomEffect(itemHelper.getPotionEffect(type, duration, amplifier, ambient, particles, color, icon), false);
            }
            item.getItemStack().setItemMeta(meta);
        }

        if (mechanism.matches("potion")) {
            String[] data = mechanism.getValue().asString().split(",", 4);
            if (data.length < 4) {
                if (mechanism.getValue().isInt()) {
                    item.getItemStack().setDurability((short) mechanism.getValue().asInt());
                }
                else {
                    Debug.echoError("Invalid effect format, use name,amplifier,extended,splash.");
                }
            }
            else {
                ElementTag data1 = new ElementTag(data[1]);
                ElementTag data2 = new ElementTag(data[2]);
                ElementTag data3 = new ElementTag(data[3]);
                PotionType type;
                try {
                    type = PotionType.valueOf(data[0].toUpperCase());
                }
                catch (Exception ex) {
                    Debug.echoError("Invalid potion effect type '" + data[0] + "'");
                    return;
                }
                if (!data1.isInt()) {
                    Debug.echoError("Cannot apply effect '" + data[0] + "': '" + data[1] + "' is not a valid integer!");
                    return;
                }
                if (!data2.isBoolean()) {
                    Debug.echoError("Cannot apply effect '" + data[0] + "': '" + data[2] + "' is not a valid boolean!");
                    return;
                }
                if (!data3.isBoolean()) {
                    Debug.echoError("Cannot apply effect '" + data[0] + "': '" + data[3] + "' is not a valid boolean!");
                    return;
                }
                Potion pot = new Potion(type);
                int d1 = data1.asInt();
                if (d1 >= 1 && d1 <= pot.getType().getMaxLevel()) {
                    pot.setLevel(d1);
                }
                if (!pot.getType().isInstant()) {
                    pot.setHasExtendedDuration(data2.asBoolean());
                }
                pot.setSplash(data3.asBoolean());
                item.setDurability((short) 0);
                pot.apply(item.getItemStack());
            }
        }

    }
}
