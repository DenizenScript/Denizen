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

    public MaterialDistance(MaterialTag material) {
        super(material);
    }

    @Override
    public String getPropertyId() {
        return "distance";
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getBlockData() instanceof Scaffolding scaffolding) {
            return new ElementTag(scaffolding.getDistance());
        }
        else if (getBlockData() instanceof Leaves leaves) {
            return new ElementTag(leaves.getDistance());
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireInteger()) {
            return;
        }
        int distance = value.asInt();
        if (getBlockData() instanceof Scaffolding scaffolding) {
            if (distance >= 0 && distance <= scaffolding.getMaximumDistance()) {
                scaffolding.setDistance(distance);
            }
            else {
                mechanism.echoError("Distance must be between 0 and " + scaffolding.getMaximumDistance());
            }
        }
        else if (getBlockData() instanceof Leaves leaves) {
            leaves.setDistance(distance);
        }
    }

    public static void register() {
        autoRegister("distance", MaterialDistance.class, ElementTag.class, true);
    }
}
