package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Cake;

public class MaterialLevel implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData().data instanceof Levelled
                || ((MaterialTag) material).getModernData().data instanceof Cake);
    }

    public static MaterialLevel getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialLevel((MaterialTag) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "maximum_level", "level"
    };

    public static final String[] handledMechs = new String[] {
            "level"
    };


    private MaterialLevel(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <MaterialTag.maximum_level>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum level for a levelable material (like water, lava, and Cauldrons), or a cake.
        // -->
        if (attribute.startsWith("maximum_level")) {
            return new ElementTag(getMax()).getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <MaterialTag.level>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.level
        // @group properties
        // @description
        // Returns the current level for a levelable material (like water, lava, and Cauldrons), or a cake.
        // -->
        if (attribute.startsWith("level")) {
            return new ElementTag(getCurrent()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public boolean isCake() {
        return material.getModernData().data instanceof Cake;
    }

    public Levelled getLevelled() {
        return (Levelled) material.getModernData().data;
    }

    public Cake getCake() {
        return (Cake) material.getModernData().data;
    }

    public int getCurrent() {
        if (isCake()) {
            return getCake().getBites();
        }
        return getLevelled().getLevel();
    }

    public int getMax() {
        if (isCake()) {
            return getCake().getMaximumBites();
        }
        return getLevelled().getMaximumLevel();
    }

    public void setCurrent(int level) {
        if (isCake()) {
            getCake().setBites(level);
            return;
        }
        getLevelled().setLevel(level);
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
        // @object MaterialTag
        // @name level
        // @input Element(Number)
        // @description
        // Sets the current level for a levelable material (like water, lava, and Cauldrons), or a cake.
        // @tags
        // <MaterialTag.level>
        // <MaterialTag.maximum_level>
        // -->
        if (mechanism.matches("level") && mechanism.requireInteger()) {
            int level = mechanism.getValue().asInt();
            if (level < 0 || level > getMax()) {
                Debug.echoError("Level value '" + level + "' is not valid. Must be between 0 and " + getMax() + " for material '" + material.realName() + "'.");
                return;
            }
            setCurrent(level);
        }
    }
}
