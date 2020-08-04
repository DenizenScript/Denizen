package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.Openable;
import org.bukkit.block.data.type.DaylightDetector;
import org.bukkit.block.data.type.Dispenser;

public class MaterialSwitchable  implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && (((MaterialTag) material).getModernData().data instanceof Powerable
                || ((MaterialTag) material).getModernData().data instanceof Openable
                || ((MaterialTag) material).getModernData().data instanceof Dispenser
                || ((MaterialTag) material).getModernData().data instanceof DaylightDetector);
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

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.switched>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.switched
        // @group properties
        // @description
        // Returns whether a powerable material (like pressure plates), an openable material (like doors), a dispenser, or a daylight sensor is switched.
        // -->
        PropertyParser.<MaterialSwitchable>registerTag("switched", (attribute, material) -> {
            return new ElementTag(material.getState());
        });
    }

    public boolean isPowerable() {
        return material.getModernData().data instanceof Powerable;
    }

    public Powerable getPowerable() {
        return (Powerable) material.getModernData().data;
    }

    public boolean isOpenable() {
        return material.getModernData().data instanceof Openable;
    }

    public boolean isDisepnser() {
        return material.getModernData().data instanceof Dispenser;
    }

    public Openable getOpenable() {
        return (Openable) material.getModernData().data;
    }

    public Dispenser getDispenser() {
        return (Dispenser) material.getModernData().data;
    }

    public DaylightDetector getDaylightDetector() {
        return (DaylightDetector) material.getModernData().data;
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
        else {
            return getDaylightDetector().isInverted();
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
        else {
            getDaylightDetector().setInverted(state);
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
        // Sets whether a powerable material (like pressure plates), an openable material (like doors), a dispenser, or a daylight sensor is switched.
        // @tags
        // <MaterialTag.switched>
        // -->
        if (mechanism.matches("switched") && mechanism.requireBoolean()) {
            setState(mechanism.getValue().asBoolean());
        }
    }
}
