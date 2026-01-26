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
import org.bukkit.block.data.type.*;

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
    // For dried ghasts, this is the level of hydration. 1.21+ only.
    // See also <@link tag MaterialTag.maximum_level> and <@link tag MaterialTag.minimum_level>.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Levelled 
                || data instanceof Cake 
                || data instanceof Snow 
                || data instanceof Farmland 
                || data instanceof Beehive 
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && data instanceof Brushable)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && data instanceof DriedGhast);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getBlockData() instanceof Cake cake) {
            return new ElementTag(cake.getBites());
        }
        else if (getBlockData() instanceof Snow snow) {
            return new ElementTag(snow.getLayers());
        }
        else if (getBlockData() instanceof Beehive beehive) {
            return new ElementTag(beehive.getHoneyLevel());
        }
        else if (getBlockData() instanceof Farmland farmland) {
            return new ElementTag(farmland.getMoisture());
        }
        else if (getBlockData() instanceof Levelled levelled) {
            return new ElementTag(levelled.getLevel());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && getBlockData() instanceof Brushable brushable) {
            return new ElementTag(brushable.getDusted());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof DriedGhast driedGhast) {
            return new ElementTag(driedGhast.getHydration());
        }
        return null;
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
        if (getBlockData() instanceof Cake cake) {
            cake.setBites(level);
        }
        else if (getBlockData() instanceof Snow snow) {
            snow.setLayers(level);
        }
        else if (getBlockData() instanceof Beehive beehive) {
            beehive.setHoneyLevel(level);
        }
        else if (getBlockData() instanceof Farmland farmland) {
            farmland.setMoisture(level);
        }
        else if (getBlockData() instanceof Levelled levelled) {
            levelled.setLevel(level);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && getBlockData() instanceof Brushable brushable) {
            brushable.setDusted(level);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof DriedGhast driedGhast) {
            driedGhast.setHydration(level);
        }
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

    public int getMax() {
        if (getBlockData() instanceof Cake cake) {
            return cake.getMaximumBites();
        }
        else if (getBlockData() instanceof Snow snow) {
            return snow.getMaximumLayers();
        }
        else if (getBlockData() instanceof Beehive beehive) {
            return beehive.getMaximumHoneyLevel();
        }
        else if (getBlockData() instanceof Farmland farmland) {
            return farmland.getMaximumMoisture();
        }
        else if (getBlockData() instanceof Levelled levelled) {
            return levelled.getMaximumLevel();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_20) && getBlockData() instanceof Brushable brushable) {
            return brushable.getMaximumDusted();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof DriedGhast driedGhast) {
            return driedGhast.getMaximumHydration();
        }
        throw new UnsupportedOperationException();
    }

    public int getMin() {
        if (getBlockData() instanceof Snow snow) {
            return snow.getMinimumLayers();
        }
        return 0;
    }
}
