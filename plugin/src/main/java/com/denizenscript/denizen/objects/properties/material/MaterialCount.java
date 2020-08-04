package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.block.data.type.TurtleEgg;

public class MaterialCount implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData().data instanceof SeaPickle
                || ((MaterialTag) material).getModernData().data instanceof TurtleEgg);
    }

    public static MaterialCount getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialCount((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "count", "pickle_count"
    };

    private MaterialCount(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.count>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the amount of pickles in a sea pickle material, or eggs in a turtle egg material.
        // -->
        PropertyParser.<MaterialCount>registerTag("count", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        }, "pickle_count");

        // <--[tag]
        // @attribute <MaterialTag.count_max>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the maximum amount of pickles allowed in a sea pickle material, or eggs in a turtle egg material.
        // -->
        PropertyParser.<MaterialCount>registerTag("count_max", (attribute, material) -> {
            return new ElementTag(material.getMax());
        }, "pickle_max");

        // <--[tag]
        // @attribute <MaterialTag.count_min>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the minimum amount of pickles allowed in a sea pickle material, or eggs in a turtle egg material.
        // -->
        PropertyParser.<MaterialCount>registerTag("count_min", (attribute, material) -> {
            return new ElementTag(material.getMin());
        }, "pickle_min");

    }

    public SeaPickle getSeaPickle() {
        return (SeaPickle) material.getModernData().data;
    }

    public TurtleEgg getTurtleEgg() {
        return (TurtleEgg) material.getModernData().data;
    }

    public boolean isSeaPickle() {
        return material.getModernData().data instanceof SeaPickle;
    }

    public int getCurrent() {
        if (isSeaPickle()) {
            return getSeaPickle().getPickles();
        }
        else {
            return getTurtleEgg().getEggs();
        }
    }

    public int getMax() {
        if (isSeaPickle()) {
            return getSeaPickle().getMaximumPickles();
        }
        else {
            return getTurtleEgg().getMaximumEggs();
        }
    }

    public int getMin() {
        if (isSeaPickle()) {
            return getSeaPickle().getMinimumPickles();
        }
        else {
            return getTurtleEgg().getMinimumEggs();
        }
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "count";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name count
        // @input ElementTag(Number)
        // @description
        // Sets the amount of pickles in a sea pickle material, or eggs in a turtle egg material.
        // @tags
        // <MaterialTag.count>
        // <MaterialTag.count_min>
        // <MaterialTag.count_max>
        // -->
        if ((mechanism.matches("count") || (mechanism.matches("pickle_count"))) && mechanism.requireInteger()) {
            int count = mechanism.getValue().asInt();
            if (count < getMin() || count > getMax()) {
                Debug.echoError("Material count mechanism value '" + count + "' is not valid. Must be between " + getMin() + " and " + getMax() + ".");
                return;
            }
            if (isSeaPickle()) {
                getSeaPickle().setPickles(count);
            }
            else {
                getTurtleEgg().setEggs(count);
            }
        }
    }
}
