package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Bamboo;

public class MaterialLeafSize implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Bamboo;
    }

    public static MaterialLeafSize getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialLeafSize((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "leaf_size"
    };

    private MaterialLeafSize(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.leaf_size>
        // @returns ElementTag
        // @mechanism MaterialTag.leaf_size
        // @group properties
        // @description
        // Returns the size of the leaves for this bamboo block.
        // Output is SMALL, LARGE, or NONE.
        // -->
        PropertyParser.registerStaticTag(MaterialLeafSize.class, ElementTag.class, "leaf_size", (attribute, material) -> {
            return new ElementTag(material.getBamboo().getLeaves());
        });
    }

    public Bamboo getBamboo() {
        return (Bamboo) material.getModernData();
    }

    public void setLeafSize(String size) {
        getBamboo().setLeaves(Bamboo.Leaves.valueOf(size));
    }

    @Override
    public String getPropertyString() {
        return getBamboo().getLeaves().name();
    }

    @Override
    public String getPropertyId() {
        return "leaf_size";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name leaf_size
        // @input ElementTag
        // @description
        // Sets the size of the leaves for this bamboo block.
        // Valid input is SMALL, LARGE, or NONE.
        // @tags
        // <MaterialTag.leaf_size>
        // -->
        if (mechanism.matches("leaf_size") && mechanism.requireEnum(Bamboo.Leaves.class)) {
            setLeafSize(mechanism.getValue().asString().toUpperCase());
        }
    }
}
