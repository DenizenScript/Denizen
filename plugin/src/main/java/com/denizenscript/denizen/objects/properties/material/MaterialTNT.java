package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.TNT;

public class MaterialTNT implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof TNT;
    }

    public static MaterialTNT getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialTNT((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "is_unstable"
    };

    private MaterialTNT(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.is_unstable>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.is_unstable
        // @group properties
        // @description
        // Returns whether this TNT will explode when punched, or not.
        // -->
        PropertyParser.<MaterialTNT>registerTag("is_unstable", (attribute, material) -> {
            return new ElementTag(material.getTNT().isUnstable());
        });
    }

    public TNT getTNT() {
        return (TNT) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getTNT().isUnstable());
    }

    @Override
    public String getPropertyId() {
        return "is_unstable";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name is_unstable
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this TNT will explode when punched, or not.
        // @tags
        // <MaterialTag.is_unstable>
        // -->
        if (mechanism.matches("is_unstable") && mechanism.requireBoolean()) {
            getTNT().setUnstable(mechanism.getValue().asBoolean());
        }
    }
}

