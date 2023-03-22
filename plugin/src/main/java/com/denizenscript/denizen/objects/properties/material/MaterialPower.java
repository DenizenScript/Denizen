package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.AnaloguePowerable;

public class MaterialPower implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof AnaloguePowerable;
    }

    public static MaterialPower getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialPower((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "power"
    };

    public MaterialPower(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.power>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.power
        // @group properties
        // @description
        // Returns the redstone power level of an analogue-powerable block.
        // -->
        PropertyParser.registerStaticTag(MaterialPower.class, ElementTag.class, "power", (attribute, material) -> {
            return new ElementTag(((AnaloguePowerable) material.material.getModernData()).getPower());
        });

        // <--[tag]
        // @attribute <MaterialTag.max_power>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.power
        // @group properties
        // @description
        // Returns the maximum redstone power an analogue-powerable block can have.
        // -->
        PropertyParser.registerStaticTag(MaterialPower.class, ElementTag.class, "max_power", (attribute, material) -> {
            return new ElementTag(((AnaloguePowerable) material.material.getModernData()).getMaximumPower());
        });
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(((AnaloguePowerable) material.getModernData()).getPower());
    }

    @Override
    public String getPropertyId() {
        return "power";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name power
        // @input ElementTag(Number)
        // @description
        // Sets the redstone power level of an analogue-powerable block.
        // @tags
        // <MaterialTag.power>
        // <MaterialTag.max_power>
        // -->
        if (mechanism.matches("power") && mechanism.requireInteger()) {
            int power = mechanism.getValue().asInt();
            AnaloguePowerable powerable = (AnaloguePowerable) material.getModernData();
            if (power < 0 || power > powerable.getMaximumPower()) {
                mechanism.echoError("Material power mechanism value '" + power + "' is not valid. Must be between 0 and " + powerable.getMaximumPower() + ".");
                return;
            }
            powerable.setPower(power);
        }
    }
}
