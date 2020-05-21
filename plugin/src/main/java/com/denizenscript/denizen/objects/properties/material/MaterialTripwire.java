package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Tripwire;

public class MaterialTripwire implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Tripwire;
    }

    public static MaterialTripwire getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialTripwire((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "disarmed"
    };

    private MaterialTripwire(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.is_disarmed>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.is_disarmed
        // @group properties
        // @description
        // Returns whether a tripwire block is disarmed, or not.
        // -->
        PropertyParser.<MaterialTripwire>registerTag("disarmed", (attribute, material) -> {
            return new ElementTag(material.getTripWire().isDisarmed());
        });
    }

    public Tripwire getTripWire() {
        return (Tripwire) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getTripWire().isDisarmed());
    }

    @Override
    public String getPropertyId() {
        return "disarmed";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name is_disarmed
        // @input ElementTag(Boolean)
        // @description
        // Sets whether a tripwire block is disarmed, or not.
        // @tags
        // <MaterialTag.is_disarmed>
        // -->
        if (mechanism.matches("disarmed") && mechanism.requireBoolean()) {
            getTripWire().setDisarmed(mechanism.getValue().asBoolean());
        }
    }
}

