package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.Snow;

public class MaterialLevel implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData().data instanceof Levelled
                || ((MaterialTag) material).getModernData().data instanceof Cake
                || ((MaterialTag) material).getModernData().data instanceof Snow
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15) && ((MaterialTag) material).getModernData().data instanceof Beehive));
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
        // Returns the maximum level for a Levelled material (like water, lava, and cauldrons), cake, beehives, and snow.
        // -->
        PropertyParser.<MaterialLevel>registerTag("maximum_level", (attribute, material) -> {
            return new ElementTag(material.getMax());
        });

        // <--[tag]
        // @attribute <MaterialTag.minimum_level>
        // @returns ElementTag(Number)
        // @group properties
        // @description
        // Returns the minimum level for a Levelled material (like water, lava, and cauldrons), cake, beehives, and snow.
        // This will return 0 for all valid materials aside from snow.
        // -->
        PropertyParser.<MaterialLevel>registerTag("minimum_level", (attribute, material) -> {
            return new ElementTag(material.getMin());
        });

        // <--[tag]
        // @attribute <MaterialTag.level>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.level
        // @group properties
        // @description
        // Returns the current level for a Levelled material (like water, lava, and cauldrons), cake, beehives, and snow.
        // -->
        PropertyParser.<MaterialLevel>registerTag("level", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        });
    }
    
    public Levelled getLevelled() {
        return (Levelled) material.getModernData().data;
    }

    public boolean isCake() {
        return material.getModernData().data instanceof Cake;
    }

    public Cake getCake() {
        return (Cake) material.getModernData().data;
    }

    public boolean isSnow() {
        return material.getModernData().data instanceof Snow;
    }

    public Snow getSnow() {
        return (Snow) material.getModernData().data;
    }

    public boolean isHive() {
        return (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15) && material.getModernData().data instanceof Beehive);
    }

    public int getHoneyLevel() {
        return ((Beehive) material.getModernData().data).getHoneyLevel();
    }

    public int getMaxHoneyLevel() {
        return ((Beehive) material.getModernData().data).getMaximumHoneyLevel();
    }

    public void setHoneyLevel(int level) {
        ((Beehive) material.getModernData().data).setHoneyLevel(level);
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
        // Sets the current level for a Levelled material (like water, lava, and cauldrons), cake, beehives, and snow.
        // @tags
        // <MaterialTag.level>
        // <MaterialTag.maximum_level>
        // <MaterialTag.minimum_level>
        // -->
        if (mechanism.matches("level") && mechanism.requireInteger()) {
            int level = mechanism.getValue().asInt();
            if (level < getMin() || level > getMax()) {
                Debug.echoError("Level value '" + level + "' is not valid. Must be between " + getMin() + " and " + getMax() + " for material '" + material.realName() + "'.");
                return;
            }
            setCurrent(level);
        }
    }
}
