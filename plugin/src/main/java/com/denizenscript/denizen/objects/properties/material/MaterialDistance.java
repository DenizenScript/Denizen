package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Leaves;
import org.bukkit.block.data.type.Scaffolding;

public class MaterialDistance extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name distance
    // @input ElementTag(Number)
    // @description
    // Controls the horizontal distance between a scaffolding block and the nearest scaffolding block placed above a 'bottom' scaffold,
    // or between a leaves block and the nearest log (a distance of 7 will cause a leaf to decay if 'persistent' is also false, less than 7 will prevent decay).
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Scaffolding
                || data instanceof Leaves;
    }

    MaterialTag material;

    @Override
    public String getPropertyId() {
        return "distance";
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getDistance());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireInteger()) {
            return;
        }
        int distance = value.asInt();
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

    public static void register() {
        autoRegister("distance", MaterialDistance.class, ElementTag.class, true);
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
}
