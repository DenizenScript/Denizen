package net.aufdemrand.denizen.objects.properties.material;

import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.Mechanism;
import net.aufdemrand.denizencore.objects.dObject;
import net.aufdemrand.denizencore.objects.properties.Property;
import net.aufdemrand.denizencore.tags.Attribute;
import org.bukkit.block.data.Ageable;

public class MaterialPlantGrowth implements Property {

    public static boolean describes(dObject material) {
        return material instanceof dMaterial
                && ((dMaterial) material).hasModernData()
                && ((dMaterial) material).getModernData().data instanceof Ageable;
    }

    public static MaterialPlantGrowth getFrom(dObject _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialPlantGrowth((dMaterial) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "maximum_plant_growth", "plant_growth"
    };

    public static final String[] handledMechs = new String[] {
            "plant_growth"
    };


    private MaterialPlantGrowth(dMaterial _material) {
        material = _material;
    }

    dMaterial material;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <m@material.maximum_plant_growth>
        // @returns Element(Number)
        // @group properties
        // @description
        // Returns the maximum plant growth stage for a plant material.
        // -->
        if (attribute.startsWith("maximum_plant_growth")) {
            return new Element(getMax()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <m@material.plant_growth>
        // @returns Element(Number)
        // @mechanism dMaterial.plant_growth
        // @group properties
        // @description
        // Returns the current plant growth stage for a plant material.
        // -->
        if (attribute.startsWith("plant_growth")) {
            return new Element(((Ageable) material.getModernData().data).getAge())
                    .getAttribute(attribute.fulfill(1));
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
        return "plant_growth";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dMaterial
        // @name plant_growth
        // @input Element(Number)
        // @description
        // Sets a plant material's current growth level.
        // @tags
        // <m@material.plant_growth>
        // <m@material.maximum_plant_growth>
        // -->
        if (mechanism.matches("plant_growth") && mechanism.requireInteger()) {
            int growth = mechanism.getValue().asInt();
            if (growth < 0 || growth > getMax()) {
                dB.echoError("Growth value '" + growth + "' is not valid. Must be between 0 and " + getAgeable() + " for material '" + material.realName() + "'.");
                return;
            }
            getAgeable().setAge(growth);
        }
    }
}
