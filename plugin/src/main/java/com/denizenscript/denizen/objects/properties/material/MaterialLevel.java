package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Snow;

public class MaterialLevel implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData() instanceof Levelled
                || ((MaterialTag) material).getModernData() instanceof Cake
                || ((MaterialTag) material).getModernData() instanceof Snow
                || ((MaterialTag) material).getModernData() instanceof Farmland
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15) && ((MaterialTag) material).getModernData() instanceof Beehive));
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
        // Returns the maximum level for a Levelled material (like water, lava, and cauldrons), cake, beehives, snow, and farmland.
        // -->
        PropertyParser.<MaterialLevel, ElementTag>registerStaticTag(ElementTag.class, "maximum_level", (attribute, material) -> {
            return new ElementTag(material.getMax());
        });

        // <--[tag]
        // @attribute <MaterialTag.minimum_level>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the minimum level for a Levelled material (like water, lava, and cauldrons), cake, beehives, snow, and farmland.
        // This will return 0 for all valid materials aside from snow.
        // -->
        PropertyParser.<MaterialLevel, ElementTag>registerStaticTag(ElementTag.class, "minimum_level", (attribute, material) -> {
            return new ElementTag(material.getMin());
        });

        // <--[tag]
        // @attribute <MaterialTag.level>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.level
        // @group properties
        // @description
        // Returns the current level for a Levelled material (like water, lava, and cauldrons), cake, beehives, snow, and farmland.
        // -->
        PropertyParser.<MaterialLevel, ElementTag>registerStaticTag(ElementTag.class, "level", (attribute, material) -> {
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
        return (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15) && material.getModernData() instanceof Beehive);
    }

    public int getHoneyLevel() {
        return ((Beehive) material.getModernData()).getHoneyLevel();
    }

    public int getMaxHoneyLevel() {
        return ((Beehive) material.getModernData()).getMaximumHoneyLevel();
    }

    public void setHoneyLevel(int level) {
        ((Beehive) material.getModernData()).setHoneyLevel(level);
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
            return getHoneyLevel();
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
            return getMaxHoneyLevel();
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
            setHoneyLevel(level);
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
        // Sets the current level for a Levelled material (like water, lava, and cauldrons), cake, beehives, snow, and farmland.
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
