package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

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
                || data instanceof BubbleColumn
                || data instanceof StructureBlock
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && data instanceof SculkSensor)
                || data instanceof DaylightDetector
                || data instanceof CommandBlock;
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
        // For structure-blocks, output is CORNER, DATA, LOAD, or SAVE.
        // For sculk-sensors, output is ACTIVE, COOLDOWN, or INACTIVE.
        // For daylight-detectors, output is INVERTED or NORMAL.
        // For command-blocks, output is CONDITIONAL or NORMAL.
        // -->
        PropertyParser.<MaterialMode, ElementTag>registerStaticTag(ElementTag.class, "mode", (attribute, material) -> {
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

    public boolean isStructureBlock() {
        return material.getModernData() instanceof StructureBlock;
    }

    public boolean isSculkSensor() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && material.getModernData() instanceof SculkSensor;
    }

    public boolean isDaylightDetector() {
        return material.getModernData() instanceof DaylightDetector;
    }

    public boolean isCommandBlock() {
        return material.getModernData() instanceof CommandBlock;
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

    public StructureBlock getStructureBlock() {
        return (StructureBlock) material.getModernData();
    }

    public DaylightDetector getDaylightDetector() {
        return (DaylightDetector) material.getModernData();
    }

    public CommandBlock getCommandBlock() {
        return (CommandBlock) material.getModernData();
    }

    /*public SculkSensor getSculkSensor() { // TODO 1.17
        return (SculkSensor) material.getModernData();
    }*/

    @Override
    public String getPropertyString() {
        if (isComparator()) {
            return getComparator().getMode().name();
        }
        else if (isBubbleColumn()) {
            return getBubbleColumn().isDrag() ? "DRAG" : "NORMAL";
        }
        else if (isPistonHead()) {
            return getPistonHead().isShort() ? "SHORT" : "NORMAL";
        }
        else if (isStructureBlock()) {
            return getStructureBlock().getMode().name();
        }
        else if (isSculkSensor()) {
            return ((SculkSensor) material.getModernData()).getPhase().name(); // TODO 1.17
        }
        else if (isDaylightDetector()) {
            return getDaylightDetector().isInverted() ? "INVERTED" : "NORMAL";
        }
        else if (isCommandBlock()) {
            return getCommandBlock().isConditional() ? "CONDITIONAL" : "NORMAL";
        }
        return null; //Unreachable
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
        // For piston-heads, input is NORMAL or SHORT.
        // For bubble-columns, input is NORMAL or DRAG.
        // For structure-blocks, input is CORNER, DATA, LOAD, or SAVE.
        // For sculk-sensors, input is ACTIVE, COOLDOWN, or INACTIVE.
        // For daylight-detectors, input is INVERTED or NORMAL.
        // For command-blocks, input is CONDITIONAL or NORMAL.
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
            else if (isStructureBlock() && mechanism.requireEnum(false, StructureBlock.Mode.values())) {
                getStructureBlock().setMode(StructureBlock.Mode.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isSculkSensor() && mechanism.requireEnum(false, SculkSensor.Phase.values())) {
                ((SculkSensor) material.getModernData()).setPhase(SculkSensor.Phase.valueOf(mechanism.getValue().asString().toUpperCase())); // TODO 1.17
            }
            else if (isDaylightDetector()) {
                getDaylightDetector().setInverted(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "inverted"));
            }
            else if (isCommandBlock()) {
                getCommandBlock().setConditional(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "conditional"));
            }
        }
    }
}
