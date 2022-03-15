package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.Deprecations;
import org.bukkit.block.data.type.Campfire;

@Deprecated
public class MaterialCampfire implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Campfire;
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
        PropertyParser.<MaterialCampfire, ElementTag>registerTag(ElementTag.class, "signal_fire", (attribute, material) -> {
            Deprecations.materialCampfire.warn(attribute.context);
            return new ElementTag(material.getCampfire().isSignalFire());
        });
    }

    public Campfire getCampfire() {
        return (Campfire) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        return null;
    }

    @Override
    public String getPropertyId() {
        return "signal_fire";
    }

    @Override
    public void adjust(Mechanism mechanism) {
        if (mechanism.matches("signal_fire") && mechanism.requireBoolean()) {
            Deprecations.materialCampfire.warn(mechanism.context);
            getCampfire().setSignalFire(mechanism.getValue().asBoolean());
        }
    }
}
