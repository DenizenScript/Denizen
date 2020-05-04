package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.data.type.Repeater;

public class MaterialRepeaterLock implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Repeater;
    }

    public static MaterialRepeaterLock getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialRepeaterLock((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "is_locked"
    };

    private MaterialRepeaterLock(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.is_locked>
        // @returns ElementTag(Boolean)
        // @mechanism MaterialTag.is_locked
        // @group properties
        // @description
        // Returns whether this redstone repeater material is locked.
        // -->
        PropertyParser.<MaterialRepeaterLock>registerTag("is_locked", (attribute, material) -> {
            return new ElementTag(material.isLocked());
        });
    }

    public Repeater getLockable() {
        return (Repeater) material.getModernData().data;
    }

    public boolean isLocked() {
        return getLockable().isLocked();
    }

    @Override
    public String getPropertyString() {
        return String.valueOf(isLocked());
    }

    @Override
    public String getPropertyId() {
        return "is_locked";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name is_locked
        // @input ElementTag(Boolean)
        // @description
        // Sets this redstone repeater material to be locked.
        // @tags
        // <MaterialTag.is_locked>
        // -->
        if (mechanism.matches("is_locked") && mechanism.requireBoolean()) {
            getLockable().setLocked(mechanism.getValue().asBoolean());
        }
    }
}
