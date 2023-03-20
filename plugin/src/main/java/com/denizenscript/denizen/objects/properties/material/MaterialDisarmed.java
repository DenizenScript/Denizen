package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Tripwire;

public class MaterialDisarmed implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag mat)) {
            return false;
        }
        if (!mat.hasModernData()) {
            return false;
        }
        return (mat.getModernData() instanceof Tripwire);
    }

    public static MaterialDisarmed getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDisarmed((MaterialTag) _material);
        }
    }

    private MaterialDisarmed(MaterialTag _material) {
        material = _material;
    }

    public MaterialTag material;

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.disarmed>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.disarmed
        // @group properties
        // @description
        // Returns the current disarmed state of the tripwire.
        // For tripwires, is true (corresponding to "disarmed") or false (corresponding to "armed").
        // -->
        PropertyParser.registerStaticTag(MaterialDisarmed.class, ElementTag.class, "disarmed", (attribute, material) -> {
            return new ElementTag(material.getTripwire().isDisarmed());
        });

        // <--[mechanism]
        // @object MaterialTag
        // @name disarmed
        // @input ElementTag(Boolean)
        // @description
        // Sets the current disarmed state of the tripwire.
        // For tripwires, input is true (corresponding to "disarmed") or false (corresponding to "armed").
        // @tags
        // <MaterialTag.disarmed>
        // -->
        PropertyParser.registerMechanism(MaterialDisarmed.class, ElementTag.class, "disarmed", (prop, mechanism, param) -> {
            prop.getTripwire().setDisarmed(param.asBoolean());
        });
    }

    public Tripwire getTripwire() {
        return (Tripwire) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getTripwire().isDisarmed());
    }

    @Override
    public String getPropertyId() {
        return "disarmed";
    }
}
