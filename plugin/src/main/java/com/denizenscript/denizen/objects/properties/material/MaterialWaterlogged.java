package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Waterlogged;

public class MaterialWaterlogged implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Waterlogged;
    }

    public static MaterialWaterlogged getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialWaterlogged((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "waterlogged"
    };

    public MaterialWaterlogged(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.waterlogged>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.waterlogged
        // @group properties
        // @description
        // Returns whether this block is waterlogged or not.
        // -->
        PropertyParser.registerStaticTag(MaterialWaterlogged.class, ElementTag.class, "waterlogged", (attribute, material) -> {
            return new ElementTag(material.isWaterlogged());
        });
    }

    public Waterlogged getWaterlogged() {
        return (Waterlogged) material.getModernData();
    }

    public boolean isWaterlogged() {
        return getWaterlogged().isWaterlogged();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(isWaterlogged());
    }

    @Override
    public String getPropertyId() {
        return "waterlogged";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name waterlogged
        // @input ElementTag(Boolean)
        // @description
        // Sets this block to be waterlogged, or not.
        // @tags
        // <MaterialTag.waterlogged>
        // -->
        if (mechanism.matches("waterlogged") && mechanism.requireBoolean()) {
            getWaterlogged().setWaterlogged(mechanism.getValue().asBoolean());
        }
    }
}
