package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Campfire;

public class MaterialCampfire implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Campfire;
    }

    public static MaterialCampfire getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialCampfire((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "signal_fire"
    };

    private MaterialCampfire(MaterialTag _material) {
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
        PropertyParser.<MaterialCampfire>registerTag("signal_fire", (attribute, material) -> {
            return new ElementTag(material.getCampfire().isSignalFire());
        });
    }

    public Campfire getCampfire() {
        return (Campfire) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCampfire().isSignalFire());
    }

    @Override
    public String getPropertyId() {
        return "signal_fire";
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
        if (mechanism.matches("signal_fire") && mechanism.requireBoolean()) {
            getCampfire().setSignalFire(mechanism.getValue().asBoolean());
        }
    }
}

