package net.aufdemrand.denizen.utilities;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.dNPC;
import net.minecraft.server.v1_4_R1.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_4_R1.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

/**
 * This class has utility methods for various tasks.
 *
 * @author aufdemrand, dbixler, AgentK
 */
public class Utilities {


    public static String arrayToString(String[] input, String glue){
        String output="";
        int length = input.length;
        int i = 1;
        for(String s : input){
            output.concat(s);
            i++;
            if(i!=length){
                output.concat(glue);
            }
        }
        return output;
    }

    /**
     *
     *
     * @param player  the player doing the talking
     * @param npc  the npc being talked to
     * @param range  the range, in blocks, that 'bystanders' will hear he chat
     *
     */
    public static void talkToNPC(String message, Player player, dNPC npc, int range) {
        // Get formats from Settings, and fill in <TEXT>
        String talkFormat = Settings.ChatToNpcFormat()
                .replace("<TEXT>", message).replace("<text>", message).replace("<Text>", message);
        String bystanderFormat = Settings.ChatToNpcBystandersFormat()
                .replace("<TEXT>", message).replace("<text>", message).replace("<Text>", message);

        // Fill in tags
        talkFormat = DenizenAPI.getCurrentInstance().tagManager()
                .tag(player, npc, talkFormat, false);
        bystanderFormat = DenizenAPI.getCurrentInstance().tagManager()
                .tag(player, npc, bystanderFormat, false);

        // Send message to player
        player.sendMessage(talkFormat);

        // Send message to bystanders
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target != player)
                if (target.getWorld().equals(player.getWorld())
                        && target.getLocation().distance(player.getLocation()) <= range)
                    target.sendMessage(bystanderFormat);
        }
    }


    public static int lastIndexOfUCL(String str) {
        for(int i=str.length()-1; i>=0; i--) {
            if(Character.isUpperCase(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }


    public static int lastIndexOfLCL(String str) {
        for(int i=str.length()-1; i>=0; i--) {
            if(Character.isLowerCase(str.charAt(i))) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Gets the plugin version from the maven info in the jar, if available.
     *
     * @return
     */
    public String getVersionNumber() {
        Properties props = new Properties();
        //Set a default just in case.
        props.put("version", "Unknown development build");
        try	{
            props.load(this.getClass().getResourceAsStream("/META-INF/maven/net.aufdemrand/denizen/pom.properties"));
        }
        catch(Exception e) {
            //Maybe log?
        }
        return props.getProperty("version");
    }


    /*
     * This utility changes an entity's yaw and pitch to make it face
     * a location.
     *
     * Thanks to fullwall for it.
     *
     * @param  from  the Entity whose yaw and pitch you want to change
     * @param at  the Location it should be looking at
     */
    public static void faceLocation(Entity from, Location at) {
        if (from.getWorld() != at.getWorld()) return;
        Location loc = from.getLocation();

        double xDiff = at.getX() - loc.getX();
        double yDiff = at.getY() - loc.getY();
        double zDiff = at.getZ() - loc.getZ();

        double distanceXZ = Math.sqrt(xDiff * xDiff + zDiff * zDiff);
        double distanceY = Math.sqrt(distanceXZ * distanceXZ + yDiff * yDiff);

        double yaw = (Math.acos(xDiff / distanceXZ) * 180 / Math.PI);
        double pitch = (Math.acos(yDiff / distanceY) * 180 / Math.PI) - 90;
        if (zDiff < 0.0) {
            yaw = yaw + (Math.abs(180 - yaw) * 2);
        }

        if (from instanceof LivingEntity) {
            EntityLiving handle = ((CraftLivingEntity) from).getHandle();
            handle.yaw = (float) yaw - 90;
            handle.pitch = (float) pitch;
            handle.az = handle.yaw;
        } else {
            net.minecraft.server.v1_4_R1.Entity handle = ((CraftEntity) from).getHandle();
            handle.yaw = (float) yaw - 90;
            handle.pitch = (float) pitch;
        }

    }


    /**
     * This utility changes an entity's yaw and pitch to make it face
     * another entity.
     *
     * Thanks to fullwall for it.
     *
     * @param entity  the Entity whose yaw and pitch you want to change
     * @param target  the Entity it should be looking at
     */
    public static void faceEntity(Entity entity, Entity target) {
        faceLocation(entity, target.getLocation());
    }


    /**
     * This is a Utility method for finding the closest NPC to a particular
     * location.
     *
     * @param location	The location to find the closest NPC to.
     * @param range	The maximum range to look for the NPC.
     *
     * @return	The closest NPC to the location, or null if no NPC was found
     * 					within the range specified.
     */
    public static dNPC getClosestNPC (Location location, int range) {
        dNPC closestNPC = null;
        Double	closestDistance = Double.valueOf(range);
        Iterator<dNPC>	it = DenizenAPI.getSpawnedNPCs().iterator();
        while (it.hasNext ()) {
            dNPC	npc = it.next ();
            if (npc.getLocation().getWorld().equals(location.getWorld())
                    && npc.getLocation().distance(location) < closestDistance) {
                closestNPC = npc;
                closestDistance = npc.getLocation().distance(location);
            }
        }
        return closestNPC;
    }


    /**
     * Returns a list of all NPCs within a certain range.
     *
     * @param location	The location to search.
     * @param maxRange	The maximum range of the NPCs
     *
     * @return	The list of NPCs within the max range.
     */
    public static Set<dNPC> getClosestNPCs (Location location, int maxRange) {
        Set<dNPC> closestNPCs = new HashSet<dNPC> ();
        Iterator<dNPC> it = DenizenAPI.getSpawnedNPCs().iterator();
        while (it.hasNext ()) {
            dNPC npc = it.next ();
            if (npc.getLocation().getWorld().equals(location.getWorld())
                    && npc.getLocation().distance(location) < maxRange) {
                closestNPCs.add (npc);
            }
        }
        return closestNPCs;
    }


    /**
     * This utility normalizes Mincraft's yaws (which can be negative or
     * can exceed 360) by turning them into proper yaw values that only go from
     * 0 to 359.
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
     * This utility checks if an Entity is facing a Location.
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
        
    	double currentYaw;
    	
        if (from instanceof Player) // need to subtract 90 from player yaws
            currentYaw = normalizeYaw(from.getLocation().getYaw() - 90);
        else
            currentYaw = normalizeYaw(from.getLocation().getYaw());

        double requiredYaw = normalizeYaw(getYaw(at.toVector().subtract(
                from.getLocation().toVector()).normalize()));

        if (Math.abs(requiredYaw - currentYaw) < degreeLimit ||
                Math.abs(requiredYaw + 360 - currentYaw) < degreeLimit ||
                Math.abs(currentYaw + 360 - requiredYaw) < degreeLimit)
            return true;

        return false;
    }


    /**
     * This utility checks if an Entity is facing another Entity.
     *
     * @param  from  The Entity we check.
     * @param at  The Entity we want to know if it is looking at.
     * @param  degreeLimit  How many degrees can be between the direction the
     * 						Entity is facing and the direction we check if it
     * 						is facing.
     *
     * @return  Returns a boolean.
     */
    public static boolean isFacingEntity(Entity from, Entity at, float degreeLimit) {

        return isFacingLocation(from, at.getLocation(), degreeLimit);
    }


    /**
     * Converts a vector to a yaw.
     *
     * Thanks to bergerkiller
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


    /**
     * Checks entity's location against a Location (with leeway). Should be faster than
     * bukkit's built in Location.distance(Location) since there's no sqrt math.
     *
     * Thanks chainsol :)
     *
     * @return true if within the specified location, false otherwise.
     */
    public static boolean checkLocation(LivingEntity entity, Location theLocation, int theLeeway) {

        if (!entity.getWorld().getName().equals(theLocation.getWorld().getName()))
            return false;

        Location entityLocation = entity.getLocation();

        if (Math.abs(entityLocation.getBlockX() - theLocation.getBlockX())
                > theLeeway) return false;
        if (Math.abs(entityLocation.getBlockY() - theLocation.getBlockY())
                > theLeeway) return false;
        if (Math.abs(entityLocation.getBlockZ() - theLocation.getBlockZ())
                > theLeeway) return false;

        return true;
    }

}