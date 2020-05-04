package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.TurtleEgg;

public class MaterialTurtleEggStage implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof TurtleEgg;
    }

    public static MaterialTurtleEggStage getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialTurtleEggStage((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "egg_stage"
    };

    private MaterialTurtleEggStage(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.egg_stage_max>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum allowed hatching stage for a turtle egg material.
        // -->
        PropertyParser.<MaterialTurtleEggStage>registerTag("egg_stage_max", (attribute, material) -> {
            return new ElementTag(material.getHatchMax());
        });

        // <--[tag]
        // @attribute <MaterialTag.egg_stage>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the egg hatching stage for a turtle egg material.
        // -->
        PropertyParser.<MaterialTurtleEggStage>registerTag("egg_stage", (attribute, material) -> {
            return new ElementTag(material.getHatch());
        });

    }

    public TurtleEgg getTurtleEgg() {
        return (TurtleEgg) material.getModernData().data;
    }

    public int getCurrent() {
        return getTurtleEgg().getEggs();
    }

    public int getHatchMax() {
        return getTurtleEgg().getMaximumHatch();
    }

    public int getHatch() {
        return getTurtleEgg().getHatch();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "egg_stage";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name egg_stage
        // @input ElementTag(Number)
        // @description
        // Sets the egg hatching stage for a turtle egg material.
        // @tags
        // <MaterialTag.egg_stage>
        // -->
        if (mechanism.matches("egg_stage") && mechanism.requireInteger()) {
            int count = mechanism.getValue().asInt();
            if (count < 0 || count > getHatchMax()) {
                Debug.echoError("Egg hatch stage value '" + count + "' is not valid. Must be between 0 and " + getHatchMax() + ".");
                return;
            }
            getTurtleEgg().setHatch(count);
        }
    }
}
