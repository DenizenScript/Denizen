package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.Unreachable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Hatchable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.block.data.type.TurtleEgg;

public class MaterialAge extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name age
    // @input ElementTag(Number)
    // @description
    // Controls an ageable material's current age. This includes plant growth.
    // See also <@link tag MaterialTag.maximum_age>.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Ageable
                || data instanceof TurtleEgg
                || data instanceof Sapling
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && data instanceof Hatchable);
    }

    public MaterialAge(MaterialTag material) { // NOTE: BlockGrowsScriptEvent needs this available
        super(material);
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getCurrent());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        if (!mechanism.requireInteger()) {
            return;
        }
        int age = val.asInt();
        if (age < 0 || age > getMax()) {
            mechanism.echoError("Age value '" + age + "' is not valid. Must be between 0 and " + getMax() + " for material '" + object.name() + "'.");
            return;
        }
        BlockData data = getBlockData();
        if (data instanceof TurtleEgg turtle) {
            turtle.setHatch(age);
        }
        else if (data instanceof Sapling sapling) {
            sapling.setStage(age);
        }
        else if (data instanceof Ageable ageable) {
            ageable.setAge(age);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && data instanceof Hatchable hatchable) {
            hatchable.setHatch(age);
        }
    }

    @Override
    public String getPropertyId() {
        return "age";
    }

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.maximum_age>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum age for an ageable material. This includes plant growth.
        // -->
        PropertyParser.registerStaticTag(MaterialAge.class, ElementTag.class, "maximum_age", (attribute, prop) -> {
            return new ElementTag(prop.getMax());
        }, "maximum_plant_growth");

        autoRegister("age", MaterialAge.class, ElementTag.class, true, "plant_growth");
    }

    public int getCurrent() {
        BlockData data = getBlockData();
        if (data instanceof TurtleEgg turtle) {
            return turtle.getHatch();
        }
        else if (data instanceof Sapling sapling) {
            return sapling.getStage();
        }
        else if (data instanceof Ageable age) {
            return age.getAge();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && data instanceof Hatchable hatchable) {
            return hatchable.getHatch();
        }
        throw new Unreachable();
    }

    public int getMax() {
        BlockData data = getBlockData();
        if (data instanceof TurtleEgg turtle) {
            return turtle.getMaximumHatch();
        }
        else if (data instanceof Sapling sapling) {
            return sapling.getMaximumStage();
        }
        else if (data instanceof Ageable age) {
            return age.getMaximumAge();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && data instanceof Hatchable hatchable) {
            return hatchable.getMaximumHatch();
        }
        throw new Unreachable();
    }
}
