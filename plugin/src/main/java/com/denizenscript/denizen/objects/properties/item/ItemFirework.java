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
                Color ColOne = effect.getColors() != null && effect.getColors().size() > 0 ? effect.getColors().get(0) : Color.BLUE;
                Color ColTwo = effect.getFadeColors() != null && effect.getFadeColors().size() > 0 ? effect.getFadeColors().get(0) : ColOne;
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
            int size = effects.size();
            for (int i = 0; i < size; i++) {
                FireworkEffect effect = effects.get(i);
                if (effect == null) {
                    continue;
                }
                Color ColOne = effect.getColors() != null && effect.getColors().size() > 0 ? effect.getColors().get(0) : Color.BLUE;
                Color ColTwo = effect.getFadeColors() != null && effect.getFadeColors().size() > 0 ? effect.getFadeColors().get(0) : ColOne;
                MapTag effectMap = new MapTag();
                effectMap.putObject("trail", new ElementTag(effect.hasTrail()));
                effectMap.putObject("flicker", new ElementTag(effect.hasFlicker()));
                effectMap.putObject("type", new ElementTag(effect.getType().name()));
                effectMap.putObject("color", new ColorTag(ColOne));
                effectMap.putObject("fadeColor", new ColorTag(ColTwo));
                map.putObject(String.valueOf(i+1), effectMap);
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

        // <--[tag]
        // @attribute <ItemTag.firework_map>
        // @returns MapTag
        // @group properties
        // @mechanism ItemTag.firework
        // @description
        // Returns the firework's property value as a map, matching the MapTag format of the mechanism.
        // -->
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
        // Can take MapTag input: an optional key for power and indexed keys for each effect,
        // with "type", "color", "fadeColor", "trail", and "flicker" keys. All but "color" are optional.
        // Types: ball, ball_large, star, burst, or creeper
        // Notice that this is an ADD operation, provide no input to clear all effects.
        // @tags
        // <ItemTag.firework>
        // <ItemTag.firework_map>
        // -->
        if (mechanism.matches("firework")) {
            ItemMeta meta = item.getItemMeta();
            if (!mechanism.hasValue()) {
                if (meta instanceof FireworkMeta) {
                    ((FireworkMeta) meta).clearEffects();
                    ((FireworkMeta) meta).setPower(0);
                }
                else {
                    ((FireworkEffectMeta) meta).setEffect(null);
                }
            }
            else if (mechanism.getValue().asString().startsWith("map@")) {
                MapTag map = mechanism.valueAsType(MapTag.class);
                if (meta instanceof FireworkMeta && map.getObject("power") != null) {
                    ((FireworkMeta) meta).setPower(map.getObject("power").asElement().asInt());
                }
                else if (!(meta instanceof FireworkMeta)) {
                    mechanism.echoError("Cannot set the power of a firework effect!");
                }
                map.map.remove(new StringHolder("power"));
                ListTag keys = map.keys();
                int keysSize = keys.size();
                for (int i = 1; i <= keysSize; i++) {
                    if (map.getObject(String.valueOf(i)) == null) {
                        mechanism.echoError("Invalid effect key '" + keys.get(i - 1) + "', keys must be ordered integers.");
                        continue;
                    }
                    MapTag effectMap = map.getObject(String.valueOf(i)).asType(MapTag.class, mechanism.context);
                    FireworkEffect.Builder builder = FireworkEffect.builder();
                    ObjectTag type = effectMap.getObject("type");
                    ObjectTag color = effectMap.getObject("color");
                    ObjectTag fadeColor = effectMap.getObject("fadeColor");
                    ObjectTag trail = effectMap.getObject("trail");
                    ObjectTag flicker = effectMap.getObject("flicker");
                    builder.trail(trail != null && trail.asElement().asBoolean());
                    builder.flicker(flicker != null && flicker.asElement().asBoolean());
                    if (type != null) {
                        ElementTag effectType = type.asElement();
                        if (effectType.matchesEnum(FireworkEffect.Type.values())) {
                            builder.with(FireworkEffect.Type.valueOf(effectType.asString().toUpperCase()));
                        }
                        else {
                            mechanism.echoError("Invalid firework type '" + effectType.asString() + "' for effect " + i);
                        }
                    }
                    ColorTag co = new ColorTag(Color.BLACK);
                    if (color != null && ColorTag.matches(color.toString())) {
                        co = ColorTag.valueOf(color.toString(), mechanism.context);
                    }
                    else if (color != null) {
                        mechanism.echoError("Invalid color '" + color + "' for effect " + i);
                    }
                    builder.withColor(co.getColor());
                    if (fadeColor != null) {
                        ColorTag fadeCo = ColorTag.valueOf(fadeColor.toString(), mechanism.context);
                        if (fadeCo != null) {
                            builder.withFade(fadeCo.getColor());
                        }
                        else {
                            mechanism.echoError("Invalid fade color '" + fadeColor + "' for effect " + i);
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
            item.setItemMeta(meta);
        }
    }
}
