package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.Unreachable;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

public class MaterialBlockType extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name type
    // @input ElementTag
    // @description
    // Controls the current type of the block.
    // For slabs, input is TOP, BOTTOM, or DOUBLE.
    // For piston_heads, input is NORMAL or STICKY.
    // For campfires, input is NORMAL or SIGNAL.
    // For pointed dripstone, input is BASE, FRUSTUM, MIDDLE, TIP, or TIP_MERGE.
    // For cave vines, input is NORMAL or BERRIES.
    // For scaffolding, input is NORMAL or BOTTOM.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Slab || data instanceof TechnicalPiston || data instanceof Campfire
                || data instanceof Scaffolding || data instanceof PointedDripstone || data instanceof CaveVinesPlant;
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getType());
    }

    @Override
    public void setPropertyValue(ElementTag val, Mechanism mechanism) {
        BlockData data = getBlockData();
        if (data instanceof Slab slab && mechanism.requireEnum(Slab.Type.class)) {
            slab.setType(val.asEnum(Slab.Type.class));
        }
        else if (data instanceof TechnicalPiston piston && mechanism.requireEnum(TechnicalPiston.Type.class)) {
            piston.setType(val.asEnum(TechnicalPiston.Type.class));
        }
        else if (data instanceof Campfire campfire) {
            campfire.setSignalFire(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "signal"));
        }
        else if (data instanceof PointedDripstone dripstone && mechanism.requireEnum(PointedDripstone.Thickness.class)) {
            dripstone.setThickness(val.asEnum(PointedDripstone.Thickness.class));
        }
        else if (data instanceof CaveVinesPlant vines) {
            vines.setBerries(CoreUtilities.equalsIgnoreCase(val.asString(), "berries"));
        }
        else if (data instanceof Scaffolding scaffolding) {
            scaffolding.setBottom(CoreUtilities.equalsIgnoreCase(val.asString(), "bottom"));
        }
    }

    @Override
    public String getPropertyId() {
        return "type";
    }

    public static void register() {
        autoRegister("type", MaterialBlockType.class, ElementTag.class, true, "slab_type");
    }

    public String getType() {
        BlockData data = getBlockData();
        if (data instanceof Slab slab) {
            return slab.getType().name();
        }
        else if (data instanceof TechnicalPiston piston) {
            return piston.getType().name();
        }
        else if (data instanceof Campfire campfire) {
            return campfire.isSignalFire()  ? "SIGNAL" : "NORMAL";
        }
        else if (data instanceof PointedDripstone dripstone) {
            return dripstone.getThickness().name();
        }
        else if (data instanceof CaveVinesPlant vines) {
            return vines.isBerries() ? "BERRIES" : "NORMAL";
        }
        else if (data instanceof Scaffolding scaffolding) {
            return scaffolding.isBottom() ? "BOTTOM" : "NORMAL";
        }
        throw new Unreachable();
    }
}
