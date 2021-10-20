package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.BubbleColumn;
import org.bukkit.block.data.type.Comparator;
import org.bukkit.block.data.type.PistonHead;

public class MaterialMode implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag)) {
            return false;
        }
        MaterialTag mat = (MaterialTag) material;
        if (!mat.hasModernData()) {
            return false;
        }
        BlockData data = mat.getModernData();
        return data instanceof Comparator
                || data instanceof PistonHead
                || data instanceof BubbleColumn;
    }

    public static MaterialMode getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialMode((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "mode"
    };

    private MaterialMode(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.mode>
        // @returns ElementTag
        // @mechanism MaterialTag.mode
        // @group properties
        // @description
        // Returns a block's mode.
        // For comparators, output is COMPARE or SUBTRACT.
        // For piston_heads, output is NORMAL or SHORT.
        // For bubble-columns, output is NORMAL or DRAG.
        // -->
        PropertyParser.<MaterialMode, ElementTag>registerTag(ElementTag.class, "mode", (attribute, material) -> {
            return new ElementTag(material.getPropertyString());
        });
    }

    public boolean isComparator() {
        return material.getModernData() instanceof Comparator;
    }

    public boolean isPistonHead() {
        return material.getModernData() instanceof PistonHead;
    }

    public boolean isBubbleColumn() {
        return material.getModernData() instanceof BubbleColumn;
    }

    public Comparator getComparator() {
        return (Comparator) material.getModernData();
    }

    public PistonHead getPistonHead() {
        return (PistonHead) material.getModernData();
    }

    public BubbleColumn getBubbleColumn() {
        return (BubbleColumn) material.getModernData();
    }

    @Override
    public String getPropertyString() {
        if (isComparator()) {
            return getComparator().getMode().name();
        }
        else if (isBubbleColumn()) {
            return getBubbleColumn().isDrag() ? "DRAG" : "NORMAL";
        }
        else {
            return getPistonHead().isShort() ? "SHORT" : "NORMAL";
        }
    }

    @Override
    public String getPropertyId() {
        return "mode";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name mode
        // @input ElementTag
        // @description
        // Set a block's mode.
        // For comparators, input is COMPARE or SUBTRACT.
        // For piston_heads, input is NORMAL or SHORT.
        // For bubble-columns, input is NORMAL or DRAG.
        // @tags
        // <MaterialTag.mode>
        // -->
        if (mechanism.matches("mode")) {
            if (isComparator() && mechanism.requireEnum(false, Comparator.Mode.values())) {
                getComparator().setMode(Comparator.Mode.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isBubbleColumn()) {
                getBubbleColumn().setDrag(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "drag"));
            }
            else if (isPistonHead()) {
                getPistonHead().setShort(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "short"));
            }
        }
    }
}
