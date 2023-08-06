package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Snowable;

public class MaterialSnowable implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Snowable;
    }

    public static MaterialSnowable getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialSnowable((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "snowy"
    };

    public MaterialSnowable(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.snowy>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.snowy
        // @group properties
        // @description
        // Returns whether this material is covered in snow or not.
        // -->
        PropertyParser.registerStaticTag(MaterialSnowable.class, ElementTag.class, "snowy", (attribute, material) -> {
            return new ElementTag(material.isSnowy());
        });
    }

    public Snowable getSnowable() {
        return (Snowable) material.getModernData();
    }

    public boolean isSnowy() {
        return getSnowable().isSnowy();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(isSnowy());
    }

    @Override
    public String getPropertyId() {
        return "snowy";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name snowy
        // @input ElementTag(Boolean)
        // @description
        // Sets this material to be covered in snow, or not.
        // @tags
        // <MaterialTag.snowy>
        // -->
        if (mechanism.matches("snowy") && mechanism.requireBoolean()) {
            getSnowable().setSnowy(mechanism.getValue().asBoolean());
        }
    }
}
