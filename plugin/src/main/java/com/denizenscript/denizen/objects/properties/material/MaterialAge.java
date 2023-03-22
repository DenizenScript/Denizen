package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.Unreachable;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.block.data.type.TurtleEgg;

public class MaterialAge extends MaterialProperty {

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Ageable || data instanceof TurtleEgg || data instanceof Sapling;
    }

    public MaterialAge(MaterialTag material) { // NOTE: BlockGrowsScriptEvent needs this available
        super(material);
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getCurrent());
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

        // <--[tag]
        // @attribute <MaterialTag.age>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.age
        // @group properties
        // @description
        // Returns the current age for an ageable material. This includes plant growth.
        // -->
        PropertyParser.registerStaticTag(MaterialAge.class, ElementTag.class, "age", (attribute, prop) -> {
            return new ElementTag(prop.getCurrent());
        }, "plant_growth");

        // <--[mechanism]
        // @object MaterialTag
        // @name age
        // @input ElementTag(Number)
        // @description
        // Sets an ageable material's current age. This includes plant growth.
        // @tags
        // <MaterialTag.age>
        // <MaterialTag.maximum_age>
        // -->
        PropertyParser.registerMechanism(MaterialAge.class, ElementTag.class, "age", (prop, mechanism, param) -> {
            if (!mechanism.requireInteger()) {
                return;
            }
            int age = param.asInt();
            if (age < 0 || age > prop.getMax()) {
                mechanism.echoError("Age value '" + age + "' is not valid. Must be between 0 and " + prop.getMax() + " for material '" + prop.object.name() + "'.");
                return;
            }
            BlockData data = prop.getBlockData();
            if (data instanceof TurtleEgg turtle) {
                turtle.setHatch(age);
            }
            else if (data instanceof Sapling sapling) {
                sapling.setStage(age);
            }
            else if (data instanceof Ageable ageable) {
                ageable.setAge(age);
            }
        }, "plant_growth");
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
        throw new Unreachable();
    }
}
