package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ItemFirework implements Property {

    public static boolean describes(ObjectTag item) {
        return item instanceof ItemTag
                && ((((ItemTag) item).getItemMeta() instanceof FireworkMeta)
                || (((ItemTag) item).getItemMeta() instanceof FireworkEffectMeta));
    }

    public static ItemFirework getFrom(ObjectTag _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemFirework((ItemTag) _item);
        }
    }

    public static final String[] handledMechs = new String[] {
            "firework", "firework_power"
    };

    private ItemFirework(ItemTag _item) {
        item = _item;
    }

    ItemTag item;

    public ListTag getFireworkData() {
        List<FireworkEffect> effects;
        ListTag list = new ListTag();
        if (item.getItemMeta() instanceof FireworkMeta) {
            effects = ((FireworkMeta) item.getItemMeta()).getEffects();
            int power = ((FireworkMeta) item.getItemMeta()).getPower();
            if (power != 0) {
                list.add(String.valueOf(power));
            }
        }
        else {
            effects = Collections.singletonList(((FireworkEffectMeta) item.getItemMeta()).getEffect());
        }
        if (effects != null) {
            for (FireworkEffect effect : effects) {
                if (effect == null) {
                    continue;
                }
                Color ColOne = effect.getColors() != null && effect.getColors().size() > 0 ? effect.getColors().get(0) : Color.BLUE;
                Color ColTwo = effect.getFadeColors() != null && effect.getFadeColors().size() > 0 ? effect.getFadeColors().get(0) : ColOne;
                list.add(effect.hasTrail() + "," + effect.hasFlicker() + "," + effect.getType().name() + "," +
                        ColOne.getRed() + "," + ColOne.getGreen() + "," + ColOne.getBlue() + "," +
                        ColTwo.getRed() + "," + ColTwo.getGreen() + "," + ColTwo.getBlue());
            }
        }
        return list;
    }

    public ListTag getFireworkDataMap() {
        List<FireworkEffect> effects;
        ListTag list = new ListTag();
        if (item.getItemMeta() instanceof FireworkMeta) {
            effects = ((FireworkMeta) item.getItemMeta()).getEffects();
        }
        else {
            effects = Collections.singletonList(((FireworkEffectMeta) item.getItemMeta()).getEffect());
        }
        if (effects != null) {
            for (FireworkEffect effect : effects) {
                if (effect == null) {
                    continue;
                }
                Color ColOne = effect.getColors() != null && effect.getColors().size() > 0 ? effect.getColors().get(0) : Color.BLUE;
                Color ColTwo = effect.getFadeColors() != null && effect.getFadeColors().size() > 0 ? effect.getFadeColors().get(0) : ColOne;
                MapTag effectMap = new MapTag();
                effectMap.putObject("trail", new ElementTag(effect.hasTrail()));
                effectMap.putObject("flicker", new ElementTag(effect.hasFlicker()));
                effectMap.putObject("type", new ElementTag(effect.getType()));
                effectMap.putObject("color", new ColorTag(ColOne));
                effectMap.putObject("fade_color", new ColorTag(ColTwo));
                list.addObject(effectMap);
            }
        }
        return list;
    }

    public int getPower() {
        return item.getItemMeta() instanceof FireworkMeta ? ((FireworkMeta) item.getItemMeta()).getPower() : 0;
    }

    public static void registerTags() {

        // <--[tag]
        // @attribute <ItemTag.firework>
        // @returns ListTag
        // @group properties
        // @mechanism ItemTag.firework
        // @description
        // Returns the firework's property value as a list, matching the non-MapTag format of the mechanism.
        // Consider instead using <@link tag ItemTag.firework_data>
        // -->
        PropertyParser.registerTag(ItemFirework.class, ListTag.class, "firework", (attribute, object) -> {
            return object.getFireworkData();
        });

        // <--[tag]
        // @attribute <ItemTag.firework_data>
        // @returns ListTag
        // @group properties
        // @mechanism ItemTag.firework
        // @description
        // Returns the firework's property value as a ListTag of MapTags, matching the MapTag format of the mechanism.
        // -->
        PropertyParser.registerTag(ItemFirework.class, ListTag.class, "firework_data", (attribute, object) -> {
            return object.getFireworkDataMap();
        });

        // <--[tag]
        // @attribute <ItemTag.firework_power>
        // @returns ElementTag(Number)
        // @group properties
        // @mechanism ItemTag.firework_power
        // @description
        // Returns the firework's power.
        // Power primarily affects how high the firework flies, with each level of power corresponding to approximately half a second of additional flight them.
        // -->
        PropertyParser.registerTag(ItemFirework.class, ElementTag.class, "firework_power", (attribute, object) -> {
            ItemMeta meta = object.item.getItemMeta();
            return meta instanceof FireworkMeta ? new ElementTag(((FireworkMeta) meta).getPower()) : null;
        });
    }

    @Override
    public String getPropertyString() {
        ListTag data = getFireworkData();
        return data.size() > 0 ? data.identify() : null;
    }

    @Override
    public String getPropertyId() {
        return "firework";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object ItemTag
        // @name firework_power
        // @input ElementTag(Number)
        // @description
        // Sets the power of a firework.
        // @tags
        // <ItemTag.firework_power>
        // -->
        if (mechanism.matches("firework_power") && mechanism.requireInteger()) {
            if (item.getItemMeta() instanceof FireworkMeta) {
                ItemMeta meta = item.getItemMeta();
                ((FireworkMeta) meta).setPower(mechanism.getValue().asInt());
                item.setItemMeta(meta);
            }
            else {
                mechanism.echoError("Cannot set the power of a firework effect!");
            }
        }

        // <--[mechanism]
        // @object ItemTag
        // @name firework
        // @input ListTag
        // @description
        // Sets the firework's settings.
        // Each item in the list can be any of the following:
        // 1: Comma-separated effect data in the format: TRAIL,FLICKER,TYPE,RED,GREEN,BLUE,RED,GREEN,BLUE
        // For example: true,false,BALL,255,0,0,0,255,0 would create a trailing ball firework that fades from red to green.
        // 2: A MapTag, with "type", "color", "fade_color", "trail", and "flicker" keys.
        // For example: [type=ball;color=red;fade_color=green;trail=true;flicker=false]
        // 3: A single number, to set the power.
        // Types: ball, ball_large, star, burst, or creeper
        // Note that this is an add operation, provide no input to clear all effects.
        // @tags
        // <ItemTag.firework>
        // <ItemTag.firework_data>
        // -->
        if (mechanism.matches("firework")) {
            ItemMeta meta = item.getItemMeta();
            if (!mechanism.hasValue()) {
                if (meta instanceof FireworkMeta) {
                    ((FireworkMeta) meta).clearEffects();
                }
                else {
                    ((FireworkEffectMeta) meta).setEffect(null);
                }
            }
            else {
                Collection<ObjectTag> list = CoreUtilities.objectToList(mechanism.getValue(), mechanism.context);
                for (ObjectTag object : list) {
                    if (object.canBeType(MapTag.class)) {
                        MapTag effectMap = object.asType(MapTag.class, mechanism.context);
                        FireworkEffect.Builder builder = FireworkEffect.builder();
                        ElementTag type = effectMap.getElement("type");
                        ObjectTag color = effectMap.getObject("color");
                        ObjectTag fadeColor = effectMap.getObject("fade_color");
                        ElementTag trail = effectMap.getElement("trail", "false");
                        ElementTag flicker = effectMap.getElement("flicker", "false");
                        builder.trail(trail.asBoolean());
                        builder.flicker(flicker.asBoolean());
                        if (type != null) {
                            if (type.matchesEnum(FireworkEffect.Type.class)) {
                                builder.with(type.asEnum(FireworkEffect.Type.class));
                            }
                            else {
                                mechanism.echoError("Invalid firework type '" + type.asString() + "'");
                            }
                        }
                        ColorTag co = new ColorTag(Color.BLACK);
                        if (color != null && ColorTag.matches(color.toString())) {
                            co = ColorTag.valueOf(color.toString(), mechanism.context);
                        }
                        else if (color != null) {
                            mechanism.echoError("Invalid color '" + color + "'");
                        }
                        builder.withColor(co.getColor());
                        if (fadeColor != null) {
                            ColorTag fadeCo = ColorTag.valueOf(fadeColor.toString(), mechanism.context);
                            if (fadeCo != null) {
                                builder.withFade(fadeCo.getColor());
                            }
                            else {
                                mechanism.echoError("Invalid fade color '" + fadeColor + "'");
                            }
                        }
                        FireworkEffect built = builder.build();
                        if (meta instanceof FireworkMeta) {
                            ((FireworkMeta) meta).addEffect(built);
                        }
                        else {
                            ((FireworkEffectMeta) meta).setEffect(built);
                        }
                    }
                    else {
                        String effect = object.toString();
                        String[] data = effect.split(",");
                        if (data.length == 9) {
                            FireworkEffect.Builder builder = FireworkEffect.builder();
                            builder.trail(new ElementTag(data[0]).asBoolean());
                            builder.flicker(new ElementTag(data[1]).asBoolean());
                            if (new ElementTag(data[2]).matchesEnum(FireworkEffect.Type.class)) {
                                builder.with(FireworkEffect.Type.valueOf(data[2].toUpperCase()));
                            }
                            else {
                                mechanism.echoError("Invalid firework type '" + data[2] + "'");
                            }
                            builder.withColor(Color.fromRGB(new ElementTag(data[3]).asInt(),
                                    new ElementTag(data[4]).asInt(),
                                    new ElementTag(data[5]).asInt()));
                            builder.withFade(Color.fromRGB(new ElementTag(data[6]).asInt(),
                                    new ElementTag(data[7]).asInt(),
                                    new ElementTag(data[8]).asInt()));

                            FireworkEffect built = builder.build();
                            if (meta instanceof FireworkMeta) {
                                ((FireworkMeta) meta).addEffect(built);
                            }
                            else {
                                ((FireworkEffectMeta) meta).setEffect(built);
                            }
                        }
                        else if (data.length == 1) {
                            if (meta instanceof FireworkMeta) {
                                ((FireworkMeta) meta).setPower(new ElementTag(data[0]).asInt());
                            }
                            else {
                                mechanism.echoError("Cannot set the power of a firework effect!");
                            }
                        }
                        else {
                            mechanism.echoError("Invalid firework data '" + effect + "'");
                        }
                    }
                }
            }
            item.setItemMeta(meta);
        }
    }
}
