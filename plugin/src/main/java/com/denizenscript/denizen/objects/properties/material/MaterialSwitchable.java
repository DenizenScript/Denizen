package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Lightable;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.DaylightDetector;
import org.bukkit.block.data.type.Dispenser;
import org.bukkit.block.data.type.Piston;
import org.bukkit.block.data.type.EndPortalFrame;

public class MaterialSwitchable implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag)) {
            return false;
        }
        MaterialTag mat = (MaterialTag) material;
        if (!mat.hasModernData()) {
            return false;
        }
        BlockData data = mat.getModernData();
        return data instanceof Powerable
                || data instanceof Openable
                || data instanceof Dispenser
                || data instanceof DaylightDetector
                || data instanceof Piston
                || data instanceof Lightable
                || data instanceof EndPortalFrame;
    }

    public static MaterialSwitchable getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialSwitchable((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "switched"
    };

    private MaterialSwitchable(MaterialTag _material) {
        material = _material;
    }

    public MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.switched>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.switched
        // @synonyms MaterialTag.lit, MaterialTag.open, MaterialTag.active
        // @group properties
        // @description
        // Returns whether a Powerable material (like pressure plates) an Openable material (like doors), a dispenser, a daylight sensor, a lightable block, or a piston is switched,
        // Or whether an end portal frame has an ender eye.
        // -->
        PropertyParser.<MaterialSwitchable, ElementTag>registerStaticTag(ElementTag.class, "switched", (attribute, material) -> {
            return new ElementTag(material.getState());
        });
    }

    public boolean isPowerable() {
        return material.getModernData() instanceof Powerable;
    }

    public Powerable getPowerable() {
        return (Powerable) material.getModernData();
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
        } else {
            return getEndFrame().hasEye();
        }
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
        } else {
            getEndFrame().setEye(state);
        }
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(getState());
    }

    @Override
    public String getPropertyId() {
        return "switched";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name switched
        // @input ElementTag(Boolean)
        // @description
        // Sets whether a Powerable material (like pressure plates) an Openable material (like doors), a dispenser, a daylight sensor, a lightable block, or a piston is switched,
        // Or whether an end portal frame has an ender eye.
        // @tags
        // <MaterialTag.switched>
        // -->
        if (mechanism.matches("switched") && mechanism.requireBoolean()) {
            setState(mechanism.getValue().asBoolean());
        }
    }
}
