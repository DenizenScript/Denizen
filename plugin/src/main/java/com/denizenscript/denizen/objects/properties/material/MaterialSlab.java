package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Slab;

public class MaterialSlab implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Slab;
    }

    public static MaterialSlab getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialSlab((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "slab_type"
    };


    private MaterialSlab(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.slab_type>
        // @returns ElementTag
        // @mechanism MaterialTag.slab_type
        // @group properties
        // @description
        // Returns the current type for a slab.
        // Output is "BOTTOM", "TOP", or "DOUBLE".
        // -->
        PropertyParser.<MaterialSlab>registerTag("slab_type", (attribute, material) -> {
            return new ElementTag(material.getSlab().getType().name());
        });
    }

    public Slab getSlab() {
        return (Slab) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getSlab().getType());
    }

    @Override
    public String getPropertyId() {
        return "slab_type";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name slab_type
        // @input Element
        // @description
        // Sets the current type of the slab.
        // @tags
        // <MaterialTag.slab_type>
        // -->
        if (mechanism.matches("slab_type") && mechanism.requireEnum(false, Slab.Type.values())) {
            getSlab().setType(Slab.Type.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
