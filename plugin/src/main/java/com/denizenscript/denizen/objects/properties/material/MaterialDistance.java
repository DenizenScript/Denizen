package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Scaffolding;

public class MaterialDistance implements Property {

    public static boolean describes(Object material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData() instanceof Scaffolding;
    }

    public static MaterialDistance getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDistance((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "distance"
    };

    private MaterialDistance(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.distance>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.distance
        // @group properties
        // @description
        // Returns the horizontal distance between a scaffolding block and the nearest scaffolding block placed above a 'bottom' scaffold.
        // -->
        PropertyParser.<MaterialDistance, ElementTag>registerStaticTag(ElementTag.class, "distance", (attribute, material) -> {
            return new ElementTag(material.getDistance());
        });
    }

    public Scaffolding getScaffolding() {
        return (Scaffolding) material.getModernData();
    }

    public int getDistance() {
        return getScaffolding().getDistance();
    }

    public int getMaxDistance() {
        return getScaffolding().getMaximumDistance();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getScaffolding().getDistance());
    }

    @Override
    public String getPropertyId() {
        return "distance";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name distance
        // @input ElementTag(Number)
        // @description
        // Sets the horizontal distance between a scaffolding block and the nearest scaffolding block placed above a 'bottom' scaffold.
        // @tags
        // <MaterialTag.distance>
        // -->
        if (mechanism.matches("distance") && mechanism.requireInteger()) {
            int distance = mechanism.getValue().asInt();
            if (distance >= 0 && distance <= getMaxDistance()) {
                getScaffolding().setDistance(distance);
            }
            else {
                mechanism.echoError("Distance must be between 0 and " + getMaxDistance());
            }
        }
    }
}
