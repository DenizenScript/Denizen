package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

public class MaterialMode extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name mode
    // @input ElementTag
    // @description
    // Controls a block's mode.
    // Valid values vary by block type.
    // For comparators, either COMPARE or SUBTRACT.
    // For piston_heads, either NORMAL or SHORT.
    // For bubble_columns, either NORMAL or DRAG.
    // For structure_blocks, either CORNER, DATA, LOAD, or SAVE.
    // For sculk_sensors, either ACTIVE, COOLDOWN, or INACTIVE.
    // For daylight_detectors, either INVERTED or NORMAL.
    // For command_blocks, either CONDITIONAL or NORMAL.
    // For big_dripleafs, either FULL, NONE, PARTIAL, or UNSTABLE.
    // For sculk_catalysts, either BLOOM or NORMAL.
    // For sculk_shriekers, either SHRIEKING or NORMAL.
    // For tripwires, either ARMED or DISARMED.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Comparator
                || data instanceof PistonHead
                || data instanceof BubbleColumn
                || data instanceof StructureBlock
                || data instanceof DaylightDetector
                || data instanceof CommandBlock
                || data instanceof SculkSensor
                || data instanceof BigDripleaf
                || data instanceof Tripwire
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && (data instanceof SculkCatalyst
                                                                        || data instanceof SculkShrieker));
    }

    public MaterialTag material;

    @Override
    public String getPropertyId() {
        return "mode";
    }

    @Override
    public ElementTag getPropertyValue() {
        if (isComparator()) {
            return new ElementTag(getComparator().getMode().name());
        }
        else if (isBubbleColumn()) {
            return new ElementTag(getBubbleColumn().isDrag() ? "DRAG" : "NORMAL");
        }
        else if (isPistonHead()) {
            return new ElementTag(getPistonHead().isShort() ? "SHORT" : "NORMAL");
        }
        else if (isStructureBlock()) {
            return new ElementTag(getStructureBlock().getMode().name());
        }
        else if (isDaylightDetector()) {
            return new ElementTag(getDaylightDetector().isInverted() ? "INVERTED" : "NORMAL");
        }
        else if (isCommandBlock()) {
            return new ElementTag(getCommandBlock().isConditional() ? "CONDITIONAL" : "NORMAL");
        }
        else if (isSculkSensor()) {
            return new ElementTag(getSculkSensor().getPhase().name());
        }
        else if (isBigDripleaf()) {
            return new ElementTag(getBigDripleaf().getTilt().name());
        }
        else if (isTripwire()) {
            return new ElementTag(getTripwire().isDisarmed() ? "DISARMED" : "ARMED");
        }
        else if (isSculkCatalyst()) {
            return new ElementTag(((SculkCatalyst) material.getModernData()).isBloom() ? "BLOOM" : "NORMAL"); // TODO: 1.19
        }
        else if (isSculkShrieker()) {
            return new ElementTag(((SculkShrieker) material.getModernData()).isShrieking() ? "SHRIEKING" : "NORMAL"); // TODO: 1.19
        }
        return null; // Unreachable
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (isComparator() && mechanism.requireEnum(Comparator.Mode.class)) {
            getComparator().setMode(Comparator.Mode.valueOf(value.asString().toUpperCase()));
        }
        else if (isBubbleColumn()) {
            getBubbleColumn().setDrag(CoreUtilities.equalsIgnoreCase(value.asString(), "drag"));
        }
        else if (isPistonHead()) {
            getPistonHead().setShort(CoreUtilities.equalsIgnoreCase(value.asString(), "short"));
        }
        else if (isStructureBlock() && mechanism.requireEnum(StructureBlock.Mode.class)) {
            getStructureBlock().setMode(StructureBlock.Mode.valueOf(value.asString().toUpperCase()));
        }
        else if (isDaylightDetector()) {
            getDaylightDetector().setInverted(CoreUtilities.equalsIgnoreCase(value.asString(), "inverted"));
        }
        else if (isCommandBlock()) {
            getCommandBlock().setConditional(CoreUtilities.equalsIgnoreCase(value.asString(), "conditional"));
        }
        else if (isSculkSensor() && mechanism.requireEnum(SculkSensor.Phase.class)) {
            getSculkSensor().setPhase(SculkSensor.Phase.valueOf(value.asString().toUpperCase()));
        }
        else if (isBigDripleaf() && mechanism.requireEnum(BigDripleaf.Tilt.class)) {
            getBigDripleaf().setTilt(BigDripleaf.Tilt.valueOf(value.asString().toUpperCase()));
        }
        else if (isTripwire()) {
            getTripwire().setDisarmed(CoreUtilities.equalsIgnoreCase(value.asString(), "disarmed"));
        }
        else if (isSculkCatalyst()) {
            ((SculkCatalyst) material.getModernData()).setBloom(CoreUtilities.equalsIgnoreCase(value.asString(), "bloom")); // TODO: 1.19
        }
        else if (isSculkShrieker()) {
            ((SculkShrieker) material.getModernData()).setShrieking(CoreUtilities.equalsIgnoreCase(value.asString(), "shrieking")); // TODO: 1.19
        }
    }

    public static void register() {
        autoRegister("mode", MaterialMode.class, ElementTag.class, true);
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

    public boolean isDaylightDetector() {
        return material.getModernData() instanceof DaylightDetector;
    }

    public boolean isCommandBlock() {
        return material.getModernData() instanceof CommandBlock;
    }

    public boolean isSculkSensor() {
        return material.getModernData() instanceof SculkSensor;
    }

    public boolean isBigDripleaf() {
        return material.getModernData() instanceof BigDripleaf;
    }

    public boolean isTripwire() {
        return material.getModernData() instanceof Tripwire;
    }

    public boolean isSculkCatalyst() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && material.getModernData() instanceof SculkCatalyst;
    }

    public boolean isSculkShrieker() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && material.getModernData() instanceof SculkShrieker;
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

    public SculkSensor getSculkSensor() {
        return (SculkSensor) material.getModernData();
    }

    public BigDripleaf getBigDripleaf() {
        return (BigDripleaf) material.getModernData();
    }

    public Tripwire getTripwire() {
        return (Tripwire) material.getModernData();
    }

    /*public SculkCatalyst getSculkCatalyst() { // TODO: 1.19
        return (SculkCatalyst) material.getModernData();
    }

    public SculkShrieker getSculkShrieker() {
        return (SculkShrieker) material.getModernData();
    }*/

}
