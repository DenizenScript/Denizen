package com.denizenscript.denizen.utilities.blocks;

import com.denizenscript.denizen.utilities.Utilities;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashSet;

public class SpawnableHelper {

    /**
     * Materials that would be a problem to spawn in/on.
     */
    public static HashSet<Material> DANGEROUS_MATERIALS = new HashSet<>(Utilities.allMaterialsThatMatch(
            "fire|cactus|water|lava|magma_block|*cauldron|*campfire|*portal" // Core problems
            + "|cobweb|ladder|*fence|*door|end_rod|iron_bars|chain|*wall|*_pane" // Unstable
            + "*egg|*plate|tripwire|*piston")); // Could hurt

    /**
     * Returns true if the location would likely be safe to spawn a player at (based on material solidity at, below, and above the location).
     */
    public static boolean isSpawnable(Location loc) {
        World w = loc.getWorld();
        if (w == null) {
            return false;
        }
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        if (y - 1 <= w.getMinHeight() || y + 1 >= w.getMaxHeight()) {
            return false;
        }
        if (!w.getBlockAt(x, y + 1, z).getType().isAir()) {
            return false;
        }
        Material self = w.getBlockAt(x, y, z).getType();
        if (self.isSolid()) {
            return false;
        }
        if (DANGEROUS_MATERIALS.contains(self)) {
            return false;
        }
        Material below = w.getBlockAt(x, y - 1, z).getType();
        if (!below.isSolid()) {
            return false;
        }
        if (DANGEROUS_MATERIALS.contains(below)) {
            return false;
        }
        return true;
    }
}
