package com.denizenscript.denizen.utilities;

import com.denizenscript.denizen.objects.dLocation;
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

    public static List<dLocation> getPath(Location start, Location dest) {
        VectorGoal goal = new VectorGoal(dest, 1);
        Path plan = (Path) ASTAR.runFully(goal,
                new VectorNode(goal, start, new ChunkBlockSource(start, 100), new MinecraftBlockExaminer()),
                50000);
        if (plan == null || plan.isComplete()) {
            return new ArrayList<>();
        }
        else {
            List<dLocation> path = new ArrayList<>();
            while (!plan.isComplete()) {
                Vector v = plan.getCurrentVector();
                path.add(new dLocation(start.getWorld(), v.getX(), v.getY(), v.getZ()));
                plan.update(null);
            }
            return path;
        }
    }
}
