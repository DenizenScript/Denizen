package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.util.jnbt.CompoundTag;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizencore.flags.MapTagBasedFlagTracker;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.MapTag;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.text.StringHolder;
import org.bukkit.Axis;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.*;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.block.data.type.Wall;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.Chest;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FullBlockData {

    public static void init() {
        HashSet<Class<? extends BlockData>> classesHandled = new HashSet<>();
        for (Material material : Material.values()) {
            if (!material.isBlock() || material.isLegacy()) {
                continue;
            }
            BlockData data = material.createBlockData();
            if (classesHandled.add(data.getClass())) {
                initBlockDataClass(data);
            }
        }
    }

    /** Trigger a bunch of pointless block data changes, to cause the enum class cache CraftBlockData.ENUM_VALUES to be filled in, thus making it probably safe for async usage. */
    private static void initBlockDataClass(BlockData data) {
        try {
            Class<? extends BlockData> dataClass = data.getClass();
            HashMap<String, Method> setMethods = new HashMap<>();
            for (Method m : dataClass.getMethods()) {
                if (m.getName().startsWith("set") && m.getParameterCount() == 1 && m.getReturnType() == void.class) {
                    setMethods.put(m.getName(), m);
                }
            }
            for (Method m : dataClass.getMethods()) {
                if (m.getName().startsWith("get") && m.getParameterCount() == 0 && m.getReturnType().isEnum()) {
                    Method setter = setMethods.get("set" + m.getName().substring("get".length()));
                    if (setter != null && setter.getParameterTypes()[0] == m.getReturnType()) {
                        setter.setAccessible(true);
                        m.setAccessible(true);
                        Object curVal = m.invoke(data);
                        setter.invoke(data, curVal);
                    }
                }
            }
        }
        catch (Throwable ex) {
            Debug.echoError("Errored while trying to pre-load BlockData class '" + data.getClass().getName() + "'");
            Debug.echoError(ex);
        }
    }

    public FullBlockData(Block block, boolean copyFlags) {
        this(block);
        if (copyFlags) {
            MapTagBasedFlagTracker flagMap = (MapTagBasedFlagTracker) new LocationTag(block.getLocation()).getFlagTracker();
            flags = new MapTag();
            for (String flag : flagMap.listAllFlags()) {
                flags.putObject(flag, flagMap.getRootMap(flag));
            }
            if (flags.isEmpty()) {
                flags = null;
            }
        }
    }

    public FullBlockData(Block block) {
        this(block.getBlockData());
        tileEntityData = NMSHandler.blockHelper.getNbtData(block);
    }

    public FullBlockData(BlockData data) {
        this.data = data;
    }

    public FullBlockData(BlockData data, CompoundTag tileEntityData, MapTag flags) {
        this.data = data;
        this.tileEntityData = tileEntityData;
        this.flags = flags;
    }

    public static BlockFace rotateFaceOne(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.WEST;
            case EAST:
                return BlockFace.NORTH;
            case SOUTH:
                return BlockFace.EAST;
            case WEST:
                return BlockFace.SOUTH;
            case NORTH_EAST:
                return BlockFace.NORTH_WEST;
            case NORTH_WEST:
                return BlockFace.SOUTH_WEST;
            case SOUTH_WEST:
                return BlockFace.SOUTH_EAST;
            case SOUTH_EAST:
                return BlockFace.NORTH_EAST;
            case NORTH_NORTH_EAST:
                return BlockFace.WEST_NORTH_WEST;
            case NORTH_NORTH_WEST:
                return BlockFace.WEST_SOUTH_WEST;
            case SOUTH_SOUTH_WEST:
                return BlockFace.EAST_SOUTH_EAST;
            case SOUTH_SOUTH_EAST:
                return BlockFace.EAST_NORTH_EAST;
            case EAST_NORTH_EAST:
                return BlockFace.NORTH_NORTH_WEST;
            case WEST_NORTH_WEST:
                return BlockFace.SOUTH_SOUTH_WEST;
            case WEST_SOUTH_WEST:
                return BlockFace.SOUTH_SOUTH_EAST;
            case EAST_SOUTH_EAST:
                return BlockFace.NORTH_NORTH_EAST;
        }
        return face;
    }

    public static Rail.Shape rotateRailShapeOne(Rail.Shape shape) {
        switch (shape) {
            case NORTH_SOUTH:
                return Rail.Shape.EAST_WEST;
            case EAST_WEST:
                return Rail.Shape.NORTH_SOUTH;
            case ASCENDING_EAST:
                return Rail.Shape.ASCENDING_NORTH;
            case ASCENDING_WEST:
                return Rail.Shape.ASCENDING_SOUTH;
            case ASCENDING_NORTH:
                return Rail.Shape.ASCENDING_WEST;
            case ASCENDING_SOUTH:
                return Rail.Shape.ASCENDING_EAST;
            case SOUTH_EAST:
                return Rail.Shape.NORTH_EAST;
            case SOUTH_WEST:
                return Rail.Shape.SOUTH_EAST;
            case NORTH_WEST:
                return Rail.Shape.SOUTH_WEST;
            case NORTH_EAST:
                return Rail.Shape.NORTH_WEST;
        }
        return shape;
    }

    public FullBlockData rotateOne() {
        if (data instanceof Orientable) {
            BlockData newData = data.clone();
            switch (((Orientable) data).getAxis()) {
                case X:
                    ((Orientable) newData).setAxis(Axis.Z);
                    break;
                case Z:
                    ((Orientable) newData).setAxis(Axis.X);
                    break;
            }
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Rotatable) {
            BlockData newData = data.clone();
            ((Rotatable) newData).setRotation(rotateFaceOne(((Rotatable) data).getRotation()));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Directional) {
            BlockData newData = data.clone();
            ((Directional) newData).setFacing(rotateFaceOne(((Directional) data).getFacing()));
            return new FullBlockData(newData, tileEntityData, flags);

        }
        else if (data instanceof Rail) {
            BlockData newData = data.clone();
            ((Rail) newData).setShape(rotateRailShapeOne(((Rail) data).getShape()));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof MultipleFacing) {
            MultipleFacing newData = (MultipleFacing) data.clone();
            for (BlockFace face : ((MultipleFacing) data).getFaces()) {
                newData.setFace(face, false);
            }
            for (BlockFace face : ((MultipleFacing) data).getFaces()) {
                newData.setFace(rotateFaceOne(face), true);
            }
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof RedstoneWire) {
            RedstoneWire newData = (RedstoneWire) data.clone();
            newData.setFace(BlockFace.NORTH, ((RedstoneWire) data).getFace(BlockFace.EAST));
            newData.setFace(BlockFace.WEST, ((RedstoneWire) data).getFace(BlockFace.NORTH));
            newData.setFace(BlockFace.EAST, ((RedstoneWire) data).getFace(BlockFace.SOUTH));
            newData.setFace(BlockFace.SOUTH, ((RedstoneWire) data).getFace(BlockFace.WEST));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Wall) {
            Wall newData = (Wall) data.clone();
            newData.setHeight(BlockFace.NORTH, ((Wall) data).getHeight(BlockFace.EAST));
            newData.setHeight(BlockFace.WEST, ((Wall) data).getHeight(BlockFace.NORTH));
            newData.setHeight(BlockFace.EAST, ((Wall) data).getHeight(BlockFace.SOUTH));
            newData.setHeight(BlockFace.SOUTH, ((Wall) data).getHeight(BlockFace.WEST));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        return this;
    }

    public static Door.Hinge flipDoorHinge(Door.Hinge hinge) {
        switch (hinge) {
            case LEFT:
                return Door.Hinge.RIGHT;
            case RIGHT:
                return Door.Hinge.LEFT;
        }
        return hinge;
    }

    public static Chest.Type flipChestType(Chest.Type type) {
        switch (type) {
            case LEFT:
                return Chest.Type.RIGHT;
            case RIGHT:
                return Chest.Type.LEFT;
        }
        return type;
    }

    public static Bisected.Half flipBisectedHalf(Bisected.Half half) {
        switch (half) {
            case TOP:
                return Bisected.Half.BOTTOM;
            case BOTTOM:
                return Bisected.Half.TOP;
        }
        return half;
    }

    public static BlockFace flipFaceX(BlockFace face) {
        switch (face) {
            case EAST:
                return BlockFace.WEST;
            case WEST:
                return BlockFace.EAST;
            case NORTH_EAST:
                return BlockFace.NORTH_WEST;
            case NORTH_WEST:
                return BlockFace.NORTH_EAST;
            case SOUTH_WEST:
                return BlockFace.SOUTH_EAST;
            case SOUTH_EAST:
                return BlockFace.SOUTH_WEST;
            case NORTH_NORTH_EAST:
                return BlockFace.NORTH_NORTH_WEST;
            case NORTH_NORTH_WEST:
                return BlockFace.NORTH_NORTH_EAST;
            case SOUTH_SOUTH_WEST:
                return BlockFace.SOUTH_SOUTH_EAST;
            case SOUTH_SOUTH_EAST:
                return BlockFace.SOUTH_SOUTH_WEST;
            case EAST_NORTH_EAST:
                return BlockFace.WEST_NORTH_WEST;
            case WEST_NORTH_WEST:
                return BlockFace.EAST_NORTH_EAST;
            case WEST_SOUTH_WEST:
                return BlockFace.EAST_SOUTH_EAST;
            case EAST_SOUTH_EAST:
                return BlockFace.WEST_SOUTH_WEST;
        }
        return face;
    }

    public static Rail.Shape flipRailShapeX(Rail.Shape shape) {
        switch (shape) {
            case ASCENDING_EAST:
                return Rail.Shape.ASCENDING_WEST;
            case ASCENDING_WEST:
                return Rail.Shape.ASCENDING_EAST;
            case ASCENDING_NORTH:
                return Rail.Shape.ASCENDING_NORTH;
            case ASCENDING_SOUTH:
                return Rail.Shape.ASCENDING_SOUTH;
            case SOUTH_EAST:
                return Rail.Shape.SOUTH_WEST;
            case SOUTH_WEST:
                return Rail.Shape.SOUTH_EAST;
            case NORTH_WEST:
                return Rail.Shape.NORTH_EAST;
            case NORTH_EAST:
                return Rail.Shape.NORTH_WEST;
        }
        return shape;
    }

    public static BlockFace flipStairFaceX(Stairs.Shape shape, BlockFace face) {
        switch (shape) {
            case INNER_RIGHT:
            case OUTER_RIGHT:
                switch (face) {
                    case NORTH:
                        return BlockFace.WEST;
                    case EAST:
                        return BlockFace.SOUTH;
                    case SOUTH:
                        return BlockFace.EAST;
                    case WEST:
                        return BlockFace.NORTH;
                }
            case INNER_LEFT:
            case OUTER_LEFT:
                switch (face) {
                    case NORTH:
                        return BlockFace.EAST;
                    case EAST:
                        return BlockFace.NORTH;
                    case SOUTH:
                        return BlockFace.WEST;
                    case WEST:
                        return BlockFace.SOUTH;
                }
            case STRAIGHT:
                switch (face) {
                    case EAST:
                        return BlockFace.WEST;
                    case WEST:
                        return BlockFace.EAST;
                }
        }
        return face;
    }

    public FullBlockData flipX() {
        if (data instanceof Rotatable) {
            BlockData newData = data.clone();
            ((Rotatable) newData).setRotation(flipFaceX(((Rotatable) data).getRotation()));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Stairs) {
            BlockData newData = data.clone();
            BlockFace face = ((Stairs) data).getFacing();
            Stairs.Shape shape = ((Stairs) data).getShape();
            ((Stairs) newData).setFacing(flipStairFaceX(shape, face));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Directional) {
            BlockData newData = data.clone();
            ((Directional) newData).setFacing(flipFaceX(((Directional) data).getFacing()));
            if (data instanceof Chest) {
                ((Chest) newData).setType(flipChestType(((Chest) data).getType()));
            }
            else if (data instanceof Door) {
                switch (((Door) data).getFacing()) {
                    case NORTH:
                    case SOUTH:
                        ((Door) newData).setHinge(flipDoorHinge(((Door) data).getHinge()));
                        break;
                }
            }
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Rail) {
            BlockData newData = data.clone();
            ((Rail) newData).setShape(flipRailShapeX(((Rail) data).getShape()));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof MultipleFacing) {
            MultipleFacing newData = (MultipleFacing) data.clone();
            for (BlockFace face : ((MultipleFacing) data).getFaces()) {
                newData.setFace(face, false);
            }
            for (BlockFace face : ((MultipleFacing) data).getFaces()) {
                newData.setFace(flipFaceX(face), true);
            }
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof RedstoneWire) {
            RedstoneWire newData = (RedstoneWire) data.clone();
            newData.setFace(BlockFace.NORTH, ((RedstoneWire) data).getFace(BlockFace.NORTH));
            newData.setFace(BlockFace.WEST, ((RedstoneWire) data).getFace(BlockFace.EAST));
            newData.setFace(BlockFace.EAST, ((RedstoneWire) data).getFace(BlockFace.WEST));
            newData.setFace(BlockFace.SOUTH, ((RedstoneWire) data).getFace(BlockFace.SOUTH));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Wall) {
            Wall newData = (Wall) data.clone();
            newData.setHeight(BlockFace.NORTH, ((Wall) data).getHeight(BlockFace.NORTH));
            newData.setHeight(BlockFace.WEST, ((Wall) data).getHeight(BlockFace.EAST));
            newData.setHeight(BlockFace.EAST, ((Wall) data).getHeight(BlockFace.WEST));
            newData.setHeight(BlockFace.SOUTH, ((Wall) data).getHeight(BlockFace.SOUTH));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        return this;
    }

    public static BlockFace flipFaceY(BlockFace face) {
        switch (face) {
            case DOWN:
                return BlockFace.UP;
            case UP:
                return BlockFace.DOWN;
        }
        return face;
    }

    public FullBlockData flipY() {
        if (data instanceof Bisected) {
            BlockData newData = data.clone();
            ((Bisected) newData).setHalf(flipBisectedHalf(((Bisected) data).getHalf()));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof MultipleFacing) {
            MultipleFacing newData = (MultipleFacing) data.clone();
            for (BlockFace face : ((MultipleFacing) data).getFaces()) {
                newData.setFace(face, false);
            }
            for (BlockFace face : ((MultipleFacing) data).getFaces()) {
                newData.setFace(flipFaceY(face), true);
            }
            return new FullBlockData(newData, tileEntityData, flags);
        }
        return this;
    }

    public static BlockFace flipFaceZ(BlockFace face) {
        switch (face) {
            case NORTH:
                return BlockFace.SOUTH;
            case SOUTH:
                return BlockFace.NORTH;
            case NORTH_EAST:
                return BlockFace.SOUTH_EAST;
            case NORTH_WEST:
                return BlockFace.SOUTH_WEST;
            case SOUTH_WEST:
                return BlockFace.NORTH_WEST;
            case SOUTH_EAST:
                return BlockFace.NORTH_EAST;
            case NORTH_NORTH_EAST:
                return BlockFace.SOUTH_SOUTH_EAST;
            case NORTH_NORTH_WEST:
                return BlockFace.SOUTH_SOUTH_WEST;
            case SOUTH_SOUTH_WEST:
                return BlockFace.NORTH_NORTH_WEST;
            case SOUTH_SOUTH_EAST:
                return BlockFace.NORTH_NORTH_EAST;
            case EAST_NORTH_EAST:
                return BlockFace.EAST_SOUTH_EAST;
            case WEST_NORTH_WEST:
                return BlockFace.WEST_SOUTH_WEST;
            case WEST_SOUTH_WEST:
                return BlockFace.WEST_NORTH_WEST;
            case EAST_SOUTH_EAST:
                return BlockFace.EAST_NORTH_EAST;
        }
        return face;
    }

    public static Rail.Shape flipRailShapeZ(Rail.Shape shape) {
        switch (shape) {
            case ASCENDING_NORTH:
                return Rail.Shape.ASCENDING_SOUTH;
            case ASCENDING_SOUTH:
                return Rail.Shape.ASCENDING_NORTH;
            case SOUTH_EAST:
                return Rail.Shape.NORTH_EAST;
            case SOUTH_WEST:
                return Rail.Shape.NORTH_WEST;
            case NORTH_WEST:
                return Rail.Shape.SOUTH_WEST;
            case NORTH_EAST:
                return Rail.Shape.SOUTH_EAST;
        }
        return shape;
    }

    public static BlockFace flipStairFaceZ(Stairs.Shape shape, BlockFace face) {
        switch (shape) {
            case INNER_RIGHT:
            case OUTER_RIGHT:
                switch (face) {
                    case NORTH:
                        return BlockFace.EAST;
                    case EAST:
                        return BlockFace.NORTH;
                    case SOUTH:
                        return BlockFace.WEST;
                    case WEST:
                        return BlockFace.SOUTH;
                }
            case INNER_LEFT:
            case OUTER_LEFT:
                switch (face) {
                    case NORTH:
                        return BlockFace.WEST;
                    case EAST:
                        return BlockFace.SOUTH;
                    case SOUTH:
                        return BlockFace.EAST;
                    case WEST:
                        return BlockFace.NORTH;
                }
            case STRAIGHT:
                switch (face) {
                    case NORTH:
                        return BlockFace.SOUTH;
                    case SOUTH:
                        return BlockFace.NORTH;
                }
        }
        return face;
    }

    public FullBlockData flipZ() {
        if (data instanceof Rotatable) {
            BlockData newData = data.clone();
            ((Rotatable) newData).setRotation(flipFaceZ(((Rotatable) data).getRotation()));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Stairs) {
            BlockData newData = data.clone();
            BlockFace face = ((Stairs) data).getFacing();
            Stairs.Shape shape = ((Stairs) data).getShape();
            ((Stairs) newData).setFacing(flipStairFaceZ(shape, face));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Directional) {
            BlockData newData = data.clone();
            ((Directional) newData).setFacing(flipFaceZ(((Directional) data).getFacing()));
            if (data instanceof Chest) {
                ((Chest) newData).setType(flipChestType(((Chest) data).getType()));
            }
            else if (data instanceof Door) {
                switch (((Door) data).getFacing()) {
                    case EAST:
                    case WEST:
                        ((Door) newData).setHinge(flipDoorHinge(((Door) data).getHinge()));
                        break;
                }
            }
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Rail) {
            BlockData newData = data.clone();
            ((Rail) newData).setShape(flipRailShapeZ(((Rail) data).getShape()));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof MultipleFacing) {
            MultipleFacing newData = (MultipleFacing) data.clone();
            for (BlockFace face : ((MultipleFacing) data).getFaces()) {
                newData.setFace(face, false);
            }
            for (BlockFace face : ((MultipleFacing) data).getFaces()) {
                newData.setFace(flipFaceZ(face), true);
            }
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof RedstoneWire) {
            RedstoneWire newData = (RedstoneWire) data.clone();
            newData.setFace(BlockFace.NORTH, ((RedstoneWire) data).getFace(BlockFace.SOUTH));
            newData.setFace(BlockFace.WEST, ((RedstoneWire) data).getFace(BlockFace.WEST));
            newData.setFace(BlockFace.EAST, ((RedstoneWire) data).getFace(BlockFace.EAST));
            newData.setFace(BlockFace.SOUTH, ((RedstoneWire) data).getFace(BlockFace.NORTH));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        else if (data instanceof Wall) {
            Wall newData = (Wall) data.clone();
            newData.setHeight(BlockFace.NORTH, ((Wall) data).getHeight(BlockFace.SOUTH));
            newData.setHeight(BlockFace.WEST, ((Wall) data).getHeight(BlockFace.WEST));
            newData.setHeight(BlockFace.EAST, ((Wall) data).getHeight(BlockFace.EAST));
            newData.setHeight(BlockFace.SOUTH, ((Wall) data).getHeight(BlockFace.NORTH));
            return new FullBlockData(newData, tileEntityData, flags);
        }
        return this;
    }

    public BlockData data;

    public CompoundTag tileEntityData;

    public MapTag flags;

    public void set(Block block, boolean physics) {
        block.setBlockData(data, physics);
        if (tileEntityData != null) {
            NMSHandler.blockHelper.setNbtData(block, tileEntityData);
        }
        if (flags != null) {
            MapTagBasedFlagTracker flagMap = (MapTagBasedFlagTracker) new LocationTag(block.getLocation()).getFlagTracker();
            for (Map.Entry<StringHolder, ObjectTag> entry : flags.entrySet()) {
                flagMap.setRootMap(entry.getKey().str, entry.getValue().asType(MapTag.class, CoreUtilities.noDebugContext));
            }
        }
    }
}
