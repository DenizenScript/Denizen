package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Repeater;

public class MaterialDelay implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Repeater;
    }

    public static MaterialDelay getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDelay((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "delay"
    };

    private MaterialDelay(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.delay>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.delay
        // @group properties
        // @description
        // Returns the current delay of a redstone repeater material.
        // -->
        PropertyParser.<MaterialDelay>registerTag("delay", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        });

        // <--[tag]
        // @attribute <MaterialTag.max_delay>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.delay
        // @group properties
        // @description
        // Returns the maximum delay allowed for the redstone repeater material.
        // -->
        PropertyParser.<MaterialDelay>registerTag("max_delay", (attribute, material) -> {
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
        PropertyParser.<MaterialDelay>registerTag("min_delay", (attribute, material) -> {
            return new ElementTag(material.getMin());
        });

    }

    public Repeater getRepeater() {
        return (Repeater) material.getModernData().data;
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

    @Override
    public String getPropertyString() {
        return String.valueOf(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "delay";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name delay
        // @input ElementTag(Number)
        // @description
        // Sets the delay of a redstone repeater material.
        // @tags
        // <MaterialTag.delay>
        // <MaterialTag.max_delay>
        // <MaterialTag.min_delay>
        // -->
        if (mechanism.matches("delay") && mechanism.requireInteger()) {
            int delay = mechanism.getValue().asInt();
            if (delay < getMin() || delay > getMax()) {
                Debug.echoError("Delay value '" + delay + "' is not valid. Must be between " + getMin() + " and " + getMax() + ".");
                return;
            }
            getRepeater().setDelay(delay);
        }
    }
}
