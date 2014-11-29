package net.aufdemrand.denizen.utilities.entity;

import net.minecraft.server.v1_8_R1.EntityHuman;
import net.minecraft.server.v1_8_R1.EntityLiving;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftEntity;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Utilities related to entity yaws and pitches.
 *
 * @author David Cernat, fullwall
 */

public class Rotation {

    /**
     * Rotates an entity.
     *
     * @param entity The Entity you want to rotate.
     * @param yaw The new yaw of the entity.
     * @param pitch The new pitch of the entity.
     */

    public static void rotate(Entity entity, float yaw, float pitch) {
        // If this entity is a real player instead of a player type NPC,
        // it will appear to be online

        if (entity instanceof Player && ((Player) entity).isOnline()) {
            Location location = entity.getLocation();
            location.setYaw(yaw);
            location.setPitch(pitch);

            // The only way to change a player's yaw and pitch in Bukkit
            // is to use teleport on him/her

            entity.teleport(location);
        }

        else if (entity instanceof LivingEntity) {
            if (entity instanceof EnderDragon) yaw = normalizeYaw(yaw - 180);
            look(entity, yaw, pitch);
        }

        else {
            net.minecraft.server.v1_8_R1.Entity handle = ((CraftEntity) entity).getHandle();
            handle.yaw = yaw;
            handle.pitch = pitch;
        }
    }

    // Taken from C2 NMS class for less dependency on C2
    private static void look(Entity entity, float yaw, float pitch) {
        net.minecraft.server.v1_8_R1.Entity handle = !(entity instanceof CraftEntity)?null:((CraftEntity)entity).getHandle();
        if (handle != null) {
            handle.yaw = yaw;
            if(entity instanceof EntityLiving) {
                EntityLiving livingHandle = (EntityLiving) entity;
                while (yaw < -180.0F) {
                    yaw += 360.0F;
                }
                while(yaw >= 180.0F) {
                    yaw -= 360.0F;
                }
                livingHandle.aI = yaw;
                if(!(handle instanceof EntityHuman))
                    livingHandle.aG = yaw;
                livingHandle.aJ = yaw;
            }
            handle.pitch = pitch;
        }
    }


    /**
     * Changes an entity's yaw and pitch to make it face a location.
     *
     * @param from The Entity whose yaw and pitch you want to change.
     * @param at The Location it should be looking at.
     */

    public static void faceLocation(Entity from, Location at) {
        if (from.getWorld() != at.getWorld()) return;
        Location loc = from.getLocation().getBlock().getLocation().clone().add(0.5,0.5,0.5);

        double xDiff = at.getX() - loc.getX();
        double yDiff = at.getY() - loc.getY();
        double zDiff = at.getZ() - loc.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
        double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 70;

        if (zDiff < 0.0) {
            yaw = yaw + (Math.abs(180 - yaw) * 2);
        }

        rotate(from, (float) yaw - 90, (float) pitch);
    }


    /**
     * Changes an entity's yaw and pitch to make it face another entity.
     *
     * @param entity The Entity whose yaw and pitch you want to change.
     * @param target The Entity it should be looking at.
     */

    public static void faceEntity(Entity entity, Entity target) {
        faceLocation(entity, target.getLocation());
    }


    /**
     * Checks if a Location's yaw is facing another Location.
     *
     * Note: do not use a player's location as the first argument,
     *       because player yaws need to modified. Use the method
     *       below this one instead.
     *
     * @param  from  The Location we check.
     * @param  at  The Location we want to know if the first Location's yaw
     *             is facing
     * @param  degreeLimit  How many degrees can be between the direction the
     *                         first location's yaw is facing and the direction
     *                         we check if it is facing.
     *
     * @return  Returns a boolean.
     */

    public static boolean isFacingLocation(Location from, Location at, float degreeLimit) {

        double currentYaw = normalizeYaw(from.getYaw());

        double requiredYaw = normalizeYaw(getYaw(at.toVector().subtract(
                from.toVector()).normalize()));

        return (Math.abs(requiredYaw - currentYaw) < degreeLimit ||
                Math.abs(requiredYaw + 360 - currentYaw) < degreeLimit ||
                Math.abs(currentYaw + 360 - requiredYaw) < degreeLimit);
    }


    /**
     * Checks if an Entity is facing a Location.
     *
     * @param  from  The Entity we check.
     * @param  at  The Location we want to know if it is looking at.
     * @param  degreeLimit  How many degrees can be between the direction the
     *                         Entity is facing and the direction we check if it
     *                         is facing.
     *
     * @return  Returns a boolean.
     */

    public static boolean isFacingLocation(Entity from, Location at, float degreeLimit) {

        return isFacingLocation(from.getLocation(), at, degreeLimit);
    }


    /**
     * Checks if an Entity is facing another Entity.
     *
     * @param from The Entity we check.
     * @param at The Entity we want to know if it is looking at.
     * @param degreeLimit How many degrees can be between the direction the
     *                    Entity is facing and the direction we check if it
     *                    is facing.
     *
     * @return  Returns a boolean.
     */

    public static boolean isFacingEntity(Entity from, Entity at, float degreeLimit) {

        return isFacingLocation(from.getLocation(), at.getLocation(), degreeLimit);
    }


    /**
     * Normalizes Mincraft's yaws (which can be negative or can exceed 360)
     * by turning them into proper yaw values that only go from 0 to 359.
     *
     * @param  yaw  The original yaw.
     *
     * @return  The normalized yaw.
     */

    public static float normalizeYaw(float yaw) {
        yaw = yaw  % 360;
        if (yaw < 0) yaw += 360.0;
        return yaw;
    }


    /**
     * Converts a vector to a yaw.
     *
     * Thanks to bergerkiller.
     *
     * @param  vector  The vector you want to get a yaw from.
     *
     * @return  The yaw.
     */

    public static float getYaw(Vector vector) {
        double dx = vector.getX();
        double dz = vector.getZ();
        double yaw = 0;
        // Set yaw
        if (dx != 0) {
            // Set yaw start value based on dx
            if (dx < 0) {
                yaw = 1.5 * Math.PI;
            } else {
                yaw = 0.5 * Math.PI;
            }
            yaw -= Math.atan(dz / dx);
        } else if (dz < 0) {
            yaw = Math.PI;
        }
        return (float) (-yaw * 180 / Math.PI);
    }


    /**
     * Converts a yaw to a cardinal direction name.
     *
     * @param  yaw  The yaw you want to get a cardinal direction from.
     *
     * @return  The name of the cardinal direction as a String.
     */

    public static String getCardinal(float yaw) {
        yaw = normalizeYaw(yaw);
        // Compare yaws, return closest direction.
        if (0 <= yaw && yaw < 22.5)
            return "south";
        else if (22.5 <= yaw && yaw < 67.5)
            return "southwest";
        else if (67.5 <= yaw && yaw < 112.5)
            return "west";
        else if (112.5 <= yaw && yaw < 157.5)
            return "northwest";
        else if (157.5 <= yaw && yaw < 202.5)
            return "north";
        else if (202.5 <= yaw && yaw < 247.5)
            return "northeast";
        else if (247.5 <= yaw && yaw < 292.5)
            return "east";
        else if (292.5 <= yaw && yaw < 337.5)
            return "southeast";
        else if (337.5 <= yaw && yaw < 360.0)
            return "south";
        else
            return null;
    }
}
