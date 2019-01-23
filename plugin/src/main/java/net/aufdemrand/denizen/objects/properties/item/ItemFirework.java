package net.aufdemrand.denizen.objects.properties.item;

import net.aufdemrand.denizen.objects.dItem;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.inventory.meta.FireworkEffectMeta;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

public class ItemFirework implements Property {

    public static boolean describes(dObject item) {
        return item instanceof dItem
                && ((((dItem) item).getItemStack().getItemMeta() instanceof FireworkMeta)
                || (((dItem) item).getItemStack().getItemMeta() instanceof FireworkEffectMeta));
    }

    public static ItemFirework getFrom(dObject _item) {
        if (!describes(_item)) {
            return null;
        }
        else {
            return new ItemFirework((dItem) _item);
        }
    }

    public static final String[] handledTags = new String[]{
            "firework"
    };

    public static final String[] handledMechs = new String[] {
            "firework"
    };


    private ItemFirework(dItem _item) {
        item = _item;
    }

    dItem item;

    public dList getFireworkData() {
        List<FireworkEffect> effects;
        dList list = new dList();
        if (item.getItemStack().getItemMeta() instanceof FireworkMeta) {
            effects = ((FireworkMeta) item.getItemStack().getItemMeta()).getEffects();
            int power = ((FireworkMeta) item.getItemStack().getItemMeta()).getPower();
            if (power != 0) {
                list.add(String.valueOf(power));
            }
        }
        else {
            effects = Arrays.asList(((FireworkEffectMeta) item.getItemStack().getItemMeta()).getEffect());
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
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <i@item.firework>
        // @returns dList
        // @group properties
        // @mechanism dItem.firework
        // @description
        // Returns the firework's property value as a list, matching the format of the mechanism.
        // -->
        // TODO: Easy tags to get individual parts...
        if (attribute.startsWith("firework")) {
            return getFireworkData().getAttribute(attribute.fulfill(1));
        }

        return null;
    }


    @Override
    public String getPropertyString() {
        dList data = getFireworkData();
        return data.size() > 0 ? data.identify() : null;
    }

    @Override
    public String getPropertyId() {
        return "firework";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dItem
        // @name firework
        // @input dList
        // @description
        // Sets the firework's settings.
        // Each item in the list is formatted as: TRAIL,FLICKER,TYPE,RED,GREEN,BLUE,RED,GREEN,BLUE
        // For example: true,false,BALL,255,0,0,0,255,0 would create a trailing ball firework that fades from red to green.
        // Optionally add a list entry that's just a single number to set the power.
        // @tags
        // <i@item.firework>
        // -->

        if (mechanism.matches("firework")) {
            dList fireworks = mechanism.getValue().asType(dList.class);
            ItemMeta meta = item.getItemStack().getItemMeta();
            for (String effect : fireworks) {
                String[] data = effect.split(",");
                if (data.length == 9) {
                    FireworkEffect.Builder builder = FireworkEffect.builder();
                    builder.trail(new Element(data[0]).asBoolean());
                    builder.flicker(new Element(data[1]).asBoolean());
                    if (new Element(data[2]).matchesEnum(FireworkEffect.Type.values())) {
                        builder.with(FireworkEffect.Type.valueOf(data[2].toUpperCase()));
                    }
                    else {
                        dB.echoError("Invalid firework type '" + data[2] + "'");
                    }
                    builder.withColor(Color.fromRGB(new Element(data[3]).asInt(),
                            new Element(data[4]).asInt(),
                            new Element(data[5]).asInt()));
                    builder.withFade(Color.fromRGB(new Element(data[6]).asInt(),
                            new Element(data[7]).asInt(),
                            new Element(data[8]).asInt()));

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
                        ((FireworkMeta) meta).setPower(new Element(data[0]).asInt());
                    }
                    else {
                        dB.echoError("Cannot set the power of a firework effect!");
                    }
                }
                else {
                    dB.echoError("Invalid firework data '" + effect + "'");
                }
            }
            item.getItemStack().setItemMeta(meta);
        }
    }
}
