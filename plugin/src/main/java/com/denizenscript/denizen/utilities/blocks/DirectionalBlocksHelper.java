package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.Utilities;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.util.Vector;

public class DirectionalBlocksHelper {

    public static BlockFace getFace(Block b) {
        ModernBlockData mbd = new ModernBlockData(b);
        if (mbd.data instanceof Directional) {
            return ((Directional) mbd.data).getFacing();
        }
        else if (mbd.data instanceof Rotatable) {
            return ((Rotatable) mbd.data).getRotation();
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
        ModernBlockData mbd = new ModernBlockData(b);
        if (mbd.data instanceof Directional) {
            Directional dir = (Directional) mbd.data;
            dir.setFacing(face);
            b.setBlockData(dir);
        }
        else if (mbd.data instanceof Rotatable) {
            Rotatable dir = (Rotatable) mbd.data;
            dir.setRotation(face);
            b.setBlockData(dir);
        }
    }

    public static void setFacing(Block b, Vector faceVec) {
        BlockFace newFace = Utilities.faceFor(faceVec);
        if (newFace == null) {
            Debug.echoError("Direction '" + faceVec + "' does not appear to be a valid block face.");
            return;
        }
        setFace(b, newFace);
    }
}
