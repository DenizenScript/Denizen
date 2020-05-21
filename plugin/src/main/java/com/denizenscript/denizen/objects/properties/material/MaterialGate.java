package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Gate;

public class MaterialGate implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Gate;
    }

    public static MaterialGate getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialGate((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "is_in_wall"
    };

    private MaterialGate(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.signal_fire>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.signal_fire
        // @group properties
        // @description
        // Returns whether this campfire will produce longer smoke trails, or not.
        // -->
        PropertyParser.<MaterialGate>registerTag("is_in_wall", (attribute, material) -> {
            return new ElementTag(material.getGate().isInWall());
        });
    }

    public Gate getGate() {
        return (Gate) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getGate().isInWall());
    }

    @Override
    public String getPropertyId() {
        return "is_in_wall";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name signal_fire
        // @input ElementTag(Boolean)
        // @description
        // Sets a campfire block to have longer smoke trails, or not.
        // @tags
        // <MaterialTag.signal_fire>
        // -->
        if (mechanism.matches("is_in_wall") && mechanism.requireBoolean()) {
            getGate().setInWall(mechanism.getValue().asBoolean());
        }
    }
}

