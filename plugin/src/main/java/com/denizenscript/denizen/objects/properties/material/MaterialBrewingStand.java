package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.BrewingStand;

public class MaterialBrewingStand implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof BrewingStand;
    }

    public static MaterialBrewingStand getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialBrewingStand((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "bottles"
    };

    public MaterialBrewingStand(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.bottles>
        // @returns ListTag
        // @mechanism MaterialTag.bottles
        // @group properties
        // @description
        // Returns a list of booleans that represent whether a slot in a brewing stand has a bottle.
        // Under current implementation this always returns exactly 3 values, like "true|false|true".
        // -->
        PropertyParser.registerStaticTag(MaterialBrewingStand.class, ListTag.class, "bottles", (attribute, material) -> {
            return material.getBottleBooleans();
        });
    }

    public BrewingStand getBrewingStand() {
        return (BrewingStand) material.getModernData();
    }

    public int getMaxBottles() {
        return getBrewingStand().getMaximumBottles();
    }

    public ListTag getBottleBooleans() {
        ListTag result = new ListTag();
        for (int i = 0; i < getMaxBottles(); i++) {
            result.addObject(new ElementTag(getBrewingStand().hasBottle(i)));
        }
        return result;
    }

    @Override
    public String getPropertyString() {
        return getBottleBooleans().identify();
    }

    @Override
    public String getPropertyId() {
        return "bottles";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name bottles
        // @input ListTag
        // @description
        // Sets the bottles in a brewing stand. Input is a list of booleans representing whether that slot has a bottle.
        // @tags
        // <MaterialTag.bottles>
        // -->
        if (mechanism.matches("bottles")) {
            ListTag bottles = mechanism.valueAsType(ListTag.class);
            if (bottles.size() > getMaxBottles()) {
                mechanism.echoError("Too many values specified! Brewing stand has a maximum of " + getMaxBottles() + " bottles.");
                return;
            }
            for (int i = 0; i < bottles.size(); i++) {
                getBrewingStand().setBottle(i, new ElementTag(bottles.get(i)).asBoolean());
            }
        }
    }

}
