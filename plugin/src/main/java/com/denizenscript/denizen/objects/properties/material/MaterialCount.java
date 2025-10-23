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
    // TODO The PinkPetals interface was deprecated in 1.21.5 and merged into the FlowerBed interface.
    //  All references and checks involving them can be removed once 1.21 is the minimum supported version.

    // <--[property]
    // @object MaterialTag
    // @name count
    // @input ElementTag(Number)
    // @description
    // Controls the amount of pickles in a Sea Pickle material, eggs in a Turtle Egg material, charges in a Respawn Anchor material, candles in a Candle material, flowers in a flower bed, or leaves on the ground.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof SeaPickle
                || data instanceof TurtleEgg
                || data instanceof RespawnAnchor
                || data instanceof Candle
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && data instanceof PinkPetals)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && data instanceof FlowerBed)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && data instanceof LeafLitter);
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getCurrent());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireInteger()) {
            int count = value.asInt();
            if (count < getMin() || count > getMax()) {
                mechanism.echoError("Material count mechanism value '" + count + "' is not valid. Must be between " + getMin() + " and " + getMax() + ".");
                return;
            }
            if (isSeaPickle()) {
                getSeaPickle().setPickles(count);
            }
            else if (isTurtleEgg()) {
                getTurtleEgg().setEggs(count);
            }
            else if (isRespawnAnchor()) {
                getRespawnAnchor().setCharges(count);
            }
            else if (isCandle()) {
                getCandle().setCandles(count);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof FlowerBed flowerBed) {
                flowerBed.setFlowerAmount(count);
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof PinkPetals pinkPetals) {
                pinkPetals.setFlowerAmount(count);
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

    public boolean isSeaPickle() {
        return getBlockData() instanceof SeaPickle;
    }

    public boolean isTurtleEgg() {
        return getBlockData() instanceof TurtleEgg;
    }

    public boolean isRespawnAnchor() {
        return getBlockData() instanceof RespawnAnchor;
    }

    public boolean isCandle() {
        return getBlockData() instanceof Candle;
    }

    public TurtleEgg getTurtleEgg() {
        return (TurtleEgg) getBlockData();
    }

    public SeaPickle getSeaPickle() {
        return (SeaPickle) getBlockData();
    }

    public RespawnAnchor getRespawnAnchor() {
        return (RespawnAnchor) getBlockData();
    }

    public Candle getCandle() {
        return (Candle) getBlockData();
    }

    public int getCurrent() {
        if (isSeaPickle()) {
            return getSeaPickle().getPickles();
        }
        else if (isTurtleEgg()) {
            return getTurtleEgg().getEggs();
        }
        else if (isRespawnAnchor()) {
            return getRespawnAnchor().getCharges();
        }
        else if (isCandle()) {
            return getCandle().getCandles();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof FlowerBed flowerBed) {
            return flowerBed.getFlowerAmount();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof PinkPetals pinkPetals) {
            return pinkPetals.getFlowerAmount();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof LeafLitter leafLitter) {
            return leafLitter.getSegmentAmount();
        }
        throw new UnsupportedOperationException();
    }

    public int getMax() {
        if (isSeaPickle()) {
            return getSeaPickle().getMaximumPickles();
        }
        else if (isTurtleEgg()) {
            return getTurtleEgg().getMaximumEggs();
        }
        else if (isRespawnAnchor()) {
            return getRespawnAnchor().getMaximumCharges();
        }
        else if (isCandle()) {
            return getCandle().getMaximumCandles();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof FlowerBed flowerBed) {
            return flowerBed.getMaximumFlowerAmount();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof PinkPetals pinkPetals) {
            return pinkPetals.getMaximumFlowerAmount();
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof LeafLitter leafLitter) {
            return leafLitter.getMaximumSegmentAmount();
        }
        throw new UnsupportedOperationException();
    }

    public int getMin() {
        if (isSeaPickle()) {
            return getSeaPickle().getMinimumPickles();
        }
        else if (isTurtleEgg()) {
            return getTurtleEgg().getMinimumEggs();
        }
        else if (isRespawnAnchor()) {
            return 0;
        }
        else if ((NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof PinkPetals)
            || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && (getBlockData() instanceof FlowerBed) || getBlockData() instanceof LeafLitter)
            || isCandle()) {
            return 1;
        }
        throw new UnsupportedOperationException();
    }
}
