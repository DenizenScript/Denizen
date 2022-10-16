package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Door;

public class MaterialHinge implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Door;
    }

    public static MaterialHinge getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialHinge((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "hinge"
    };

    private MaterialHinge(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.hinge>
        // @returns ElementTag
        // @mechanism MaterialTag.hinge
        // @group properties
        // @description
        // Returns a door's hinge side.
        // Output is LEFT or RIGHT.
        // -->
        PropertyParser.registerStaticTag(MaterialHinge.class, ElementTag.class, "hinge", (attribute, material) -> {
            return new ElementTag(material.getDoor().getHinge());
        });
    }

    public Door getDoor() {
        return (Door) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        return getDoor().getHinge().name();
    }

    @Override
    public String getPropertyId() {
        return "hinge";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name hinge
        // @input ElementTag
        // @description
        // Sets a door's hinge side to LEFT or RIGHT.
        // @tags
        // <MaterialTag.hinge>
        // -->
        if (mechanism.matches("hinge") && mechanism.requireEnum(Door.Hinge.class)) {
            getDoor().setHinge(Door.Hinge.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
