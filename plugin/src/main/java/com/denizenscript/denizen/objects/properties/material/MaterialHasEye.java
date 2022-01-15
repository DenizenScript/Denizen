package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.EndPortalFrame;

public class MaterialHasEye implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof EndPortalFrame;
    }

    public static MaterialHasEye getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialHasEye((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "has_eye"
    };

    private MaterialHasEye(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.has_eye>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.has_eye
        // @group properties
        // @description
        // Returns whether this end portal frame has an eye.
        // -->
        PropertyParser.<MaterialHasEye, ElementTag>registerStaticTag(ElementTag.class, "has_eye",(attribute, material) -> {
            return new ElementTag(material.hasEye());
        });
    }

    public EndPortalFrame getFrame() {
        return (EndPortalFrame) material.getModernData();
    }

    public boolean hasEye() {
        return getFrame().hasEye();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(hasEye());
    }

    @Override
    public String getPropertyId() {
        return "has_eye";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name has_eye
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this end portal frame has an eye.
        // @tags
        // <MaterialTag.has_eye>
        // -->
        if (mechanism.matches("has_eye") && mechanism.requireBoolean()) {
            getFrame().setEye(mechanism.getValue().asBoolean());
        }
    }
}
