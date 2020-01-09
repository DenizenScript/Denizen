package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;

public class MaterialSwitchFace implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof MaterialTag
                && ((MaterialTag) material).hasModernData()
                && ((MaterialTag) material).getModernData().data instanceof Switch;
    }

    public static MaterialSwitchFace getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialSwitchFace((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "switch_face"
    };

    private MaterialSwitchFace(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    public static void registerTags() {

        // <--[tag]
        // @attribute <MaterialTag.switch_face>
        // @returns ElementTag
        // @mechanism MaterialTag.switch_face
        // @group properties
        // @description
        // Returns the current attach direction for a switch.
        // Output is "CEILING", "FLOOR", or "WALL".
        // -->
        PropertyParser.<MaterialSwitchFace>registerTag("switch_face", (attribute, material) -> {
            return new ElementTag(material.getSwitch().getFace().name());
        });
    }

    public Switch getSwitch() {
        return (Switch) material.getModernData().data;
    }

    @Override
    public String getPropertyString() {
        return getSwitch().getFace().name();
    }

    public BlockFace getAttachedTo() {
        switch (getSwitch().getFace()) {
            case WALL:
                return getSwitch().getFacing().getOppositeFace();
            case FLOOR:
                return BlockFace.DOWN;
            case CEILING:
                return BlockFace.UP;
            default:
                return BlockFace.SELF;
        }
    }

    @Override
    public String getPropertyId() {
        return "switch_face";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name switch_face
        // @input Element
        // @description
        // Sets the current attach direction for a switch.
        // @tags
        // <MaterialTag.switch_face>
        // -->
        if (mechanism.matches("switch_face") && mechanism.requireEnum(false, Switch.Face.values())) {
            getSwitch().setFace(Switch.Face.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
