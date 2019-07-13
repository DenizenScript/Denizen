package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizencore.objects.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.data.Levelled;

public class MaterialLevel implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof dMaterial
                && ((dMaterial) material).hasModernData()
                && ((dMaterial) material).getModernData().data instanceof Levelled;
    }

    public static MaterialLevel getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialLevel((dMaterial) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "maximum_level", "level"
    };

    public static final String[] handledMechs = new String[] {
            "level"
    };


    private MaterialLevel(dMaterial _material) {
        material = _material;
    }

    dMaterial material;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <m@material.maximum_level>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum level for a levelable material (like water, lava, and Cauldrons).
        // -->
        if (attribute.startsWith("maximum_level")) {
            return new ElementTag(getMax()).getAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <m@material.level>
        // @returns ElementTag(Number)
        // @mechanism dMaterial.level
        // @group properties
        // @description
        // Returns the current level for a levelable material (like water, lava, and Cauldrons).
        // -->
        if (attribute.startsWith("level")) {
            return new ElementTag(getCurrent()).getAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public Levelled getLevelled() {
        return (Levelled) material.getModernData().data;
    }

    public int getCurrent() {
        return getLevelled().getLevel();
    }

    public int getMax() {
        return getLevelled().getMaximumLevel();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "level";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object dMaterial
        // @name level
        // @input Element(Number)
        // @description
        // Sets the current level for a levelable material (like water, lava, and Cauldrons).
        // @tags
        // <m@material.level>
        // <m@material.maximum_level>
        // -->
        if (mechanism.matches("level") && mechanism.requireInteger()) {
            int level = mechanism.getValue().asInt();
            if (level < 0 || level > getMax()) {
                Debug.echoError("Level value '" + level + "' is not valid. Must be between 0 and " + getMax() + " for material '" + material.realName() + "'.");
                return;
            }
            getLevelled().setLevel(level);
        }
    }
}
