package com.denizenscript.denizen.objects.properties.item;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.ItemTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
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

    public static final String[] handledTags = new String[] {
            "firework"
    };

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
            effects = Arrays.asList(((FireworkEffectMeta) item.getItemMeta()).getEffect());
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

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <ItemTag.firework>
        // @returns ListTag
        // @group properties
        // @mechanism ItemTag.firework
        // @description
        // Returns the firework's property value as a list, matching the format of the mechanism.
        // -->
        // TODO: Easy tags to get individual parts...
        if (attribute.startsWith("firework")) {
            return getFireworkData().getObjectAttribute(attribute.fulfill(1));
        }

        return null;
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
        // @tags
        // <ItemTag.firework>
        // -->
        if (mechanism.matches("firework")) {
            ListTag fireworks = mechanism.valueAsType(ListTag.class);
            ItemMeta meta = item.getItemMeta();
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
                        Debug.echoError("Invalid firework type '" + data[2] + "'");
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
                        Debug.echoError("Cannot set the power of a firework effect!");
                    }
                }
                else {
                    Debug.echoError("Invalid firework data '" + effect + "'");
                }
            }
            item.setItemMeta(meta);
        }
    }
}
