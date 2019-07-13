package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.data.Ageable;

public class MaterialAge implements Property {

    public static boolean describes(dObject material) {
        return material instanceof dMaterial
                && ((dMaterial) material).hasModernData()
                && ((dMaterial) material).getModernData().data instanceof Ageable;
    }

    public static MaterialAge getFrom(dObject _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialAge((dMaterial) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "maximum_age", "age", "maximum_plant_growth", "plant_growth"
    };

    public static final String[] handledMechs = new String[] {
            "age", "plant_growth"
    };


    private MaterialAge(dMaterial _material) {
        material = _material;
    }

    dMaterial material;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <m@material.maximum_age>
        // @returns Element(Number)
        // @group properties
        // @description
        // Returns the maximum age for an ageable material.
        // -->
        if (attribute.startsWith("maximum_age") || attribute.startsWith("maximum_plant_growth")) {
            return new Element(getMax()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <m@material.age>
        // @returns Element(Number)
        // @mechanism dMaterial.age
        // @group properties
        // @description
        // Returns the current age for an ageable material.
        // -->
        if (attribute.startsWith("age") || attribute.startsWith("plant_growth")) {
            return new Element(getCurrent()).getAttribute(attribute.fulfill(1));
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
        // @object dMaterial
        // @name age
        // @input Element(Number)
        // @description
        // Sets an ageable material's current age.
        // @tags
        // <m@material.age>
        // <m@material.maximum_age>
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
