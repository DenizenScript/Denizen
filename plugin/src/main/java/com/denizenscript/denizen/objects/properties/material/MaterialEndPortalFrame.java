package com.denizenscript.denizen.objects.properties.material;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.EndPortalFrame;

public class MaterialEndPortalFrame implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof EndPortalFrame;
    }

    public static MaterialEndPortalFrame getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialEndPortalFrame((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "has_eye"
    };

    private MaterialEndPortalFrame(MaterialTag _material) {
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
        PropertyParser.<MaterialEndPortalFrame>registerTag("has_eye", (attribute, material) -> {
            return new ElementTag(material.getEndPortalFrame().hasEye());
        });
    }

    public EndPortalFrame getEndPortalFrame() {
        return (EndPortalFrame) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getEndPortalFrame().hasEye());
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
        // Sets a end portal frame block to have an eye, or not.
        // @tags
        // <MaterialTag.has_eye>
        // -->
        if (mechanism.matches("has_eye") && mechanism.requireBoolean()) {
            getEndPortalFrame().setEye(mechanism.getValue().asBoolean());
        }
    }
}

