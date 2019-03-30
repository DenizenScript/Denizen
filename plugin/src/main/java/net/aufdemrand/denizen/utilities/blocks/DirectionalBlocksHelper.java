package net.aufdemrand.denizen.utilities.blocks;

import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Bed;
import org.bukkit.util.Vector;

public class DirectionalBlocksHelper {

    public static boolean isBedTopHalf(Block b) {
        if (b.getBlockData() instanceof Bed) {
            return ((Bed) b.getBlockData()).getPart() == Bed.Part.HEAD;
        }
        return false;
    }

    public static BlockFace getFace(Block b) {
        if (b.getBlockData() instanceof Directional) {
            return ((Directional) b.getBlockData()).getFacing();
        }
        else if (b.getBlockData() instanceof Rotatable) {
            return ((Rotatable) b.getBlockData()).getRotation();
        }
        return null;
    }

    public static Vector getFacing(Block b) {
        BlockFace face = getFace(b);
        if (face != null) {
            return face.getDirection();
        }
        return null;
    }

    public static void setFace(Block b, BlockFace face) {
        if (b.getBlockData() instanceof Directional) {
            Directional dir = (Directional) b.getBlockData();
            dir.setFacing(face);
            b.setBlockData(dir);
        }
        else if (b.getBlockData() instanceof Rotatable) {
            Rotatable dir = (Rotatable) b.getBlockData();
            dir.setRotation(face);
            b.setBlockData(dir);
        }
    }

    public static void setFacing(Block b, Vector faceVec) {
        BlockFace newFace = Utilities.faceFor(faceVec);
        if (newFace == null) {
            dB.echoError("Direction '" + faceVec + "' does not appear to be a valid block face.");
            return;
        }
        setFace(b, newFace);
    }
}
