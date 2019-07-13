package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.dMaterial;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Switch;

public class MaterialSwitchFace implements Property {

    public static boolean describes(ObjectTag material) {
        return material instanceof dMaterial
                && ((dMaterial) material).hasModernData()
                && ((dMaterial) material).getModernData().data instanceof Switch;
    }

    public static MaterialSwitchFace getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialSwitchFace((dMaterial) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "switch_face"
    };

    public static final String[] handledMechs = new String[] {
            "switch_face"
    };


    private MaterialSwitchFace(dMaterial _material) {
        material = _material;
    }

    dMaterial material;

    @Override
    public String getAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <m@material.switch_face>
        // @returns ElementTag
        // @mechanism dMaterial.switch_face
        // @group properties
        // @description
        // Returns the current attach direction for a switch.
        // Output is "CEILING", "FLOOR", or "WALL".
        // -->
        if (attribute.startsWith("switch_face")) {
            return new ElementTag(getSwitch().getFace().name()).getAttribute(attribute.fulfill(1));
        }

        return null;
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
        // @object dMaterial
        // @name switch_face
        // @input Element
        // @description
        // Sets the current attach direction for a switch.
        // @tags
        // <m@material.switch_face>
        // -->
        if (mechanism.matches("switch_face") && mechanism.requireEnum(false, Switch.Face.values())) {
            getSwitch().setFace(Switch.Face.valueOf(mechanism.getValue().asString().toUpperCase()));
        }
    }
}
