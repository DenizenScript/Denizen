package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Candle;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.block.data.type.TurtleEgg;

public class MaterialCount extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name count
    // @input ElementTag(Number)
    // @description
    // Controls the amount of pickles in a Sea Pickle material, eggs in a Turtle Egg material, charges in a Respawn Anchor material, or candles in a Candle material.
    // See also:
    // <@link tag MaterialTag.count>
    // <@link tag MaterialTag.count_min>
    // <@link tag MaterialTag.count_max>
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof SeaPickle
                || data instanceof TurtleEgg
                || data instanceof RespawnAnchor
                || data instanceof Candle;
    }

    MaterialTag material;

    public static void register() {

        autoRegister("count", MaterialCount.class, ElementTag.class, true, "pickle_count");

        // <--[tag]
        // @attribute <MaterialTag.count>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the amount of pickles in a Sea Pickle material, eggs in a Turtle Egg material, charges in a Respawn Anchor material, or candles in a Candle material.
        // -->
        PropertyParser.registerStaticTag(MaterialCount.class, ElementTag.class, "count", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        }, "pickle_count");

        // <--[tag]
        // @attribute <MaterialTag.count_max>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the maximum amount of pickles allowed in a Sea Pickle material, eggs in a Turtle Egg material, charges in a Respawn Anchor material, or candles in a Candle material.
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
        // Returns the minimum amount of pickles allowed in a Sea Pickle material, eggs in a Turtle Egg material, charges in a Respawn Anchor material, or candles in a Candle material.
        // -->
        PropertyParser.registerStaticTag(MaterialCount.class, ElementTag.class, "count_min", (attribute, material) -> {
            return new ElementTag(material.getMin());
        }, "pickle_min");
    }

    public boolean isSeaPickle() {
        return material.getModernData() instanceof SeaPickle;
    }

    public boolean isTurtleEgg() {
        return material.getModernData() instanceof TurtleEgg;
    }

    public boolean isRespawnAnchor() {
        return material.getModernData() instanceof RespawnAnchor;
    }

    public boolean isCandle() {
        return material.getModernData() instanceof Candle;
    }

    public TurtleEgg getTurtleEgg() {
        return (TurtleEgg) material.getModernData();
    }

    public SeaPickle getSeaPickle() {
        return (SeaPickle) material.getModernData();
    }

    public RespawnAnchor getRespawnAnchor() {
        return (RespawnAnchor) material.getModernData();
    }

    public Candle getCandle() {
        return (Candle) material.getModernData();
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
        else if (isCandle()) {
            return 1;
        }
        throw new UnsupportedOperationException();
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getCurrent());
    }

    @Override
    public void setPropertyValue(ElementTag elementTag, Mechanism mechanism) {
        if (!mechanism.requireInteger()) {
            return;
        }
        int count = elementTag.asInt();
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
    }

    @Override
    public String getPropertyId() {
        return "count";
    }
}
