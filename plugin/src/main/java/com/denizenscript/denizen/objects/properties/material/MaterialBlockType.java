package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

public class MaterialBlockType implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag)) {
            return false;
        }
        MaterialTag mat = (MaterialTag) material;
        if (!mat.hasModernData()) {
            return false;
        }
        BlockData data = mat.getModernData();
        return data instanceof Slab
                || data instanceof TechnicalPiston
                || data instanceof Campfire
                || data instanceof Scaffolding
                || data instanceof PointedDripstone
                || data instanceof CaveVinesPlant;
    }

    public static MaterialBlockType getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialBlockType((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "type", "slab_type"
    };

    private MaterialBlockType(MaterialTag _material) {
        material = _material;
    }

    public MaterialTag material;

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
        PropertyParser.registerStaticTag(MaterialBlockType.class, ElementTag.class, "type", (attribute, material) -> {
            return new ElementTag(material.getPropertyString());
        }, "slab_type");
    }

    public boolean isSlab() {
        return material.getModernData() instanceof Slab;
    }

    public boolean isPistonHead() {
        return material.getModernData() instanceof TechnicalPiston;
    }

    public boolean isCampfire() {
        return material.getModernData() instanceof Campfire;
    }

    public boolean isDripstone() {
        return material.getModernData() instanceof PointedDripstone;
    }

    public boolean isCaveVines() {
        return material.getModernData() instanceof CaveVinesPlant;
    }

    public boolean isScaffolding() {
        return material.getModernData() instanceof Scaffolding;
    }

    public Slab getSlab() {
        return (Slab) material.getModernData();
    }

    public TechnicalPiston getPistonHead() {
        return (TechnicalPiston) material.getModernData();
    }

    public Campfire getCampfire() {
        return (Campfire) material.getModernData();
    }

    public Scaffolding getScaffolding() {
        return (Scaffolding) material.getModernData();
    }

    public PointedDripstone getDripstone() {
        return (PointedDripstone) material.getModernData();
    }

    public CaveVinesPlant getCaveVines() {
        return (CaveVinesPlant) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        if (isSlab()) {
            return getSlab().getType().name();
        }
        else if (isCampfire()) {
            return getCampfire().isSignalFire() ? "SIGNAL" : "NORMAL";
        }
        else if (isPistonHead()) {
            return getPistonHead().getType().name();
        }
        else if (isScaffolding()) {
            return getScaffolding().isBottom() ? "BOTTOM" : "NORMAL";
        }
        else if (isDripstone()) {
            return getDripstone().getThickness().name();
        }
        else if (isCaveVines()) {
            return getCaveVines().isBerries() ? "BERRIES" : "NORMAL";
        }
        return null; // Unreachable.
    }

    @Override
    public String getPropertyId() {
        return "type";
    }

    @Override
    public void adjust(Mechanism mechanism) {

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
        if (mechanism.matches("type") || (mechanism.matches("slab_type"))) {
            if (isSlab() && mechanism.requireEnum(Slab.Type.class)) {
                getSlab().setType(Slab.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isCampfire()) {
                getCampfire().setSignalFire(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "signal"));
            }
            else if (isPistonHead() && mechanism.requireEnum(TechnicalPiston.Type.class)) {
                getPistonHead().setType(TechnicalPiston.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isScaffolding()) {
                getScaffolding().setBottom(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "bottom"));
            }
            else if (isDripstone() && mechanism.requireEnum(PointedDripstone.Thickness.class)) {
                ((PointedDripstone) material.getModernData()).setThickness(PointedDripstone.Thickness.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isCaveVines()) {
                getCaveVines().setBerries(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "berries"));
            }
        }
    }
}
