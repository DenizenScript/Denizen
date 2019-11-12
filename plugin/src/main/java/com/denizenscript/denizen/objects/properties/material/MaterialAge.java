package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.data.Ageable;

public class MaterialAge implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Ageable;
    }

    public static MaterialAge getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialAge((MaterialTag) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "maximum_age", "age", "maximum_plant_growth", "plant_growth"
    };

    public static final String[] handledMechs = new String[] {
            "age", "plant_growth"
    };


    private MaterialAge(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <MaterialTag.maximum_age>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum age for an ageable material. This includes plant growth.
        // -->
        if (attribute.startsWith("maximum_age") || attribute.startsWith("maximum_plant_growth")) {
            return new ElementTag(getMax()).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <MaterialTag.age>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.age
        // @group properties
        // @description
        // Returns the current age for an ageable material. This includes plant growth.
        // -->
        if (attribute.startsWith("age") || attribute.startsWith("plant_growth")) {
            return new ElementTag(getCurrent()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public Ageable getAgeable() {
        return (Ageable) material.getModernData().data;
    }

    public int getCurrent() {
        return getAgeable().getAge();
    }

    public int getMax() {
        return getAgeable().getMaximumAge();
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
                Debug.echoError("Age value '" + age + "' is not valid. Must be between 0 and " + getMax() + " for material '" + material.realName() + "'.");
                return;
            }
            getAgeable().setAge(age);
        }
    }
}
