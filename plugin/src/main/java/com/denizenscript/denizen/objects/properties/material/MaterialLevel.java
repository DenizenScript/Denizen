package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Snow;

public class MaterialLevel implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag)) {
            return false;
        }
        MaterialTag mat = (MaterialTag) material;
        if (!mat.hasModernData()) {
            return false;
        }
        BlockData data = mat.getModernData();
        return data instanceof Levelled
                || data instanceof Cake
                || data instanceof Snow
                || data instanceof Farmland
                || data instanceof Beehive;
    }

    public static MaterialLevel getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialLevel((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "level"
    };

    private MaterialLevel(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.maximum_level>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the maximum level for a Levelled material (like water, lava, and cauldrons), cake, beehives, snow, or farmland.
        // -->
        PropertyParser.registerStaticTag(MaterialLevel.class, ElementTag.class, "maximum_level", (attribute, material) -> {
            return new ElementTag(material.getMax());
        });

        // <--[tag]
        // @attribute <MaterialTag.minimum_level>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the minimum level for a Levelled material (like water, lava, and cauldrons), cake, beehives, snow, or farmland.
        // This will return 0 for all valid materials aside from snow.
        // -->
        PropertyParser.registerStaticTag(MaterialLevel.class, ElementTag.class, "minimum_level", (attribute, material) -> {
            return new ElementTag(material.getMin());
        });

        // <--[tag]
        // @attribute <MaterialTag.level>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.level
        // @group properties
        // @description
        // Returns the current level for a Levelled material, cake, beehives, snow, or farmland.
        // "Levelled" materials include: water, lava, cauldrons, composters, light blocks, and any other future Levelled implementing types.
        // For light blocks, this is the brightness of the light.
        // For water/lava this is the height of the liquid block.
        // For cauldrons, this is the amount of liquid contained.
        // For cake, this is the number of bites left.
        // For beehives/bee nests, this is the amount of honey contained.
        // For snow, this is the number of partial layers, or the height, of a snow block.
        // For farmland, this is the moisture level.
        // -->
        PropertyParser.registerStaticTag(MaterialLevel.class, ElementTag.class, "level", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        });
    }

    public Levelled getLevelled() {
        return (Levelled) material.getModernData();
    }

    public boolean isCake() {
        return material.getModernData() instanceof Cake;
    }

    public Cake getCake() {
        return (Cake) material.getModernData();
    }

    public boolean isSnow() {
        return material.getModernData() instanceof Snow;
    }

    public Snow getSnow() {
        return (Snow) material.getModernData();
    }

    public boolean isHive() {
        return material.getModernData() instanceof Beehive;
    }

    public Beehive getHive() {
        return (Beehive) material.getModernData();
    }

    public boolean isFarmland() {
        return material.getModernData() instanceof Farmland;
    }

    public Farmland getFarmland() {
        return (Farmland) material.getModernData();
    }

    public int getCurrent() {
        if (isCake()) {
            return getCake().getBites();
        }
        else if (isSnow()) {
            return getSnow().getLayers();
        }
        else if (isHive()) {
            return getHive().getHoneyLevel();
        }
        else if (isFarmland()) {
            return getFarmland().getMoisture();
        }
        return getLevelled().getLevel();
    }

    public int getMax() {
        if (isCake()) {
            return getCake().getMaximumBites();
        }
        else if (isSnow()) {
            return getSnow().getMaximumLayers();
        }
        else if (isHive()) {
            return getHive().getMaximumHoneyLevel();
        }
        else if (isFarmland()) {
            return getFarmland().getMaximumMoisture();
        }
        return getLevelled().getMaximumLevel();
    }

    public int getMin() {
        if (isSnow()) {
            return getSnow().getMinimumLayers();
        }
        return 0;
    }

    public void setCurrent(int level) {
        if (isCake()) {
            getCake().setBites(level);
            return;
        }
        else if (isSnow()) {
            getSnow().setLayers(level);
            return;
        }
        else if (isHive()) {
            getHive().setHoneyLevel(level);
            return;
        }
        else if (isFarmland()) {
            getFarmland().setMoisture(level);
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
        // @input ElementTag(Number)
        // @description
        // Sets the current level for a Levelled material (like water, lava, and cauldrons), cake, beehives, snow, or farmland.
        // @tags
        // <MaterialTag.level>
        // <MaterialTag.maximum_level>
        // <MaterialTag.minimum_level>
        // -->
        if (mechanism.matches("level") && mechanism.requireInteger()) {
            int level = mechanism.getValue().asInt();
            if (level < getMin() || level > getMax()) {
                mechanism.echoError("Level value '" + level + "' is not valid. Must be between " + getMin() + " and " + getMax() + " for material '" + material.name() + "'.");
                return;
            }
            setCurrent(level);
        }
    }
}
