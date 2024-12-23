package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Repeater;

public class MaterialDelay extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name delay
    // @input ElementTag(Number)
    // @description
    // Controls the delay of a redstone repeater material.
    // To get the maximum delay of the repeater, see <MaterialTag.max_delay>.
    // To get the minimum delay of the repeater, see <MaterialTag.min_delay>.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Repeater;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "delay";
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getCurrent());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireInteger()) {
            return;
        }
        int delay = value.asInt();
        if (delay < getMin() || delay > getMax()) {
            mechanism.echoError("Delay value '" + delay + "' is not valid. Must be between " + getMin() + " and " + getMax() + ".");
            return;
        }
        getRepeater().setDelay(delay);
    }

    public static void register() {
        autoRegister("delay", MaterialDelay.class, ElementTag.class, true);

        // <--[tag]
        // @attribute <MaterialTag.max_delay>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.delay
        // @group properties
        // @description
        // Returns the maximum delay allowed for the redstone repeater material.
        // -->
        PropertyParser.registerStaticTag(MaterialDelay.class, ElementTag.class, "max_delay", (attribute, material) -> {
            return new ElementTag(material.getMax());
        });

        // <--[tag]
        // @attribute <MaterialTag.min_delay>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.delay
        // @group properties
        // @description
        // Returns the minimum delay allowed for the redstone repeater material.
        // -->
        PropertyParser.registerStaticTag(MaterialDelay.class, ElementTag.class, "min_delay", (attribute, material) -> {
            return new ElementTag(material.getMin());
        });

    }

    public Repeater getRepeater() {
        return (Repeater) material.getModernData();
    }

    public int getCurrent() {
        return getRepeater().getDelay();
    }

    public int getMax() {
        return getRepeater().getMaximumDelay();
    }

    public int getMin() {
        return getRepeater().getMinimumDelay();
    }
}
