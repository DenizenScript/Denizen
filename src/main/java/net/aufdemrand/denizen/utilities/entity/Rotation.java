package net.aufdemrand.denizen.utilities.entity;

import net.minecraft.server.v1_6_R2.EntityLiving;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
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
    
    public static void rotate(Entity entity, float yaw, float pitch)
    {
    	// If this entity is a real player instead of a player type NPC,
    	// it will appear to be online
    	
        if (entity instanceof Player && ((Player) entity).isOnline())
        {
    		Location location = entity.getLocation();
    		location.setYaw(yaw);
    		location.setPitch(pitch);
    		
    		// The only way to change a player's yaw and pitch in Bukkit
    		// is to use teleport on him/her
    		
    		entity.teleport(location);
    		return;
        }
        
        if (entity instanceof LivingEntity)
        {
            EntityLiving handle = ((CraftLivingEntity) entity).getHandle();
            handle.yaw = (float) yaw;
            handle.pitch = (float) pitch;
            // !--- START NMS OBFUSCATED
            handle.aA = handle.yaw; // The head's yaw
            // !--- END NMS OBFUSCATED

            if (!(entity instanceof Player))
            {
            	// Obfuscated variable used in head turning. If not set to
            	// be equal to the yaw, non-Player entities will not rotate.
            	// But do not use on Player entities, because it will break
            	// their rotation.
            	//
            	// In case it ever gets renamed, this EntityLiving line is
            	// the one with it:
            	//
            	// float f5 = MathHelper.g(this.yaw - this.ax);
            
            	handle.ax = handle.yaw;
            }
        }

        else
        {
            net.minecraft.server.v1_6_R2.Entity handle = ((CraftEntity) entity).getHandle();
            handle.yaw = (float) yaw;
            handle.pitch = (float) pitch;
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
        Location loc = from.getLocation();

        double xDiff = at.getX() - loc.getX();
        double yDiff = at.getY() - loc.getY();
        double zDiff = at.getZ() - loc.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = Math.toDegrees(Math.acos(xDiff / distanceXZ));
        double pitch = Math.toDegrees(Math.acos(yDiff / distanceY)) - 90;
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
     * 						first location's yaw is facing and the direction
     * 						we check if it is facing.
     *
     * @return  Returns a boolean.
     */
    
    public static boolean isFacingLocation(Location from, Location at, float degreeLimit) {

        double currentYaw = normalizeYaw(from.getYaw());

        double requiredYaw = normalizeYaw(getYaw(at.toVector().subtract(
                from.toVector()).normalize()));

        if (Math.abs(requiredYaw - currentYaw) < degreeLimit ||
                Math.abs(requiredYaw + 360 - currentYaw) < degreeLimit ||
                Math.abs(currentYaw + 360 - requiredYaw) < degreeLimit)
            return true;

        return false;
    }
    

    /**
     * Checks if an Entity is facing a Location.
     *
     * @param  from  The Entity we check.
     * @param  at  The Location we want to know if it is looking at.
     * @param  degreeLimit  How many degrees can be between the direction the
     * 						Entity is facing and the direction we check if it
     * 						is facing.
     *
     * @return  Returns a boolean.
     */
    
    public static boolean isFacingLocation(Entity from, Location at, float degreeLimit) {

        Location location = from.getLocation();
        
        // Important! Need to subtract 90 from player yaws
        if (from instanceof Player) {
            location.setYaw(location.getYaw() - 90);
        }

        return isFacingLocation(location, at, degreeLimit);
    }


    /**
     * Checks if an Entity is facing another Entity.
     *
     * @param from The Entity we check.
     * @param at The Entity we want to know if it is looking at.
     * @param degreeLimit How many degrees can be between the direction the
     * 					  Entity is facing and the direction we check if it
     * 					  is facing.
     *
     * @return  Returns a boolean.
     */
    
    public static boolean isFacingEntity(Entity from, Entity at, float degreeLimit) {

        return isFacingLocation(from, at.getLocation(), degreeLimit);
    }
    
    
    /**
     * Normalizes Mincraft's yaws (which can be negative or can exceed 360)
     * by turning them into proper yaw values that only go from 0 to 359.
     *
     * @param  yaw  The original yaw.
     *
     * @return  The normalized yaw.
     */
    
    public static double normalizeYaw(double yaw) {
        yaw = (yaw - 90) % 360;
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
        return (float) (-yaw * 180 / Math.PI - 90);
    }


    /**
     * Converts a yaw to a cardinal direction name.
     *
     * Thanks to sk89qs
     *
     * @param  yaw  The yaw you want to get a cardinal direction from.
     *
     * @return  The name of the cardinal direction as a String.
     */
    
    public static String getCardinal(double yaw) {
        yaw = normalizeYaw(yaw);
        // Compare yaws, return closest direction.
        if (0 <= yaw && yaw < 22.5)
            return "north";
        else if (22.5 <= yaw && yaw < 67.5)
            return "northeast";
        else if (67.5 <= yaw && yaw < 112.5)
            return "east";
        else if (112.5 <= yaw && yaw < 157.5)
            return "southeast";
        else if (157.5 <= yaw && yaw < 202.5)
            return "south";
        else if (202.5 <= yaw && yaw < 247.5)
            return "southwest";
        else if (247.5 <= yaw && yaw < 292.5)
            return "west";
        else if (292.5 <= yaw && yaw < 337.5)
            return "northwest";
        else if (337.5 <= yaw && yaw < 360.0)
            return "north";
        else
            return null;
    }

}
