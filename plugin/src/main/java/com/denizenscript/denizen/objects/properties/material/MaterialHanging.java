package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Lantern;

public class MaterialHanging implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Lantern;
    }

    public static MaterialHanging getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialHanging((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "hanging"
    };

    private MaterialHanging(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.is_hanging>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.is_hanging
        // @group properties
        // @description
        // Returns whether this lantern is hanging, or not.
        // -->
        PropertyParser.<MaterialHanging>registerTag("hanging", (attribute, material) -> {
            return new ElementTag(material.getLantern().isHanging());
        });
    }

    public Lantern getLantern() {
        return (Lantern) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getLantern().isHanging());
    }

    @Override
    public String getPropertyId() {
        return "hanging";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name is_hanging
        // @input ElementTag(Boolean)
        // @description
        // Sets a lantern block to be hanging, or not.
        // @tags
        // <MaterialTag.is_hanging>
        // -->
        if (mechanism.matches("hanging") && mechanism.requireBoolean()) {
            getLantern().setHanging(mechanism.getValue().asBoolean());
        }
    }
}
