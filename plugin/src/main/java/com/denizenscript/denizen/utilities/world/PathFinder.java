package com.denizenscript.denizen.utilities.world;

import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import net.citizensnpcs.api.astar.AStarMachine;
import net.citizensnpcs.api.astar.pathfinder.*;
import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: Potentially a separate implementation?
 */
public class PathFinder {

    public static AStarMachine ASTAR = AStarMachine.createWithDefaultStorage();

    public static List<LocationTag> getPath(Location start, Location dest) {
        VectorGoal goal = new VectorGoal(dest, 1);
        Path plan = (Path) ASTAR.runFully(goal, new VectorNode(goal, start, new ChunkBlockSource(start, 100), new MinecraftBlockExaminer()), 50000);
        if (plan == null) {
            Debug.verboseLog("PathFinder: No path found from " + start + " to " + dest + " (return null)");
            return new ArrayList<>();
        }
        if (plan.isComplete()) {
            Debug.verboseLog("PathFinder: path from " + start + " to " + dest + " is instantly completed");
            return new ArrayList<>();
        }
        else {
            List<LocationTag> path = new ArrayList<>();
            while (!plan.isComplete()) {
                Vector v = plan.getCurrentVector();
                path.add(new LocationTag(start.getWorld(), v.getX(), v.getY(), v.getZ()));
                plan.update(null);
            }
            return path;
        }
    }
}
