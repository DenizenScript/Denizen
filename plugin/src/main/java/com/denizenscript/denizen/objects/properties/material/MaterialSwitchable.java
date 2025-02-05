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
    // Controls whether a material is 'switched on', which has different semantic meaning depending on the material type.
    // More specifically, this controls whether:
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

    @Override
    public ElementTag getPropertyValue() {
        if (getBlockData() instanceof Openable openable) {
            return new ElementTag(openable.isOpen());
        }
        else if (getBlockData() instanceof Lightable lightable) {
            return new ElementTag(lightable.isLit());
        }
        else if (getBlockData() instanceof Powerable powerable) {
            return new ElementTag(powerable.isPowered());
        }
        else if (getBlockData() instanceof Dispenser dispenser) {
            return new ElementTag(dispenser.isTriggered());
        }
        else if (getBlockData() instanceof DaylightDetector daylightDetector) {
            return new ElementTag(daylightDetector.isInverted());
        }
        else if (getBlockData() instanceof Piston piston) {
            return new ElementTag(piston.isExtended());
        }
        else if (getBlockData() instanceof EndPortalFrame endPortalFrame) {
            return new ElementTag(endPortalFrame.hasEye());
        }
        else if (getBlockData() instanceof Hopper hopper) {
            return new ElementTag(hopper.isEnabled());
        }
        else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof SculkShrieker sculkShrieker) {
            return new ElementTag(sculkShrieker.isCanSummon());
        }
        return null;
    }

    @Override
    public String getPropertyId() {
        return "switched";
    }

    @Override
    public void setPropertyValue(ElementTag value, Mechanism mechanism) {
        if (mechanism.requireBoolean()) {
            if (getBlockData() instanceof Openable openable) {
                openable.setOpen(value.asBoolean());
            }
            else if (getBlockData() instanceof Lightable lightable) {
                lightable.setLit(value.asBoolean());
            }
            else if (getBlockData() instanceof Powerable powerable) {
                powerable.setPowered(value.asBoolean());
            }
            else if (getBlockData() instanceof Dispenser dispenser) {
                dispenser.setTriggered(value.asBoolean());
            }
            else if (getBlockData() instanceof DaylightDetector daylightDetector) {
                daylightDetector.setInverted(value.asBoolean());
            }
            else if (getBlockData() instanceof Piston piston) {
                piston.setExtended(value.asBoolean());
            }
            else if (getBlockData() instanceof EndPortalFrame endPortalFrame) {
                endPortalFrame.setEye(value.asBoolean());
            }
            else if (getBlockData() instanceof Hopper hopper) {
                hopper.setEnabled(value.asBoolean());
            }
            else if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_19) && getBlockData() instanceof SculkShrieker sculkShrieker) {
                sculkShrieker.setCanSummon(value.asBoolean());
            }
        }
    }

    public static void register() {
        autoRegister("switched", MaterialSwitchable.class, ElementTag.class, true);
    }
}
