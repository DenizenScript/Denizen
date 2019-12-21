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

public class MaterialLevel implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData().data instanceof Levelled
                || ((MaterialTag) material).getModernData().data instanceof Cake
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
        // Returns the maximum level for a levelable material (like water, lava, and Cauldrons), a cake, or a beehive.
        // -->
        PropertyParser.<MaterialLevel>registerTag("maximum_level", (attribute, material) -> {
            return new ElementTag(material.getMax());
        });

        // <--[tag]
        // @attribute <MaterialTag.level>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.level
        // @group properties
        // @description
        // Returns the current level for a levelable material (like water, lava, and Cauldrons), a cake, or a beehive.
        // -->
        PropertyParser.<MaterialLevel>registerTag("level", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        });
    }

    public boolean isCake() {
        return material.getModernData().data instanceof Cake;
    }

    public boolean isHive() {
            return (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_15) && material.getModernData().data instanceof Beehive);
        }

    public Levelled getLevelled() {
        return (Levelled) material.getModernData().data;
    }

    public Cake getCake() {
        return (Cake) material.getModernData().data;
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
        else if (isHive()) {
            return getHoneyLevel();
        }
        return getLevelled().getLevel();
    }

    public int getMax() {
        if (isCake()) {
            return getCake().getMaximumBites();
        }
        else if (isHive()) {
            return getMaxHoneyLevel();
        }
        return getLevelled().getMaximumLevel();
    }

    public void setCurrent(int level) {
        if (isCake()) {
            getCake().setBites(level);
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
        // Sets the current level for a levelable material (like water, lava, and Cauldrons), a cake, or a beehive.
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
