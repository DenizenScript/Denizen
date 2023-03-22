package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.Scaffolding;

public class MaterialDistance implements Property {

    public static boolean describes(Object material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData() instanceof Scaffolding
                || ((MaterialTag) material).getModernData() instanceof Leaves);
    }

    public static MaterialDistance getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDistance((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "distance"
    };

    public MaterialDistance(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.distance>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.distance
        // @group properties
        // @description
        // Returns the horizontal distance between a scaffolding block and the nearest scaffolding block placed above a 'bottom' scaffold,
        // or between a leaves block and the nearest log (a distance of 7 will cause a leaf to decay if 'persistent' is also false, less than 7 will prevent decay).
        // -->
        PropertyParser.registerStaticTag(MaterialDistance.class, ElementTag.class, "distance", (attribute, material) -> {
            return new ElementTag(material.getDistance());
        });
    }

    public int getDistance() {
        if (isScaffolding()) {
            return getScaffolding().getDistance();
        }
        else if (isLeaves()) {
            return getLeaves().getDistance();
        }
        throw new UnsupportedOperationException();
    }

    public Scaffolding getScaffolding() {
        return (Scaffolding) material.getModernData();
    }

    public Leaves getLeaves() {
        return (Leaves) material.getModernData();
    }

    public boolean isScaffolding() {
        return material.getModernData() instanceof Scaffolding;
    }

    public boolean isLeaves() {
        return material.getModernData() instanceof Leaves;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getDistance());
    }

    @Override
    public String getPropertyId() {
        return "distance";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name distance
        // @input ElementTag(Number)
        // @description
        // Sets the horizontal distance between a scaffolding block and the nearest scaffolding block placed above a 'bottom' scaffold, or between a leaves block and the nearest log.
        // @tags
        // <MaterialTag.distance>
        // -->
        if (mechanism.matches("distance") && mechanism.requireInteger()) {
            int distance = mechanism.getValue().asInt();
            if (isScaffolding()) {
                if (distance >= 0 && distance <= getScaffolding().getMaximumDistance()) {
                    getScaffolding().setDistance(distance);
                }
                else {
                    mechanism.echoError("Distance must be between 0 and " + getScaffolding().getMaximumDistance());
                }
            }
            else if (isLeaves()) {
                getLeaves().setDistance(distance);
            }
        }
    }
}
