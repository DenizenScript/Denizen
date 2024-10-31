package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ElementTag;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.*;

public class MaterialSwitchable extends MaterialProperty<ElementTag> {

    // <--[property]
    // @object MaterialTag
    // @name switched
    // @input ElementTag(Boolean)
    // @synonyms MaterialTag.lit, MaterialTag.open, MaterialTag.active
    // @description
    // Returns whether a material is 'switched on', which has different semantic meaning depending on the material type.
    // More specifically, this returns whether:
    // - a Powerable material (like pressure plates) is activated
    // - an Openable material (like doors) is open
    // - a dispenser is powered and should dispense its contents
    // - a daylight sensor is inverted (detects darkness instead of light)
    // - a lightable block is lit
    // - a piston block is extended
    // - an end portal frame has an ender eye in it
    // - a hopper is NOT being powered by redstone
    // - a sculk_shrieker can summon a warden
    // -->

    public static boolean describes(MaterialTag material) {
        BlockData data = material.getModernData();
        return data instanceof Powerable
                || data instanceof Openable
                || data instanceof Dispenser
                || data instanceof DaylightDetector
                || data instanceof Piston
                || data instanceof Lightable
                || data instanceof EndPortalFrame
                || data instanceof Hopper
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && data instanceof SculkShrieker);
    }

    public MaterialTag material;

    public static void register() {
        autoRegister("switched", MaterialSwitchable.class, ElementTag.class, true);
    }

    public boolean isPowerable() {
        return material.getModernData() instanceof Powerable;
    }

    public boolean isOpenable() {
        return material.getModernData() instanceof Openable;
    }

    public boolean isDisepnser() {
        return material.getModernData() instanceof Dispenser;
    }

    public boolean isDaylightDetector() {
        return material.getModernData() instanceof DaylightDetector;
    }

    public boolean isLightable() {
        return material.getModernData() instanceof Lightable;
    }

    public boolean isPiston() {
        return material.getModernData() instanceof Piston;
    }

    public boolean isEndFrame() {
        return material.getModernData() instanceof EndPortalFrame;
    }

    public boolean isHopper() {
        return material.getModernData() instanceof Hopper;
    }

    public boolean isSculkShrieker() {
        return NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && material.getModernData() instanceof SculkShrieker;
    }

    public Powerable getPowerable() {
        return (Powerable) material.getModernData();
    }

    public Openable getOpenable() {
        return (Openable) material.getModernData();
    }

    public Dispenser getDispenser() {
        return (Dispenser) material.getModernData();
    }

    public DaylightDetector getDaylightDetector() {
        return (DaylightDetector) material.getModernData();
    }

    public Piston getPiston() {
        return (Piston) material.getModernData();
    }

    public Lightable getLightable() {
        return (Lightable) material.getModernData();
    }

    public EndPortalFrame getEndFrame() {
        return (EndPortalFrame) material.getModernData();
    }

    public Hopper getHopper() {
        return (Hopper) material.getModernData();
    }

    /*public SculkShrieker getSculkShrieker() { // TODO: 1.19
        return (SculkShrieker) material.getModernData();
    }*/

    public boolean getState() {
        if (isOpenable()) {
            return getOpenable().isOpen();
        }
        else if (isPowerable()) {
            return getPowerable().isPowered();
        }
        else if (isDisepnser()) {
            return getDispenser().isTriggered();
        }
        else if (isDaylightDetector()) {
            return getDaylightDetector().isInverted();
        }
        else if (isLightable()) {
            return getLightable().isLit();
        }
        else if (isPiston()) {
            return getPiston().isExtended();
        }
        else if (isEndFrame()) {
            return getEndFrame().hasEye();
        }
        else if (isHopper()) {
            return getHopper().isEnabled();
        }
        else if (isSculkShrieker()) {
            return ((SculkShrieker) material.getModernData()).isCanSummon();
        }
        return false; // Unreachable
    }

    public void setState(boolean state) {
        if (isOpenable()) {
            getOpenable().setOpen(state);
        }
        else if (isPowerable()) {
            getPowerable().setPowered(state);
        }
        else if (isDisepnser()) {
            getDispenser().setTriggered(state);
        }
        else if (isDaylightDetector()) {
            getDaylightDetector().setInverted(state);
        }
        else if (isLightable()) {
            getLightable().setLit(state);
        }
        else if (isPiston()) {
            getPiston().setExtended(state);
        }
        else if (isEndFrame()) {
            getEndFrame().setEye(state);
        }
        else if (isHopper()) {
            getHopper().setEnabled(state);
        }
        else if (isSculkShrieker()) {
            ((SculkShrieker) material.getModernData()).setCanSummon(state);
        }
    }

    @Override
    public ElementTag getPropertyValue() {
        return new ElementTag(getState());
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (!mechanism.requireBoolean()) {
            return;
        }
        setState(value.asBoolean());
    }

    @Override
    public String getPropertyId() {
        return "switched";
    }
}
