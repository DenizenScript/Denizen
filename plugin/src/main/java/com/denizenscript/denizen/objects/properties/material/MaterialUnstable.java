package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.TNT;

public class MaterialUnstable implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof TNT;
    }

    public static MaterialUnstable getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialUnstable((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "unstable"
    };

    public MaterialUnstable(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.unstable>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.unstable
        // @group properties
        // @description
        // Returns whether this TNT block is unstable (explodes when punched).
        // -->
        PropertyParser.registerStaticTag(MaterialUnstable.class, ElementTag.class, "unstable", (attribute, material) -> {
            return new ElementTag(material.isUnstable());
        });
    }

    public TNT getTNT() {
        return (TNT) material.getModernData();
    }

    public boolean isUnstable() {
        return getTNT().isUnstable();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(isUnstable());
    }

    @Override
    public String getPropertyId() {
        return "unstable";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name unstable
        // @input ElementTag(Boolean)
        // @description
        // Sets whether this TNT block is unstable (explodes when punched).
        // @tags
        // <MaterialTag.unstable>
        // -->
        if (mechanism.matches("unstable") && mechanism.requireBoolean()) {
            getTNT().setUnstable(mechanism.getValue().asBoolean());
        }
    }
}
