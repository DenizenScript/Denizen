package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.BrewingStand;

public class MaterialBottles extends MaterialProperty<ListTag> {

    // <--[property]
    // @object MaterialTag
    // @name bottles
    // @input ListTag
    // @description
    // Controls the list of booleans that represent whether a slot in a brewing stand has a bottle.
    // Under current implementation, this always returns/requires exactly 3 values, like "true|false|true".
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof BrewingStand;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "bottles";
    }

    @Override
    public ListTag getPropertyValue() {
        return getBottleBooleans();
    }

    @Override
    public void setPropertyValue(ListTag list, Mechanism mechanism) {
        if (list.size() > getMaxBottles()) {
            mechanism.echoError("Too many values specified! Brewing stand has a maximum of " + getMaxBottles() + " bottles.");
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            getBrewingStand().setBottle(i, new ElementTag(list.get(i)).asBoolean());
        }
    }

    public static void register() {
        autoRegister("bottles", MaterialBottles.class, ListTag.class, true);
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
}
