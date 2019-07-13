package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizencore.objects.Element;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.dObject;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.data.Bisected;

public class MaterialHalf implements Property {

    public static boolean describes(dObject material) {
        return material instanceof dMaterial
                && ((dMaterial) material).hasModernData()
                && ((dMaterial) material).getModernData().data instanceof Bisected;
    }

    public static MaterialHalf getFrom(dObject _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialHalf((dMaterial) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "half"
    };

    public static final String[] handledMechs = new String[] {
            "half"
    };


    private MaterialHalf(dMaterial _material) {
        material = _material;
    }

    dMaterial material;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <m@material.half>
        // @returns Element
        // @mechanism dMaterial.half
        // @group properties
        // @description
        // Returns the current half for a bisected material (like stairs).
        // Output is "BOTTOM" or "TOP".
        // -->
        if (attribute.startsWith("half")) {
            return new Element(getBisected().getHalf().name()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public Bisected getBisected() {
        return (Bisected) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getBisected().getHalf());
    }

    @Override
    public String getPropertyId() {
        return "half";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dMaterial
        // @name half
        // @input Element
        // @description
        // Sets the current half for a bisected material (like stairs).
        // @tags
        // <m@material.half>
        // -->
        if (mechanism.matches("half") && mechanism.requireEnum(false, Bisected.Half.values())) {
            getBisected().setHalf(Bisected.Half.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
