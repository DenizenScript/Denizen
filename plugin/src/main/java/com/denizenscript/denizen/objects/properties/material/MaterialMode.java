package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.utilities.MultiVersionHelper1_17;
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
                || data instanceof DaylightDetector
                || data instanceof CommandBlock
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && (data instanceof SculkSensor
                                                                        || data instanceof BigDripleaf));
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

    public MaterialTag material;

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
        // For bubble_columns, output is NORMAL or DRAG.
        // For structure_blocks, output is CORNER, DATA, LOAD, or SAVE.
        // For sculk_sensors, output is ACTIVE, COOLDOWN, or INACTIVE.
        // For daylight_detectors, output is INVERTED or NORMAL.
        // For command_blocks, output is CONDITIONAL or NORMAL.
        // For big_dripleafs, output is FULL, NONE, PARTIAL, or UNSTABLE.
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

    public boolean isBigDripleaf() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_17) && material.getModernData() instanceof BigDripleaf;
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

    /*public SculkSensor getSculkSensor() { // TODO: 1.17
        return (SculkSensor) material.getModernData();
    }

    public BigDripleaf getBigDripleaf() {
        return (BigDripleaf) material.getModernData();
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
        else if (isDaylightDetector()) {
            return getDaylightDetector().isInverted() ? "INVERTED" : "NORMAL";
        }
        else if (isCommandBlock()) {
            return getCommandBlock().isConditional() ? "CONDITIONAL" : "NORMAL";
        }
        else if (isSculkSensor()) {
            return ((SculkSensor) material.getModernData()).getPhase().name(); // TODO: 1.17
        }
        else if (isBigDripleaf()) {
            return ((BigDripleaf) material.getModernData()).getTilt().name(); // TODO: 1.17
        }
        return null; // Unreachable
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
        // For bubble_columns, input is NORMAL or DRAG.
        // For structure_blocks, input is CORNER, DATA, LOAD, or SAVE.
        // For sculk_sensors, input is ACTIVE, COOLDOWN, or INACTIVE.
        // For daylight_detectors, input is INVERTED or NORMAL.
        // For command_blocks, input is CONDITIONAL or NORMAL.
        // For big_dripleafs, input is FULL, NONE, PARTIAL, or UNSTABLE.
        // @tags
        // <MaterialTag.mode>
        // -->
        if (mechanism.matches("mode")) {
            if (isComparator() && mechanism.requireEnum(Comparator.Mode.class)) {
                getComparator().setMode(Comparator.Mode.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isBubbleColumn()) {
                getBubbleColumn().setDrag(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "drag"));
            }
            else if (isPistonHead()) {
                getPistonHead().setShort(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "short"));
            }
            else if (isStructureBlock() && mechanism.requireEnum(StructureBlock.Mode.class)) {
                getStructureBlock().setMode(StructureBlock.Mode.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isDaylightDetector()) {
                getDaylightDetector().setInverted(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "inverted"));
            }
            else if (isCommandBlock()) {
                getCommandBlock().setConditional(CoreUtilities.equalsIgnoreCase(mechanism.getValue().asString(), "conditional"));
            }
            else if (isSculkSensor() || isBigDripleaf()) {
                MultiVersionHelper1_17.materialModeRunMech(mechanism, this);
            }
        }
    }
}
