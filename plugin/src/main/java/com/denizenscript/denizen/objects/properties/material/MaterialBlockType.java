package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.exceptions.Unreachable;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

public class MaterialBlockType extends MaterialProperty {

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
    public String getPropertyId() {
        return "type";
    }

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.type>
        // @returns ElementTag
        // @mechanism MaterialTag.type
        // @group properties
        // @description
        // Returns the current type of the block.
        // For slabs, output is TOP, BOTTOM, or DOUBLE.
        // For piston_heads, output is NORMAL or STICKY.
        // For campfires, output is NORMAL or SIGNAL.
        // For pointed dripstone, output is BASE, FRUSTUM, MIDDLE, TIP, or TIP_MERGE.
        // For cave vines, output is NORMAL or BERRIES.
        // For scaffolding, output is NORMAL or BOTTOM.
        // -->
        PropertyParser.registerStaticTag(MaterialBlockType.class, ElementTag.class, "type", (attribute, prop) -> {
            return new ElementTag(prop.getPropertyString());
        }, "slab_type");

        // <--[mechanism]
        // @object MaterialTag
        // @name type
        // @input ElementTag
        // @description
        // Sets the current type of the block.
        // For slabs, input is TOP, BOTTOM, or DOUBLE.
        // For piston_heads, input is NORMAL or STICKY.
        // For campfires, input is NORMAL or SIGNAL.
        // For pointed dripstone, input is BASE, FRUSTUM, MIDDLE, TIP, or TIP_MERGE.
        // For cave vines, input is NORMAL or BERRIES.
        // For scaffolding, input is NORMAL or BOTTOM.
        // @tags
        // <MaterialTag.type>
        // -->
        PropertyParser.registerMechanism(MaterialBlockType.class, ElementTag.class, "type", (prop, mechanism, param) -> {
            BlockData data = prop.getBlockData();
            if (data instanceof Slab slab && mechanism.requireEnum(Slab.Type.class)) {
                slab.setType(mechanism.value.asElement().asEnum(Slab.Type.class));
            }
            else if (data instanceof TechnicalPiston piston && mechanism.requireEnum(TechnicalPiston.Type.class)) {
                piston.setType(mechanism.value.asElement().asEnum(TechnicalPiston.Type.class));
            }
            else if (data instanceof Campfire campfire) {
                campfire.setSignalFire(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "signal"));
            }
            else if (data instanceof PointedDripstone dripstone && mechanism.requireEnum(PointedDripstone.Thickness.class)) {
                dripstone.setThickness(mechanism.value.asElement().asEnum(PointedDripstone.Thickness.class));
            }
            else if (data instanceof CaveVinesPlant vines) {
                vines.setBerries(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "berries"));
            }
            else if (data instanceof Scaffolding scaffolding) {
                scaffolding.setBottom(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "bottom"));
            }
        });
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
