package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Brushable;

public class MaterialDustLevel extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name dust_level
    // @input ElementTag(Number)
    // @description
    // Controls the dust level of a suspicious block.
    // After setting the level, the level will reset to 0 when a player starts to brush the block.
    // See also <@link tag MaterialTag.maximum_dust_level>.
    // -->

    public static boolean describes(MaterialTag material) {
        return material.getModernData() instanceof Brushable;
    }

    @Override
    public boolean isDefaultValue(ElementTag value) {
        return value.asInt() == 0;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getBrushable().getDusted());
    }

    @Override
    public String getPropertyId() {
        return "dust_level";
    }

    @Override
    public void setPropertyValue(ElementTag level, Mechanism mechanism) {
        if (!mechanism.requireInteger()) {
            return;
        }
        if (level.asInt() < 0 || level.asInt() > getMaxLevel()) {
            mechanism.echoError("Dust level can only be an integer from 0-" + getMaxLevel() + "!");
            return;
        }
        getBrushable().setDusted(level.asInt());
    }

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.maximum_dust_level>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum dust level for a suspicious block.
        // -->
        PropertyParser.registerStaticTag(MaterialDustLevel.class, ElementTag.class, "maximum_dust_level", (attribute, prop) -> {
            return new ElementTag(prop.getMaxLevel());
        });

        autoRegister("dust_level", MaterialDustLevel.class, ElementTag.class, false);
    }

    public int getMaxLevel() {
        return getBrushable().getMaximumDusted();
    }

    public Brushable getBrushable() {
        return (Brushable) getBlockData();
    }
}
