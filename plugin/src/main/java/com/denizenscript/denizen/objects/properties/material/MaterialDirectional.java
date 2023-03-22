package com.denizenscript.denizen.objects.properties.material;

import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.Mechanism;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.Property;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import org.bukkit.Axis;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.PointedDripstone;
import org.bukkit.block.data.type.Jigsaw;
import org.bukkit.util.Vector;

public class MaterialDirectional implements Property {

    public static boolean describes(ObjectTag material) {
        if (!(material instanceof MaterialTag)) {
            return false;
        }
        MaterialTag mat = (MaterialTag) material;
        if (!mat.hasModernData()) {
            return false;
        }
        BlockData data = mat.getModernData();
        return data instanceof Directional
                || data instanceof Orientable
                || data instanceof Rotatable
                || data instanceof Rail
                || data instanceof Jigsaw
                || data instanceof PointedDripstone;
    }

    public static MaterialDirectional getFrom(ObjectTag _material) {
        if (!describes(_material)) {
            return null;
        }
        else {
            return new MaterialDirectional((MaterialTag) _material);
        }
    }

    public static final String[] handledMechs = new String[] {
            "direction"
    };

    public MaterialDirectional(MaterialTag _material) {
        material = _material;
    }

    public MaterialTag material;

    public static BlockFace[] rotatableValidFaces = new BlockFace[] {
            BlockFace.SOUTH, BlockFace.SOUTH_SOUTH_WEST, BlockFace.SOUTH_WEST, BlockFace.WEST_SOUTH_WEST, BlockFace.WEST,
            BlockFace.WEST_NORTH_WEST, BlockFace.NORTH_WEST, BlockFace.NORTH_NORTH_WEST, BlockFace.NORTH, BlockFace.NORTH_NORTH_EAST,
            BlockFace.NORTH_EAST, BlockFace.EAST_NORTH_EAST, BlockFace.EAST, BlockFace.EAST_SOUTH_EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH_SOUTH_EAST
    };

    public static void register() {

        // <--[tag]
        // @attribute <MaterialTag.valid_directions>
        // @returns ListTag
        // @mechanism MaterialTag.direction
        // @group properties
        // @description
        // Returns a list of directions that are valid for a directional material.
        // See also <@link tag MaterialTag.direction>
        // -->
        PropertyParser.registerStaticTag(MaterialDirectional.class, ListTag.class, "valid_directions", (attribute, material) -> {
            ListTag toReturn = new ListTag();
            if (material.isOrientable()) {
                for (Axis axis : material.getOrientable().getAxes()) {
                    toReturn.add(axis.name());
                }
            }
            else if (material.isRail()) {
                for (Rail.Shape shape : material.getRail().getShapes()) {
                    toReturn.add(shape.name());
                }
            }
            else if (material.isDirectional()) {
                for (BlockFace face : material.getDirectional().getFaces()) {
                    toReturn.add(face.name());
                }
            }
            else if (material.isRotatable()) {
                for (BlockFace face : rotatableValidFaces) {
                    toReturn.add(face.name());
                }
            }
            else if (material.isDripstone()) {
                for (BlockFace face : material.getDripstone().getVerticalDirections()) {
                    toReturn.add(face.name());
                }
            }
            else if (material.isJigsaw()) {
                for (Jigsaw.Orientation orientation : Jigsaw.Orientation.values()) {
                    toReturn.add(orientation.name());
                }
            }
            else { // Unreachable
                return null;
            }
            return toReturn;
        });

        // <--[tag]
        // @attribute <MaterialTag.direction>
        // @returns ElementTag
        // @mechanism MaterialTag.direction
        // @group properties
        // @description
        // Returns the current facing direction for a directional material (like a door or a bed).
        // This includes materials that Spigot classifies as "directional", "orientable", or "rotatable", as well as rails, dripstone, and jigsaw blocks.
        // Output is a direction name like "NORTH", or an axis like "X", or a rail direction like "ASCENDING_NORTH".
        // -->
        PropertyParser.registerStaticTag(MaterialDirectional.class, ElementTag.class, "direction", (attribute, material) -> {
            return new ElementTag(material.getDirectionName());
        });
    }

    public Vector getDirectionVector() {
        if (isOrientable()) {
            switch (getOrientable().getAxis()) {
                case X:
                    return new Vector(1, 0, 0);
                case Y:
                    return new Vector(0, 1, 0);
                default:
                    return new Vector(0, 0, 1);
            }
        }
        else if (isRotatable()) {
            return getRotatable().getRotation().getDirection();
        }
        else if (isRail()) {
            switch (getRail().getShape()) {
                case ASCENDING_EAST:
                    return new Vector(1, 1, 0);
                case ASCENDING_NORTH:
                    return new Vector(0, 1, -1);
                case ASCENDING_SOUTH:
                    return new Vector(0, 1, 1);
                case ASCENDING_WEST:
                    return new Vector(-1, 1, 0);
                case EAST_WEST:
                    return new Vector(1, 0, 0);
                case NORTH_EAST:
                    return new Vector(1, 0, -1);
                case NORTH_SOUTH:
                    return new Vector(0, 0, 1);
                case NORTH_WEST:
                    return new Vector(-1, 0, -1);
                case SOUTH_EAST:
                    return new Vector(1, 0, 1);
                case SOUTH_WEST:
                    return new Vector(-1, 0, 1);
            }
            return null; // Unreachable.
        }
        else if (isDirectional()) {
            return getDirectional().getFacing().getDirection();
        }
        else if (isDripstone()) {
            return getDripstone().getVerticalDirection().getDirection();
        }
        else if (isJigsaw()) {
            switch (getJigsaw().getOrientation()) {
                case DOWN_EAST:
                    return new Vector(1, -1, 0);
                case DOWN_NORTH:
                    return new Vector(0, -1, -1);
                case DOWN_SOUTH:
                    return new Vector(0, -1, 1);
                case DOWN_WEST:
                    return new Vector(-1, -1, 0);
                case EAST_UP:
                case UP_EAST:
                    return new Vector(1, 1, 0);
                case NORTH_UP:
                case UP_NORTH:
                    return new Vector(0, 1, -1);
                case SOUTH_UP:
                case UP_SOUTH:
                    return new Vector(0, 1, 1);
                case WEST_UP:
                case UP_WEST:
                    return new Vector(-1, 1, 0);
            }
        }
        return null; // Unreachable.
    }

    public String getDirectionName() {
        if (isOrientable()) {
            return getOrientable().getAxis().name();
        }
        else if (isRotatable()) {
            return getRotatable().getRotation().name();
        }
        else if (isRail()) {
            return getRail().getShape().name();
        }
        else if (isDirectional()) {
            return getDirectional().getFacing().name();
        }
        else if (isDripstone()) {
            return getDripstone().getVerticalDirection().name();
        }
        else if (isJigsaw()) {
            return getJigsaw().getOrientation().name();
        }
        return null; // Unreachable
    }

    public boolean isOrientable() {
        return material.getModernData() instanceof Orientable;
    }

    public boolean isRotatable() {
        return material.getModernData() instanceof Rotatable;
    }

    public boolean isDirectional() {
        return material.getModernData() instanceof Directional;
    }

    public boolean isDripstone() {
        return material.getModernData() instanceof PointedDripstone;
    }

    public boolean isRail() {
        return material.getModernData() instanceof Rail;
    }

    public boolean isJigsaw() {
        return material.getModernData() instanceof Jigsaw;
    }

    public Orientable getOrientable() {
        return (Orientable) material.getModernData();
    }

    public Rotatable getRotatable() {
        return (Rotatable) material.getModernData();
    }

    public Directional getDirectional() {
        return (Directional) material.getModernData();
    }

    public Jigsaw getJigsaw() {
        return (Jigsaw) material.getModernData();
    }

    public PointedDripstone getDripstone() {
        return (PointedDripstone) material.getModernData();
    }

    public Rail getRail() {
        return (Rail) material.getModernData();
    }

    public void setFacing(BlockFace face) {
        if (isOrientable()) {
            Axis axis;
            Vector vec = face.getDirection();
            if (Math.abs(vec.getX()) >= 0.5) {
                axis = Axis.X;
            }
            else if (Math.abs(vec.getY()) >= 0.5) {
                axis = Axis.Y;
            }
            else {
                axis = Axis.Z;
            }
            getOrientable().setAxis(axis);
        }
        else if (isRotatable()) {
            getRotatable().setRotation(face);
        }
        else if (isRail()) {
            switch (face) {
                case EAST:
                case WEST:
                    getRail().setShape(Rail.Shape.EAST_WEST);
                case NORTH:
                case SOUTH:
                    getRail().setShape(Rail.Shape.NORTH_SOUTH);
                case NORTH_EAST:
                    getRail().setShape(Rail.Shape.NORTH_EAST);
                case NORTH_WEST:
                    getRail().setShape(Rail.Shape.NORTH_WEST);
                case SOUTH_EAST:
                    getRail().setShape(Rail.Shape.SOUTH_EAST);
                case SOUTH_WEST:
                    getRail().setShape(Rail.Shape.SOUTH_WEST);
                default:
                    Debug.echoError("Unsupported rail direction '" + face + "'.");
            }
        }
        else if (isDirectional()) {
            getDirectional().setFacing(face);
        }
        else if (isDripstone()) {
            getDripstone().setVerticalDirection(face);
        }
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
            if (isOrientable() && mechanism.requireEnum(Axis.class)) {
                getOrientable().setAxis(Axis.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isRail() && mechanism.requireEnum(Rail.Shape.class)) {
                getRail().setShape(Rail.Shape.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (isJigsaw() && mechanism.requireEnum(Jigsaw.Orientation.class)) {
                getJigsaw().setOrientation(Jigsaw.Orientation.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else if (!isJigsaw() && mechanism.requireEnum(BlockFace.class)) {
                setFacing(BlockFace.valueOf(mechanism.getValue().asString().toUpperCase()));
            }
            else {
                mechanism.echoError("MaterialTag.Direction mechanism has bad input: directional value '" + mechanism.getValue().asString() + "' is invalid.");
            }
        }
    }
}
