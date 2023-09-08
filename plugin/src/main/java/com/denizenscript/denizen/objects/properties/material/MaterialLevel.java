package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Brushable;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Beehive;
import org.bukkit.block.data.type.Cake;
import org.bukkit.block.data.type.Farmland;
import org.bukkit.block.data.type.Snow;

public class MaterialLevel extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name level
    // @input ElementTag(Number)
    // @description
    // Controls the current level for a Levelled material, cake, beehives, snow, farmland, or brushable blocks.
    // "Levelled" materials include: water, lava, cauldrons, composters, light blocks, brushable blocks, and any other future Levelled implementing types.
    // For light blocks, this is the brightness of the light.
    // For water/lava this is the height of the liquid block.
    // For cauldrons, this is the amount of liquid contained.
    // For cake, this is the number of bites left.
    // For beehives/bee nests, this is the amount of honey contained.
    // For snow, this is the number of partial layers, or the height, of a snow block.
    // For farmland, this is the moisture level.
    // For composters, this is the amount of compost.
    // For brushable blocks (also referred to as "suspicious blocks"), this is the level of dusting. 1.20+ only.
    // See also <@link tag MaterialTag.maximum_level> and <@link tag MaterialTag.minimum_level>.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Levelled 
                || data instanceof Cake 
                || data instanceof Snow 
                || data instanceof Farmland 
                || data instanceof Beehive 
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && data instanceof Brushable);
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "level";
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireInteger()) {
            return;
        }
        int level = value.asInt();
        if (level < getMin() || level > getMax()) {
            mechanism.echoError("Level value '" + level + "' is not valid. Must be between " + getMin() + " and " + getMax() + " for material '" + getBlockData().getMaterial().name() + "'.");
            return;
        }
        setCurrent(level);
    }

    public static void register() {

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

        autoRegister("level", MaterialLevel.class, ElementTag.class, false);
    }

    public Levelled getLevelled() {
        return (Levelled) getBlockData();
    }

    public boolean isCake() {
        return getBlockData() instanceof Cake;
    }

    public Cake getCake() {
        return (Cake) getBlockData();
    }

    public boolean isSnow() {
        return getBlockData() instanceof Snow;
    }

    public Snow getSnow() {
        return (Snow) getBlockData();
    }

    public boolean isHive() {
        return getBlockData() instanceof Beehive;
    }

    public Beehive getHive() {
        return (Beehive) getBlockData();
    }

    public boolean isFarmland() {
        return getBlockData() instanceof Farmland;
    }

    public Farmland getFarmland() {
        return (Farmland) getBlockData();
    }

    public boolean isBrushable() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && getBlockData() instanceof Brushable;
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
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && isBrushable()) {
            return ((Brushable) getBlockData()).getDusted();
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
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && isBrushable()) {
            return ((Brushable) getBlockData()).getMaximumDusted();
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
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && isBrushable()) {
            ((Brushable) getBlockData()).setDusted(level);
            return;
        }
        getLevelled().setLevel(level);
    }
}
