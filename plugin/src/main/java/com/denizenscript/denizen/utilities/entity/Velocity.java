package com.denizenscript.denizen.utilities.entity;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Velocity {

    /**
     * Calculates the vector between two locations' vectors
     * <p/>
     * Original code by SethBling, edited to be a bit
     * more accurate.
     *
     * @param from       The origin's vector
     * @param to         The destination's vector
     * @param gravity    The gravity value of the entity
     * @param heightGain The gain in height
     * @return A vector
     */
    public static Vector calculate(Vector from, Vector to, double gravity, double heightGain) {
        // Block locations
        int endGain = to.getBlockY() - from.getBlockY();
        double horizDist = Math.sqrt(distanceSquared(from, to));
        if (horizDist == 0) {
            if (to.getY() > from.getY()) {
                return new Vector(0, 1, 0);
            }
            return new Vector(0, -1, 0);
        }
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

    public static Vector spread(Vector from, double yaw, double pitch) {
        Vector vec = from.clone();
        float cosyaw = (float) Math.cos(yaw);
        float cospitch = (float) Math.cos(pitch);
        float sinyaw = (float) Math.sin(yaw);
        float sinpitch = (float) Math.sin(pitch);
        float bX = (float) (vec.getY() * sinpitch + vec.getX() * cospitch);
        float bY = (float) (vec.getY() * cospitch - vec.getX() * sinpitch);
        return new Vector(bX * cosyaw - vec.getZ() * sinyaw, bY, bX * sinyaw + vec.getZ() * cosyaw);
    }

    public static double launchAngle(Location from, Vector to, double v, double elev, double g) {
        Vector victor = from.toVector().subtract(to);
        double dist = Math.sqrt(Math.pow(victor.getX(), 2) + Math.pow(victor.getZ(), 2));
        double v2 = Math.pow(v, 2);
        double v4 = Math.pow(v, 4);
        double derp = g * (g * Math.pow(dist, 2) + 2 * elev * v2);
        if (v4 < derp) {
            // Max optimal (won't hit!)
            return Math.atan((2 * g * elev + v2) / (2 * g * elev + 2 * v2));
        }
        else {
            return Math.atan((v2 - Math.sqrt(v4 - derp)) / (g * dist));
        }
    }

    public static double hangtime(double launchAngle, double v, double elev, double g) {
        double a = v * Math.sin(launchAngle);
        double b = -2 * g * elev;
        if (Math.pow(a, 2) + b < 0) {
            return 0;
        }
        return (a + Math.sqrt(Math.pow(a, 2) + b)) / g;
    }
}
