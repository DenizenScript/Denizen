package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.tags.Attribute;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Orientable;

public class MaterialDirectional implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag)) {
            return false;
        }
        MaterialTag mat = (MaterialTag) material;
        if (!mat.hasModernData()) {
            return false;
        }
        BlockData data = mat.getModernData().data;
        if (!(data instanceof Directional || data instanceof Orientable)) {
            return false;
        }
        return true;
    }

    public static MaterialDirectional getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDirectional((MaterialTag) _material);
        }
    }

    public static final String[] handledTags = new String[] {
            "direction", "valid_directions"
    };

    public static final String[] handledMechs = new String[] {
            "direction"
    };


    private MaterialDirectional(MaterialTag _material) {
        material = _material;
    }

    MaterialTag material;

    @Override
    public ObjectTag getObjectAttribute(Attribute attribute) {

        if (attribute == null) {
            return null;
        }

        // <--[tag]
        // @attribute <MaterialTag.valid_directions>
        // @returns ListTag
        // @mechanism MaterialTag.direction
        // @group properties
        // @description
        // Returns a list of directions that are valid for a directional material.
        // See also <@link tag MaterialTag.direction>
        // -->
        if (attribute.startsWith("valid_directions")) {
            ListTag toReturn = new ListTag();
            if (isOrientable()) {
                for (Axis axis : getOrientable().getAxes()) {
                    toReturn.add(axis.name());
                }
            }
            else {
                for (BlockFace face : getDirectional().getFaces()) {
                    toReturn.add(face.name());
                }
            }
            return toReturn.getObjectAttribute(attribute.fulfill(1));
        }

        // <--[tag]
        // @attribute <MaterialTag.direction>
        // @returns ElementTag
        // @mechanism MaterialTag.direction
        // @group properties
        // @description
        // Returns the current facing direction for a directional material (like a door or a bed).
        // Output is a direction name like "NORTH", or an axis like "X".
        // -->
        if (attribute.startsWith("direction")) {
            return new ElementTag(getDirectionName()).getObjectAttribute(attribute.fulfill(1));
        }

        return null;
    }

    public String getDirectionName() {
        if (isOrientable()) {
            return getOrientable().getAxis().name();
        }
        else {
            return getDirectional().getFacing().name();
        }
    }

    public boolean isOrientable() {
        return material.getModernData().data instanceof Orientable;
    }

    public Orientable getOrientable() {
        return (Orientable) material.getModernData().data;
    }

    public Directional getDirectional() {
        return (Directional) material.getModernData().data;
    }

    public BlockFace getDirection() {
        return getDirectional().getFacing();
    }

    @Override
    public String getPropertyString() {
        return getDirectionName();
    }

    @Override
    public String getPropertyId() {
        return "direction";
    }

    @Override
    public void adjust(Mechanism mechanism) {

        // <--[mechanism]
        // @object MaterialTag
        // @name direction
        // @input ElementTag
        // @description
        // Sets the current facing direction for a directional material (like a door or a bed).
        // @tags
        // <MaterialTag.direction>
        // <MaterialTag.valid_directions>
        // -->
        if (mechanism.matches("direction")) {
            if (isOrientable() && mechanism.requireEnum(false, Axis.values())) {
                getOrientable().setAxis(Axis.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (!isOrientable() && mechanism.requireEnum(false, BlockFace.values())) {
                getDirectional().setFacing(BlockFace.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
        }
    }
}
