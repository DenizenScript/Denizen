package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.RespawnAnchor;
import org.bukkit.block.data.type.SeaPickle;
import org.bukkit.block.data.type.TurtleEgg;

public class MaterialCount implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData() instanceof SeaPickle
                || ((MaterialTag) material).getModernData() instanceof TurtleEgg
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_16) && ((MaterialTag) material).getModernData() instanceof RespawnAnchor));
    }

    public static MaterialCount getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialCount((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "count", "pickle_count"
    };

    private MaterialCount(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.count>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the amount of pickles in a Sea Pickle material, eggs in a Turtle Egg material, or charges in a Respawn Anchor material.
        // -->
        PropertyParser.<MaterialCount, ElementTag>registerStaticTag(ElementTag.class, "count", (attribute, material) -> {
            return new ElementTag(material.getCurrent());
        }, "pickle_count");

        // <--[tag]
        // @attribute <MaterialTag.count_max>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the maximum amount of pickles allowed in a Sea Pickle material, eggs in a Turtle Egg material, or charges in a Respawn Anchor material.
        // -->
        PropertyParser.<MaterialCount, ElementTag>registerStaticTag(ElementTag.class, "count_max", (attribute, material) -> {
            return new ElementTag(material.getMax());
        }, "pickle_max");

        // <--[tag]
        // @attribute <MaterialTag.count_min>
        // @returns ElementTag(Number)
        // @mechanism MaterialTag.count
        // @group properties
        // @description
        // Returns the minimum amount of pickles allowed in a Sea Pickle material, eggs in a Turtle Egg material, or charges in a Respawn Anchor material.
        // -->
        PropertyParser.<MaterialCount, ElementTag>registerStaticTag(ElementTag.class, "count_min", (attribute, material) -> {
            return new ElementTag(material.getMin());
        }, "pickle_min");
    }

    public boolean isSeaPickle() {
        return material.getModernData() instanceof SeaPickle;
    }

    public boolean isTurtleEgg() {
        return material.getModernData() instanceof TurtleEgg;
    }

    public TurtleEgg getTurtleEgg() {
        return (TurtleEgg) material.getModernData();
    }

    public SeaPickle getSeaPickle() {
        return (SeaPickle) material.getModernData();
    }

    public int getCurrent() {
        if (isSeaPickle()) {
            return getSeaPickle().getPickles();
        }
        else if (isTurtleEgg()) {
            return getTurtleEgg().getEggs();
        }
        else {
            return ((RespawnAnchor) material.getModernData()).getCharges();
        }
    }

    public int getMax() {
        if (isSeaPickle()) {
            return getSeaPickle().getMaximumPickles();
        }
        else if (isTurtleEgg()) {
            return getTurtleEgg().getMaximumEggs();
        }
        else {
            return ((RespawnAnchor) material.getModernData()).getMaximumCharges();
        }
    }

    public int getMin() {
        if (isSeaPickle()) {
            return getSeaPickle().getMinimumPickles();
        }
        else if (isTurtleEgg()) {
            return getTurtleEgg().getMinimumEggs();
        }
        else { // Respawn anchor
            return 0;
        }
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getCurrent());
    }

    @Override
    public String getPropertyId() {
        return "count";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name count
        // @input ElementTag(Number)
        // @description
        // Sets the amount of pickles in a Sea Pickle material, eggs in a Turtle Egg material, or charges in a Respawn Anchor material.
        // @tags
        // <MaterialTag.count>
        // <MaterialTag.count_min>
        // <MaterialTag.count_max>
        // -->
        if ((mechanism.matches("count") || (mechanism.matches("pickle_count"))) && mechanism.requireInteger()) {
            int count = mechanism.getValue().asInt();
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
            else {
                ((RespawnAnchor) material.getModernData()).setCharges(count);
            }
        }
    }
}
