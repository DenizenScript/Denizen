package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;

public class MaterialMode extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name mode
    // @input ElementTag
    // @description
    // Controls a block's mode.
    // For big_dripleafs, modes are FULL, NONE, PARTIAL, and UNSTABLE.
    // For bubble_columns, modes are NORMAL and DRAG.
    // For command_blocks, modes are CONDITIONAL and NORMAL.
    // For comparators, modes are COMPARE and SUBTRACT.
    // For creaking_hearts, modes are AWAKE, DORMANT, and UPROOTED.
    // For daylight_detectors, modes are INVERTED and NORMAL.
    // For potent_sulfur, modes are CONTINUOUS, DORMANT, DRY, ERUPTING, and WET.
    // For piston_heads, modes are NORMAL and SHORT.
    // For sculk_catalysts, modes are BLOOM and NORMAL.
    // For sculk_sensors, modes are ACTIVE, COOLDOWN, and INACTIVE.
    // For sculk_shriekers, modes are SHRIEKING and NORMAL.
    // For structure_blocks, modes are CORNER, DATA, LOAD, and SAVE.
    // For tripwires, modes are ARMED and DISARMED.
    // For trial_spawners, modes are ACTIVE, COOLDOWN, EJECTING_REWARD, INACTIVE, WAITING_FOR_PLAYERS, and WAITING_FOR_REWARD_EJECTION.
    // For vaults, modes are ACTIVE, EJECTING, INACTIVE, and UNLOCKING.
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof BigDripleaf
                || data instanceof BubbleColumn
                || data instanceof CommandBlock
                || data instanceof Comparator
                || data instanceof DaylightDetector
                || data instanceof PistonHead
                || data instanceof SculkSensor
                || data instanceof StructureBlock
                || data instanceof Tripwire
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && (data instanceof SculkCatalyst
                                                                        || data instanceof SculkShrieker))
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && (data instanceof CreakingHeart
                                                                        || data instanceof TrialSpawner
                                                                        || data instanceof Vault))
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v26_2) && data instanceof PotentSulfur);
    }

    @Override
    public ElementTag getPropertyValue() {
        if (getBlockData() instanceof BigDripleaf bigDripleaf) {
            return new ElementTag(bigDripleaf.getTilt());
        }
        else if (getBlockData() instanceof BubbleColumn bubbleColumn) {
            return new ElementTag(bubbleColumn.isDrag() ? "DRAG" : "NORMAL", true);
        }
        else if (getBlockData() instanceof CommandBlock cmdBlock) {
            return new ElementTag(cmdBlock.isConditional() ? "CONDITIONAL" : "NORMAL", true);
        }
        else if (getBlockData() instanceof Comparator comparator) {
            return new ElementTag(comparator.getMode());
        }
        else if (getBlockData() instanceof DaylightDetector daylightDetector) {
            return new ElementTag(daylightDetector.isInverted() ? "INVERTED" : "NORMAL", true);
        }
        else if (getBlockData() instanceof PistonHead pistonHead) {
            return new ElementTag(pistonHead.isShort() ? "SHORT" : "NORMAL", true);
        }
        else if (getBlockData() instanceof SculkSensor sculkSensor) {
            return new ElementTag(sculkSensor.getPhase());
        }
        else if (getBlockData() instanceof StructureBlock structureBlock) {
            return new ElementTag(structureBlock.getMode());
        }
        else if (getBlockData() instanceof Tripwire tripwire) {
            return new ElementTag(tripwire.isDisarmed() ? "DISARMED" : "ARMED", true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof SculkCatalyst sculkCatalyst) {
            return new ElementTag(sculkCatalyst.isBloom() ? "BLOOM" : "NORMAL", true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof SculkShrieker sculkShrieker) {
            return new ElementTag(sculkShrieker.isShrieking() ? "SHRIEKING" : "NORMAL", true);
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof CreakingHeart creakingHeart) {
            return new ElementTag(creakingHeart.getCreakingHeartState().name(), true); // TODO: once 1.21 is the minimum supported version, use the enum constructor
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof TrialSpawner trialSpawner) {
            return new ElementTag(trialSpawner.getTrialSpawnerState().name(), true); // TODO: once 1.21 is the minimum supported version, use the enum constructor
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof Vault vault) {
            return new ElementTag(vault.getVaultState().name(), true); // TODO: once 1.21 is the minimum supported version, use the enum constructor
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v26_2) && getBlockData() instanceof PotentSulfur potentSulfur) {
            return new ElementTag(potentSulfur.getPotentSulfurState().name(), true); // TODO: once 26.2 is the minimum supported version, use the enum constructor
        }
        return null;
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (getBlockData() instanceof BigDripleaf bigDripleaf) {
            if (mechanism.requireEnum(BigDripleaf.Tilt.class)) {
                bigDripleaf.setTilt(value.asEnum(BigDripleaf.Tilt.class));
            }
        }
        else if (getBlockData() instanceof BubbleColumn bubbleColumn) {
            bubbleColumn.setDrag(value.asLowerString().equals("drag"));
        }
        else if (getBlockData() instanceof CommandBlock cmdBlock) {
            cmdBlock.setConditional(value.asLowerString().equals("conditional"));
        }
        else if (getBlockData() instanceof Comparator comparator) {
            if (mechanism.requireEnum(Comparator.Mode.class)) {
                comparator.setMode(value.asEnum(Comparator.Mode.class));
            }
        }
        else if (getBlockData() instanceof DaylightDetector daylightDetector) {
            daylightDetector.setInverted(value.asLowerString().equals("inverted"));
        }
        else if (getBlockData() instanceof PistonHead pistonHead) {
            pistonHead.setShort(value.asLowerString().equals("short"));
        }
        else if (getBlockData() instanceof SculkSensor sculkSensor) {
            if (mechanism.requireEnum(SculkSensor.Phase.class)) {
                sculkSensor.setPhase(value.asEnum(SculkSensor.Phase.class));
            }
        }
        else if (getBlockData() instanceof StructureBlock structureBlock) {
            if (mechanism.requireEnum(StructureBlock.Mode.class)) {
                structureBlock.setMode(value.asEnum(StructureBlock.Mode.class));
            }
        }
        else if (getBlockData() instanceof Tripwire tripwire) {
            tripwire.setDisarmed(value.asLowerString().equals("disarmed"));
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof SculkCatalyst sculkCatalyst) {
            sculkCatalyst.setBloom(value.asLowerString().equals("bloom"));
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof SculkShrieker sculkShrieker) {
            sculkShrieker.setShrieking(value.asLowerString().equals("shrieking"));
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof CreakingHeart creakingHeart) {
            if (mechanism.requireEnum(CreakingHeart.State.class)) {
                creakingHeart.setCreakingHeartState(value.asEnum(CreakingHeart.State.class));
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof TrialSpawner trialSpawner) {
            if (mechanism.requireEnum(TrialSpawner.State.class)) {
                trialSpawner.setTrialSpawnerState(value.asEnum(TrialSpawner.State.class));
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_21) && getBlockData() instanceof Vault vault) {
            if (mechanism.requireEnum(Vault.State.class)) {
                vault.setVaultState(value.asEnum(Vault.State.class));
            }
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v26_2) && getBlockData() instanceof PotentSulfur potentSulfur) {
            if (mechanism.requireEnum(PotentSulfur.State.class)) {
                potentSulfur.setPotentSulfurState(value.asEnum(PotentSulfur.State.class));
            }
        }
    }

    @Override
    public String getPropertyId() {
        return "mode";
    }

    public static void register() {
        autoRegister("mode", MaterialMode.class, ElementTag.class, false);
    }
}
