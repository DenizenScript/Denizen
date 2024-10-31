package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.AnaloguePowerable;
import org.bukkit.block.data.BlockData;

public class MaterialPower extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name power
    // @input ElementTag(Number)
    // @description
    // Controls the redstone power level of an analogue-powerable block.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof AnaloguePowerable;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "power";
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(((AnaloguePowerable) material.getModernData()).getPower());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            int power = mechanism.getValue().asInt();
            AnaloguePowerable powerable = (AnaloguePowerable) material.getModernData();
            if (power < 0 || power > powerable.getMaximumPower()) {
                mechanism.echoError("Material power mechanism value '" + power + "' is not valid. Must be between 0 and " + powerable.getMaximumPower() + ".");
                return;
            }
            powerable.setPower(power);
        }
    }

    public static void register() {
        autoRegister("power", MaterialPower.class, ElementTag.class, true);

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
}
