package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Slab;
import org.bukkit.block.data.type.TechnicalPiston;

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
                || data instanceof TechnicalPiston;
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

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.type>
        // @returns ElementTag
        // @mechanism MaterialTag.type
        // @group properties
        // @description
        // Returns the current type of the block.
        // For slabs, input is TOP, BOTTOM, or DOUBLE.
        // For piston_heads, input is NORMAL or STICKY.
        // -->
        PropertyParser.<MaterialBlockType>registerTag("type", (attribute, material) -> {
            return new ElementTag(material.getSlab().getType().name());
        }, "slab_type");
    }

    public boolean isSlab() {
        return material.getModernData() instanceof Slab;
    }

    public boolean isPistonHead() {
        return material.getModernData() instanceof TechnicalPiston;
    }

    public Slab getSlab() {
        return (Slab) material.getModernData();
    }

    public TechnicalPiston getPistonHead() {
        return (TechnicalPiston) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        if (isSlab()) {
            return String.valueOf(getSlab().getType());
        }
        else {
            return String.valueOf(getPistonHead().getType());
        }
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
        // @tags
        // <MaterialTag.type>
        // -->
        if (mechanism.matches("type") || (mechanism.matches("slab_type"))) {
            if (isSlab() && mechanism.requireEnum(false, Slab.Type.values())) {
                getSlab().setType(Slab.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isPistonHead() && mechanism.requireEnum(false, TechnicalPiston.Type.values())) {
                getPistonHead().setType(TechnicalPiston.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
        }
    }
}
