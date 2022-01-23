package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.objects.ColorTag;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.tags.Attribute;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

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
            "firework"
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
                Color ColOne = effect.getColors() != null && !effect.getColors().isEmpty() ? effect.getColors().get(0) : Color.BLUE;
                Color ColTwo = effect.getFadeColors() != null && effect.getFadeColors().isEmpty() ? effect.getFadeColors().get(0) : ColOne;
                list.add(effect.hasTrail() + "," + effect.hasFlicker() + "," + effect.getType().name() + "," +
                        ColOne.getRed() + "," + ColOne.getGreen() + "," + ColOne.getBlue() + "," +
                        ColTwo.getRed() + "," + ColTwo.getGreen() + "," + ColTwo.getBlue());
            }
        }
        return list;
    }

    public MapTag getFireworkDataMap(){
        List<FireworkEffect> effects;
        MapTag map = new MapTag();
        if (item.getItemMeta() instanceof FireworkMeta) {
            FireworkMeta meta = (FireworkMeta) item.getItemMeta();
            effects = meta.getEffects();
            int power = meta.getPower();
            if (power != 0) {
                map.putObject("power", new ElementTag(power));
            }
        }
        else {
            effects = Collections.singletonList(((FireworkEffectMeta) item.getItemMeta()).getEffect());
        }
        if (effects != null) {
            for (int index = 0;index < effects.size();index++) {
                FireworkEffect effect = effects.get(index);
                if (effect == null) {
                    continue;
                }
                Color ColOne = effect.getColors() != null && !effect.getColors().isEmpty() ? effect.getColors().get(0) : Color.BLUE;
                Color ColTwo = effect.getFadeColors() != null && !effect.getFadeColors().isEmpty() ? effect.getFadeColors().get(0) : ColOne;
                MapTag effectMap = new MapTag();
                effectMap.putObject("trail", new ElementTag(effect.hasTrail()));
                effectMap.putObject("flicker", new ElementTag(effect.hasFlicker()));
                effectMap.putObject("type", new ElementTag(effect.getType().name()));
                effectMap.putObject("color", new ColorTag(ColOne));
                effectMap.putObject("fadeColor", new ColorTag(ColTwo));
                map.putObject(String.valueOf(index+1), effectMap);
            }
        }
        return map;
    }

    public static void registerTags(){

        // <--[tag]
        // @attribute <ItemTag.firework>
        // @returns ListTag
        // @group properties
        // @mechanism ItemTag.firework
        // @description
        // Returns the firework's property value as a list, matching the format of the mechanism.
        // -->
        PropertyParser.<ItemFirework, ListTag>registerTag(ListTag.class, "firework", (attribute, object) -> {
            return object.getFireworkData();
        });

        PropertyParser.<ItemFirework, MapTag>registerTag(MapTag.class, "firework_map", (attribute, object) -> {
            return object.getFireworkDataMap();
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
        // @name firework
        // @input ListTag
        // @description
        // Sets the firework's settings.
        // Each item in the list is formatted as: TRAIL,FLICKER,TYPE,RED,GREEN,BLUE,RED,GREEN,BLUE
        // For example: true,false,BALL,255,0,0,0,255,0 would create a trailing ball firework that fades from red to green.
        // Optionally add a list entry that's just a single number to set the power.
        // Types: ball, ball_large, star, burst, or creeper
        // @tags
        // <ItemTag.firework>
        // -->
        if (mechanism.matches("firework")) {
            ItemMeta meta = item.getItemMeta();
            if (mechanism.getValue().asString().startsWith("map@")) {
                MapTag map = mechanism.valueAsType(MapTag.class);
                if (meta instanceof FireworkMeta && map.getObject("power") != null) {
                    ((FireworkMeta) meta).setPower(map.getObject("power").asElement().asInt());
                }
                else if (!(meta instanceof FireworkMeta)) {
                    mechanism.echoError("Cannot set the power of a firework effect!");
                }
                map.map.remove(new StringHolder("power"));
                for (int index = 1; index <= map.keys().size(); index++) {
                    if (map.getObject(String.valueOf(index)) == null) {
                        mechanism.echoError("Invalid effect key '" + map.keys().get(index - 1) + "', keys must be ordered integers.");
                        continue;
                    }
                    Debug.log("Reading Effect " + index + ", with key name " + map.keys().get(index - 1));
                    MapTag effectMap = MapTag.getMapFor(map.getObject(String.valueOf(index)), mechanism.context);
                    Debug.log("Effect Map: " + effectMap.identify());
                    FireworkEffect.Builder builder = FireworkEffect.builder();
                    builder.trail(effectMap.getObject("trail") != null && effectMap.getObject("trail").asElement().asBoolean());
                    builder.flicker(effectMap.getObject("flicker") != null && effectMap.getObject("flicker").asElement().asBoolean());
                    if (effectMap.getObject("type") != null) {
                        ElementTag effectType = effectMap.getObject("type").asElement();
                        if (effectType.matchesEnum(FireworkEffect.Type.values())) {
                            builder.with(FireworkEffect.Type.valueOf(effectType.asString().toUpperCase()));
                        } else {
                            mechanism.echoError("Invalid firework type '" + effectType.asString() + "' for effect '" + index + "'");
                        }
                    }
                    if (effectMap.getObject("color") != null) {
                        ColorTag color = ColorTag.valueOf(effectMap.getObject("color").toString(), mechanism.context);
                        if (color != null) {
                            builder.withColor(color.getColor());
                        }
                        else {
                            mechanism.echoError("Invalid color '" + effectMap.getObject("color") + "' for effect '" + index + "'");
                            builder.withColor(Color.BLACK);
                        }
                    }
                    else {
                        mechanism.echoError("No color specified for effect '" + index + "', color is required");
                        builder.withColor(Color.BLACK);
                    }
                    if (effectMap.getObject("fadeColor") != null) {
                        ColorTag fadeColor = ColorTag.valueOf(effectMap.getObject("fadeColor").toString(), mechanism.context);
                        if (fadeColor != null) {
                            builder.withFade(fadeColor.getColor());
                        }
                        else {
                            mechanism.echoError("Invalid fade color '" + effectMap.getObject("color") + "' for effect '" + index + "'");
                            builder.withFade(Color.BLACK);
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
            }
            else {
                ListTag fireworks = mechanism.valueAsType(ListTag.class);
                for (String effect : fireworks) {
                    String[] data = effect.split(",");
                    if (data.length == 9) {
                        FireworkEffect.Builder builder = FireworkEffect.builder();
                        builder.trail(new ElementTag(data[0]).asBoolean());
                        builder.flicker(new ElementTag(data[1]).asBoolean());
                        if (new ElementTag(data[2]).matchesEnum(FireworkEffect.Type.values())) {
                            builder.with(FireworkEffect.Type.valueOf(data[2].toUpperCase()));
                        } else {
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
                        } else {
                            ((FireworkEffectMeta) meta).setEffect(built);
                        }
                    } else if (data.length == 1) {
                        if (meta instanceof FireworkMeta) {
                            ((FireworkMeta) meta).setPower(new ElementTag(data[0]).asInt());
                        } else {
                            mechanism.echoError("Cannot set the power of a firework effect!");
                        }
                    } else {
                        mechanism.echoError("Invalid firework data '" + effect + "'");
                    }
                }
            }
            item.setItemMeta(meta);
        }
    }
}
