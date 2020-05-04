package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.TurtleEgg;

public class MaterialTurtleEggCount implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof TurtleEgg;
    }

    public static MaterialTurtleEggCount getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialTurtleEggCount((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "egg_count"
    };

    private MaterialTurtleEggCount(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.egg_count>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.egg_count
        // @group properties
        // @description
        // Returns the amount of eggs in a turtle egg material.
        // -->
        PropertyParser.<MaterialTurtleEggCount>registerTag("egg_count", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        });

        // <--[tag]
        // @attribute <MaterialTag.egg_max>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum amount of eggs allowed in a turtle egg material.
        // -->
        PropertyParser.<MaterialTurtleEggCount>registerTag("egg_max", (attribute, material) -> {
            return new ElementTag(material.getMax());
        });

        // <--[tag]
        // @attribute <MaterialTag.egg_min>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the minimum amount of eggs allowed in a turtle egg material.
        // -->
        PropertyParser.<MaterialTurtleEggCount>registerTag("egg_min", (attribute, material) -> {
            return new ElementTag(material.getMin());
        });

    }

    public TurtleEgg getTurtleEgg() {
        return (TurtleEgg) material.getModernData().data;
    }

    public int getCurrent() {
        return getTurtleEgg().getEggs();
    }

    public int getMax() {
        return getTurtleEgg().getMaximumEggs();
    }

    public int getMin() {
        return getTurtleEgg().getMinimumEggs();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "egg_count";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name egg_count
        // @input ElementTag(Number)
        // @description
        // Sets the amount of eggs in a turtle egg material.
        // @tags
        // <MaterialTag.egg_count>
        // -->
        if (mechanism.matches("egg_count") && mechanism.requireInteger()) {
            int count = mechanism.getValue().asInt();
            if (count < getMin() || count > getMax()) {
                Debug.echoError("Egg count value '" + count + "' is not valid. Must be between " + getMin() + " and " + getMax() + ".");
                return;
            }
            getTurtleEgg().setEggs(count);
        }
    }
}
