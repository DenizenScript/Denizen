package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

public class MaterialCount extends MaterialProperty<ElementTag> {
    // TODO: once 1.21 is the minimum supported version, remove PinkPetals interface in favor of FlowerBed

    // <--[property]
    // @object MaterialTag
    // @name count
    // @input ElementTag(Number)
    // @description
    // Controls the amount of pickles in a Sea Pickle material, eggs in a Turtle Egg material, charges in a Respawn Anchor material, candles in a Candle material, flowers in a flower bed, or leaves in a leaf litter.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof SeaPickle
                || data instanceof TurtleEgg
                || data instanceof RespawnAnchor
                || data instanceof Candle
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && data instanceof PinkPetals)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && (data instanceof FlowerBed
                                                                        || data instanceof LeafLitter));
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getBlockData() instanceof SeaPickle seaPickle) {
            return new ElementTag(seaPickle.getPickles());
        }
        else if (getBlockData() instanceof TurtleEgg turtleEgg) {
            return new ElementTag(turtleEgg.getEggs());
        }
        else if (getBlockData() instanceof RespawnAnchor respawnAnchor) {
            return new ElementTag(respawnAnchor.getCharges());
        }
        else if (getBlockData() instanceof Candle candle) {
            return new ElementTag(candle.getCandles());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof PinkPetals pinkPetals) {
            return new ElementTag(pinkPetals.getFlowerAmount());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof FlowerBed flowerBed) {
            return new ElementTag(flowerBed.getFlowerAmount());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof LeafLitter leafLitter) {
            return new ElementTag(leafLitter.getSegmentAmount());
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            int count = value.asInt();
            if (count < getMin() || count > getMax()) {
                mechanism.echoError("Material count mechanism value '" + count + "' is not valid. Must be between " + getMin() + " and " + getMax() + ".");
                return;
            }
            if (getBlockData() instanceof SeaPickle seaPickle) {
                seaPickle.setPickles(count);
            }
            else if (getBlockData() instanceof TurtleEgg turtleEgg) {
                turtleEgg.setEggs(count);
            }
            else if (getBlockData() instanceof RespawnAnchor respawnAnchor) {
                respawnAnchor.setCharges(count);
            }
            else if (getBlockData() instanceof Candle candle) {
                candle.setCandles(count);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof PinkPetals pinkPetals) {
                pinkPetals.setFlowerAmount(count);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof FlowerBed flowerBed) {
                flowerBed.setFlowerAmount(count);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof LeafLitter leafLitter) {
                leafLitter.setSegmentAmount(count);
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "count";
    }

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.count_max>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the maximum amount of pickles allowed in a Sea Pickle material, eggs in a Turtle Egg material, charges in a Respawn Anchor material, candles in a Candle material, or petals in a Pink Petals material.
        // -->
        PropertyParser.registerStaticTag(MaterialCount.class, ElementTag.class, "count_max", (attribute, material) -> {
            return new ElementTag(material.getMax());
        }, "pickle_max");

        // <--[tag]
        // @attribute <MaterialTag.count_min>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the minimum amount of pickles allowed in a Sea Pickle material, eggs in a Turtle Egg material, charges in a Respawn Anchor material, candles in a Candle material, or petals in a Pink Petals material.
        // -->
        PropertyParser.registerStaticTag(MaterialCount.class, ElementTag.class, "count_min", (attribute, material) -> {
            return new ElementTag(material.getMin());
        }, "pickle_min");

        autoRegister("count", MaterialCount.class, ElementTag.class, false, "pickle_count");
    }

    public int getMax() {
        if (getBlockData() instanceof SeaPickle seaPickle) {
            return seaPickle.getMaximumPickles();
        }
        else if (getBlockData() instanceof TurtleEgg turtleEgg) {
            return turtleEgg.getMaximumEggs();
        }
        else if (getBlockData() instanceof RespawnAnchor respawnAnchor) {
            return respawnAnchor.getMaximumCharges();
        }
        else if (getBlockData() instanceof Candle candle) {
            return candle.getMaximumCandles();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof PinkPetals pinkPetals) {
            return pinkPetals.getMaximumFlowerAmount();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof FlowerBed flowerBed) {
            return flowerBed.getMaximumFlowerAmount();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof LeafLitter leafLitter) {
            return leafLitter.getMaximumSegmentAmount();
        }
        throw new UnsupportedOperationException();
    }

    public int getMin() {
        if (getBlockData() instanceof SeaPickle seaPickle) {
            return seaPickle.getMinimumPickles();
        }
        else if (getBlockData() instanceof TurtleEgg turtleEgg) {
            return turtleEgg.getMinimumEggs();
        }
        else if (getBlockData() instanceof RespawnAnchor) {
            return 0;
        }
        else if (getBlockData() instanceof Candle) {
            return 1;
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof PinkPetals) {
            return 1;
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof FlowerBed) {
            return 1;
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof LeafLitter) {
            return 1;
        }
        throw new UnsupportedOperationException();
    }
}
