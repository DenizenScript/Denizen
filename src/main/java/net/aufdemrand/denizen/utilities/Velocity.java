package net.aufdemrand.denizen.utilities;

import org.bukkit.util.Vector;

public class Velocity {
    
    /**
     * Calculates the vector between two locations' vectors
     *
     * Original code by SethBling, edited to be a bit
     * more accurate.
     *
     * @param  from     The origin's vector
     * @param  to     The destination's vector
     * @param  gravity     The gravity value of the entity
     * @param  heightGain     The gain in height
     *
     * @return  A vector
     */
    
    public static Vector calculate(Vector from, Vector to, double gravity, double heightGain) {
     
        // Block locations
        int endGain = to.getBlockY() - from.getBlockY();
        double horizDist = Math.sqrt(distanceSquared(from, to));
          
        double maxGain = heightGain > (endGain + heightGain) ? heightGain : (endGain + heightGain);
     
        // Solve quadratic equation for velocity
        double a = -horizDist * horizDist / (4 * maxGain);
        double b = horizDist;
        double c = -endGain;
     
        double slope = -b / (2 * a) - Math.sqrt(b * b - 4 * a * c) / (2 * a);
        
        // Vertical velocity
        double vy = Math.sqrt(maxGain * (gravity + 0.0013675090252708 * heightGain));
     
        // Horizontal velocity
        double vh = vy / slope;
     
        // Calculate horizontal direction
        int dx = to.getBlockX() - from.getBlockX();
        int dz = to.getBlockZ() - from.getBlockZ();
        double mag = Math.sqrt(dx * dx + dz * dz);
        double dirx = dx / mag;
        double dirz = dz / mag;
     
        // Horizontal velocity components
        double vx = vh * dirx;
        double vz = vh * dirz;
     
        return new Vector(vx, vy, vz);
    }
     
    private static double distanceSquared(Vector from, Vector to) {

        double dx = to.getBlockX() - from.getBlockX();
        double dz = to.getBlockZ() - from.getBlockZ();
     
        return dx * dx + dz * dz;
    }

}
