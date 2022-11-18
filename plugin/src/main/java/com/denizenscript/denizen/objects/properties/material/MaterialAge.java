package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.type.Sapling;
import org.bukkit.block.data.type.TurtleEgg;

public class MaterialAge implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData() instanceof Ageable
                || ((MaterialTag) material).getModernData() instanceof TurtleEgg
                || ((MaterialTag) material).getModernData() instanceof Sapling);
    }

    public static MaterialAge getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialAge((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "age", "plant_growth"
    };

    private MaterialAge(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.maximum_age>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum age for an ageable material. This includes plant growth.
        // -->
        PropertyParser.registerStaticTag(MaterialAge.class, ElementTag.class, "maximum_age", (attribute, material) -> {
            return new ElementTag(material.getMax());
        }, "maximum_plant_growth");

        // <--[tag]
        // @attribute <MaterialTag.age>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.age
        // @group properties
        // @description
        // Returns the current age for an ageable material. This includes plant growth.
        // -->
        PropertyParser.registerStaticTag(MaterialAge.class, ElementTag.class, "age", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        }, "plant_growth");
    }

    public TurtleEgg getTurtleEgg() {
        return (TurtleEgg) material.getModernData();
    }

    public boolean isTurtleEgg() {
        return material.getModernData() instanceof TurtleEgg;
    }

    public Sapling getSapling() {
        return (Sapling) material.getModernData();
    }

    public boolean isSapling() {
        return material.getModernData() instanceof Sapling;
    }

    public Ageable getAgeable() {
        return (Ageable) material.getModernData();
    }

    public int getCurrent() {
        if (isTurtleEgg()) {
            return getTurtleEgg().getHatch();
        }
        else if (isSapling()) {
            return getSapling().getStage();
        }
        else {
            return getAgeable().getAge();
        }
    }

    public int getMax() {
        if (isTurtleEgg()) {
            return getTurtleEgg().getMaximumHatch();
        }
        else if (isSapling()) {
            return getSapling().getMaximumStage();
        }
        else {
            return getAgeable().getMaximumAge();
        }
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "age";
    }

    @Override
    public void adjust(Mechanism mechanism) {

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
        if ((mechanism.matches("age") || mechanism.matches("plant_growth")) && mechanism.requireInteger()) {
            int age = mechanism.getValue().asInt();
            if (age < 0 || age > getMax()) {
                mechanism.echoError("Age value '" + age + "' is not valid. Must be between 0 and " + getMax() + " for material '" + material.name() + "'.");
                return;
            }
            if (isTurtleEgg()) {
                getTurtleEgg().setHatch(age);
            }
            else if (isSapling()) {
                getSapling().setStage(age);
            }
            else {
                getAgeable().setAge(age);
            }
        }
    }
}
