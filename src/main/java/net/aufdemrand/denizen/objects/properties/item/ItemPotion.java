package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.*;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.*;

public class ItemPotion implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && (((dItem) item).getItemStack().getType() == Material.POTION
                || ((dItem) item).getItemStack().getType() == Material.SPLASH_POTION);
    }

    public static ItemPotion getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemPotion((dItem) _item);
        }
    }

    private ItemPotion(dItem item) {
        this.item = item;
    }

    dItem item;


    @Override
    public String getPropertyString() {
        if (!item.getItemStack().hasItemMeta()) {
            return null;
        }
        if (!(item.getItemStack().getItemMeta() instanceof PotionMeta)) {
            return null;
        }
        PotionMeta meta = (PotionMeta)item.getItemStack().getItemMeta();
        dList effects = new dList();
        effects.add(meta.getBasePotionData().getType() + "," + meta.getBasePotionData().isUpgraded() + "," + meta.getBasePotionData().isExtended());
        for (PotionEffect pot: meta.getCustomEffects()) {
            StringBuilder sb = new StringBuilder();
            sb.append(pot.getType().getName()).append(",")
                    .append(pot.getAmplifier()).append(",")
                    .append(pot.getDuration()).append(",")
                    .append(pot.isAmbient()).append(",")
                    .append(pot.hasParticles());
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
                && ((PotionMeta)item.getItemStack().getItemMeta()).hasCustomEffects();

        // <--[tag]
        // @attribute <i@item.has_potion_effect>
        // @returns Element(Boolean)
        // @mechanism dItem.potion
        // @description
        // Returns whether the potion has a potion effect.
        // -->
        if (attribute.startsWith("has_potion_effect")) {
            return new Element(has)
                    .getAttribute(attribute.fulfill(1));
        }

        if (has) {
            if (attribute.startsWith("potion_effect")) {
                PotionMeta meta = ((PotionMeta)item.getItemStack().getItemMeta());

                int potN = attribute.hasContext(1) ? attribute.getIntContext(1) - 1: 0;
                if (potN < 0 || potN > meta.getCustomEffects().size()) {
                    return null;
                }

                attribute = attribute.fulfill(1);

                if (attribute.startsWith("is_splash")) {
                    return new Element(item.getItemStack().getType() == Material.SPLASH_POTION)
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect[<#>].is_extend>
                // @returns Element
                // @mechanism dItem.potion_effects
                // @group properties
                // @description
                // Returns whether the potion effect is extended.
                // -->
                if (attribute.startsWith("is_extended")) {
                    return new Element(meta.getBasePotionData().isExtended())
                            .getAttribute(attribute.fulfill(1));
                }

                if (attribute.startsWith("level")) {
                    return new Element(meta.getBasePotionData().isUpgraded() ? 2: 1)
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect[<#>].is_ambient>
                // @returns Element(Boolean)
                // @mechanism dItem.potion_effects
                // @group properties
                // @description
                // Returns whether the potion is ambient.
                // -->
                if (attribute.startsWith("is_ambient")) {
                    return new Element(meta.getCustomEffects().get(potN).isAmbient())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect[<#>].has_particles>
                // @returns Element(Boolean)
                // @mechanism dItem.potion_effects
                // @group properties
                // @description
                // Returns whether the potion has particles.
                // -->
                if (attribute.startsWith("has_particles")) {
                    return new Element(meta.getCustomEffects().get(potN).hasParticles())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect[<#>].duration>
                // @returns Element(Number)
                // @mechanism dItem.potion_effects
                // @group properties
                // @description
                // Returns the duration in ticks of the potion.
                // -->
                if (attribute.startsWith("duration")) {
                    return new Element(meta.getCustomEffects().get(potN).getDuration())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect[<#>].anplifier>
                // @returns Element(Number)
                // @mechanism dItem.potion_effects
                // @group properties
                // @description
                // Returns the amplifier level of the potion.
                // -->
                if (attribute.startsWith("amplifier")) {
                    return new Element(meta.getCustomEffects().get(potN).getAmplifier())
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect[<#>].type>
                // @returns Element
                // @mechanism dItem.potion_effects
                // @group properties
                // @description
                // Returns the type of the potion.
                // -->
                if (attribute.startsWith("type")) {
                    return new Element(meta.getCustomEffects().get(potN).getType().getName())
                            .getAttribute(attribute.fulfill(1));
                }

                if (attribute.startsWith("data")) {
                    return new Element(0)
                            .getAttribute(attribute.fulfill(1));
                }

                // <--[tag]
                // @attribute <i@item.potion_effect[<#>]>
                // @returns Element
                // @mechanism dItem.potion_effects
                // @group properties
                // @warning Don't use this directly, use its sub-tags!
                // @description
                // Returns the potion effect on this item.
                // In the format Effect,Level,Extended,Splash
                // -->
                return new Element(meta.getBasePotionData().getType().name() + "," + (meta.getBasePotionData().isUpgraded() ? 2 : 1)
                + "," + meta.getBasePotionData().isExtended() + "," + (item.getItemStack().getType() == Material.SPLASH_POTION))
                        .getAttribute(attribute);
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name potion_effects
        // @input dList
        // @description
        // Sets the potion's potion effect(s).
        // Input is a formed like: Effect,Upgraded,Extended|Type,Amplifier,Duration,Ambient,Particles|...
        // For example: SPEED,true,false|SPEED,2,200,false,true
        // @tags
        // <i@item.potion_effect>
        // <i@item.potion_effect.type>
        // <i@item.potion_effect.duration>
        // <i@item.potion_effect.amplifier>
        // <i@item.potion_effect.is_ambient>
        // <i@item.potion_effects.has_particles>
        // -->
        if (mechanism.matches("potion_effects")) {
            dList data = mechanism.getValue().asType(dList.class);
            String[] d1 = data.get(1).split(",");
            PotionMeta meta = (PotionMeta)item.getItemStack().getItemMeta();
            meta.setBasePotionData(new PotionData(PotionType.valueOf(d1[0].toUpperCase()),
                    CoreUtilities.toLowerCase(d1[2]).equals("true"),
                    CoreUtilities.toLowerCase(d1[1]).equals("true")));
            meta.clearCustomEffects();
            for (int i = 1; i < data.size(); i++) {
                String[] d2 = data.get(i).split(",");
                meta.addCustomEffect(new PotionEffect(PotionEffectType.getByName(d2[0].toUpperCase()),
                        new Element(d2[2]).asInt(), new Element(d2[1]).asInt(), new Element(d2[3]).asBoolean(),
                        new Element(d2[4]).asBoolean()), false);
            }
        }

        if (mechanism.matches("potion")) {
            String[] data = mechanism.getValue().asString().split(",", 4);
            if (data.length < 4) {
                if (mechanism.getValue().isInt()) {
                    item.getItemStack().setDurability((short) mechanism.getValue().asInt());
                }
                else {
                    dB.echoError("Invalid effect format, use name,amplifier,extended,splash.");
                }
            }
            else {
                Element data1 = new Element(data[1]);
                Element data2 = new Element(data[2]);
                Element data3 = new Element(data[3]);
                PotionType type;
                try {
                    type = PotionType.valueOf(data[0].toUpperCase());
                }
                catch (Exception ex) {
                    dB.echoError("Invalid potion effect type '" + data[0] + "'");
                    return;
                }
                if (type == null) {
                    dB.echoError("Invalid potion effect type '" + data[0] + "'");
                    return;
                }
                if (!data1.isInt()) {
                    dB.echoError("Cannot apply effect '" + data[0] + "': '" + data[1] + "' is not a valid integer!");
                    return;
                }
                if (!data2.isBoolean()) {
                    dB.echoError("Cannot apply effect '" + data[0] + "': '" + data[2] + "' is not a valid boolean!");
                    return;
                }
                if (!data3.isBoolean()) {
                    dB.echoError("Cannot apply effect '" + data[0] + "': '" + data[3] + "' is not a valid boolean!");
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
