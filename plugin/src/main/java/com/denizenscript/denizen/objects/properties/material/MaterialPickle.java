package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.SeaPickle;

public class MaterialPickle implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof SeaPickle;
    }

    public static MaterialPickle getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialPickle((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "pickle_count"
    };


    private MaterialPickle(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.pickle_count>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the amount of pickles in a Sea Pickle material.
        // -->
        PropertyParser.<MaterialPickle>registerTag("pickle_count", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        });

    }

    public SeaPickle getSeaPickle() {
        return (SeaPickle) material.getModernData().data;
    }

    public int getCurrent() {
        return getSeaPickle().getPickles();
    }

    public int getMax() {
        return getSeaPickle().getMaximumPickles();
    }

    public int getMin() {
        return getSeaPickle().getMinimumPickles();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "pickle_count";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name pickle_count
        // @input ElementTag(Number)
        // @description
        // Sets the amount of pickles in a Sea Pickle material.
        // @tags
        // <MaterialTag.pickle_count>
        // -->
        if (mechanism.matches("pickle_count") && mechanism.requireInteger()) {
            int count = mechanism.getValue().asInt();
            if (count < getMin() || count > getMax()) {
                Debug.echoError("Pickle count value '" + count + "' is not valid. Must be between" + getMin() + " and " + getMax() + ".");
                return;
            }
            getSeaPickle().setPickles(count);
        }
    }
}
