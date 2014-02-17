package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.properties.Property;
import net.aufdemrand.denizen.tags.Attribute;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Material;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

import java.util.ArrayList;
import java.util.List;

public class ItemPotion implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((dItem) item).getItemStack().getType() == Material.POTION;
    }

    public static ItemPotion getFrom(dObject _item) {
        if (!describes(_item)) return null;
        else return new ItemPotion((dItem)_item);
    }


    public List<PotionEffect> getPotionEffects() {
        List<PotionEffect> potionEffects = new ArrayList<PotionEffect>();
        try {
            if (item.getMaterial().getData() > 0)
                potionEffects.addAll(Potion.fromItemStack(item.getItemStack()).getEffects());
            potionEffects.addAll(((PotionMeta) item.getItemStack().getItemMeta()).getCustomEffects());
        }
        catch (Exception ex) {
            // TODO: Make this never throw exceptions!
        }
        return potionEffects;
    }


    private ItemPotion(dItem item) {
        this.item = item;
    }

    dItem item;


    @Override
    public String getPropertyString() {
        List<PotionEffect> potionEffects = getPotionEffects();
        if (potionEffects.isEmpty()) return null;
        StringBuilder returnable = new StringBuilder();
        for (PotionEffect effect : potionEffects) {
            returnable.append(effect.getType().getName()).append(",").append(effect.getAmplifier())
                    .append(",").append(new Duration(effect.getDuration()).identify());
        }
        return returnable.substring(0, returnable.length() - 1);
    }

    @Override
    public String getPropertyId() {
        return "potion";
    }

    @Override
    public String getAttribute(Attribute attribute) {
        if (attribute == null) return "null";

        // <--[tag]
        // @attribute <i@item.has_potion_effects>
        // @returns Element(Boolean)
        // @description
        // Returns whether the potion has any potion effects.
        // -->
        if (attribute.startsWith("has_potion_effects")) {
            return new Element(!getPotionEffects().isEmpty())
                    .getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <i@item.potion_effects.with_details>
        // @returns Element
        // @description
        // Returns the potion effect on the potion, with their level, duration, and splashiness.
        // In the format of EFFECT,AMPLIFIER,EXTENDED,SPLASH - EG: POISON,1,true,true
        // -->
        if (attribute.startsWith("potion_effects.with_details")) {
            List<PotionEffect> potionEffects = getPotionEffects();
            if (potionEffects.size() > 0) {
                dList effects = new dList();
                for (PotionEffect potionEffect : potionEffects)
                    effects.add(potionEffect.getType().getName() + ","
                            + potionEffect.getAmplifier() + ","
                            + potionEffect.getDuration());
                return effects.getAttribute(attribute.fulfill(2));
            }
        }

        // <--[tag]
        // @attribute <e@entity.potion_effects.amplifiers>
        // @returns dList
        // @description
        // Returns a list of effects on the potion, showing only the amplifiers.
        // -->
        if (attribute.startsWith("potion_effects.amplifiers")) {
            dList amplifiers = new dList();
            for (PotionEffect effect : getPotionEffects())
                amplifiers.add(String.valueOf(effect.getAmplifier()));
            return amplifiers.getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <i@item.potion_effects.amplifier[<name>]>
        // @returns Element(Number)
        // @description
        // Returns the amplifier of a specified potion effect.
        // -->
        if (attribute.startsWith("potion_effects.amplifier")
                && attribute.hasContext(2)) {
            for (PotionEffect potionEffect : getPotionEffects()) {
                if (potionEffect.getType().getName().equalsIgnoreCase(attribute.getContext(2)))
                    return new Element(potionEffect.getAmplifier())
                            .getAttribute(attribute.fulfill(2));
            }
            return new Element(0)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <e@entity.potion_effects.durations>
        // @returns dList
        // @description
        // Returns a list of effects on the potion, showing only the durations, in ticks.
        // -->
        if (attribute.startsWith("potion_effects.durations")) {
            dList durations = new dList();
            for (PotionEffect effect : getPotionEffects())
                durations.add(String.valueOf(effect.getDuration()));
            return durations.getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <i@item.potion_effects.duration[<name>]>
        // @returns Duration
        // @description
        // Returns the duration, in ticks, of a specified potion effect.
        // -->
        if (attribute.startsWith("potion_effects.duration")
                && attribute.hasContext(2)) {
            for (PotionEffect potionEffect : getPotionEffects()) {
                if (potionEffect.getType().getName().equalsIgnoreCase(attribute.getContext(2)))
                    return new Duration((long) potionEffect.getDuration())
                            .getAttribute(attribute.fulfill(2));
            }
            return new Element(0)
                    .getAttribute(attribute.fulfill(2));
        }

        // <--[tag]
        // @attribute <i@item.potion_effects>
        // @returns dList
        // @description
        // Returns a list of potion effects on this potion.
        // To edit this, use <@link mechanism dItem.potion_effects>
        // -->
        if (attribute.startsWith("potion_effects")) {
            List<PotionEffect> potionEffects = getPotionEffects();
            if (potionEffects.size() > 0) {
                List<String> effects = new ArrayList<String>();
                for (PotionEffect potionEffect : potionEffects)
                    effects.add(potionEffect.getType().getName());
                return new dList(effects)
                        .getAttribute(attribute.fulfill(1));
            }
        }

        return null;
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name potion
        // @input Element
        // @description
        // Sets the potion's custom potion effects.
        // Input is a formed like: Effect,Level,Extended,Splash
        // EG: speed,1,true,false
        // @tags
        // <i@item.potion_effects>
        // <i@item.potion_effects.amplifiers>
        // <i@item.potion_effects.durations>
        // <i@item.potion_effects.with_details>
        // -->
        if (mechanism.matches("potion")) {
            String[] data = mechanism.getValue().asString().split(",", 4);
            if (data.length < 4)
                dB.echoError("Invalid effect format, use name,amplifier,extended,splash.");
            else {
                Element data1 = new Element(data[1]);
                Element data2 = new Element(data[2]);
                Element data3 = new Element(data[3]);
                PotionEffectType type = PotionEffectType.getByName(data[0]);
                if (type == null) {
                    dB.echoError("Invalid potion effect type '" + data[0] + "'");
                    return;
                }
                if (!data1.isInt()) {
                    dB.echoError("Cannot apply effect '" + data[0] +"': '" + data[1] + "' is not a valid integer!");
                    return;
                }
                if (!data2.isBoolean()) {
                    dB.echoError("Cannot apply effect '" + data[0] +"': '" + data[2] + "' is not a valid boolean!");
                    return;
                }
                if (!data3.isBoolean()) {
                    dB.echoError("Cannot apply effect '" + data[0] +"': '" + data[3] + "' is not a valid boolean!");
                    return;
                }
                Potion pot = new Potion(PotionType.getByEffect(type));
                pot.setLevel(data1.asInt());
                pot.setHasExtendedDuration(data2.asBoolean());
                pot.setSplash(data3.asBoolean());
                pot.apply(item.getItemStack());
            }
        }

    }
}
