package net.aufdemrand.denizen.utilities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.*;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.citizensnpcs.api.CitizensAPI;
import net.minecraft.server.v1_6_R2.EntityLiving;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_6_R2.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * This class has utility methods for various tasks.
 *
 * @author aufdemrand, dbixler, David Cernat, AgentK
 */
public class Utilities {

    public static Location getWalkableLocationNear(Location location, int range) {
    	
    	Location returnable;
        Random range_random  = new Random();

        int selected_x = range_random.nextInt(range * 2);
        int selected_z = range_random.nextInt(range * 2);
        returnable = location.add(selected_x - range, 1, selected_z - range);

        if (!isWalkable(returnable)) return getWalkableLocationNear(location, range);
        else return returnable;
    }

    public static boolean isWalkable(Location location) {
        if ((location.getBlock().getType() == Material.AIR
                || location.getBlock().getType() == Material.GRASS)
                && (location.add(0, 1, 0).getBlock().getType() == Material.AIR))
            return true;
        else return false;
    }

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
	
	public static String[] wrapWords(String text, int width) {
		StringBuilder sb = new StringBuilder(text);
		
		int i = 0;
		while (i + width < sb.length() && (i = sb.lastIndexOf(" ", i + width)) != -1) {
			sb.replace(i, i + 1, "\n");
		}
		
		return sb.toString().split("\n");
	}

    /**
     *
     *
     * @param player  the player doing the talking
     * @param npc  the npc being talked to
     * @param range  the range, in blocks, that 'bystanders' will hear he chat
     *
     */
    public static void talkToNPC(String message, Player player, dNPC npc, double range) {
        // Get formats from Settings, and fill in <TEXT>
        String talkFormat = Settings.ChatToNpcFormat()
                .replace("<TEXT>", message).replace("<text>", message).replace("<Text>", message);
        String bystanderFormat = Settings.ChatToNpcOverheardFormat()
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
    
    
    /**
     * Counts the quantity of all items in an inventory.
     * 
     * @param  inventory  the inventory to count it in
     */
    
    public static int countItems(Inventory inventory)
    {
    	int qty = 0;
    	
    	for (ItemStack invStack : inventory)
		{
			// If ItemStacks are empty here, they are null
			if (invStack != null)
			{
				qty = qty + invStack.getAmount();
			}
		}
    	
    	return qty;
    }

    
    /**
     * Counts the quantity of items of a specific type in an inventory.
     * 
     * @param  item  the item as an itemstack
     * @param  inventory  the inventory to count it in
     */
    
    public static int countItems(ItemStack item, Inventory inventory)
    {
    	int qty = 0;
    	
    	for (ItemStack invStack : inventory)
		{
			// If ItemStacks are empty here, they are null
			if (invStack != null)
			{
				if (invStack.isSimilar(item))
					qty = qty + invStack.getAmount();
			}
		}
    	
    	return qty;
    }
    
    
    /**
     * Counts the quantity of items of a specific type in an inventory.
     * 
     * @param  item  the item's material or ID as a string
     * @param  inventory  the inventory to count it in
     */
    
    public static int countItems(String item, Inventory inventory)
    {
    	if (aH.matchesItem("item:" + item))
    	{
    		ItemStack itemstack = new ItemStack(aH.getItemFrom("item:" + item).getItemStack());
    		return countItems(itemstack, inventory);
    	}
    	
    	return 0;
    }
    

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
     * Thanks to fullwall.
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
     * Thanks to fullwall.
     *
     * @param entity The Entity whose yaw and pitch you want to change.
     * @param target The Entity it should be looking at.
     */
    
    public static void faceEntity(Entity entity, Entity target) {
        faceLocation(entity, target.getLocation());
    }
    
    
    /**
     * Finds the closest Player to a particular location.
     *
     * @param location	The location to find the closest Player to.
     * @param range	The maximum range to look for the Player.
     *
     * @return	The closest Player to the location, or null if no Player was found
     * 					within the range specified.
     */
    
    @SuppressWarnings("unchecked")
	public static Player getClosestPlayer (Location location, int range) {
    	
        Player closestPlayer = null;
        double closestDistance = Math.pow(range, 2);
        List playerList = new ArrayList(Arrays.asList(Bukkit.getOnlinePlayers()));
        Iterator<Player> it = playerList.iterator();
        while (it.hasNext()) {
            Player player = it.next();
            Location loc = player.getLocation();
            if (loc.getWorld().equals(location.getWorld())
                    && loc.distanceSquared(location) < closestDistance) {
                closestPlayer = player;
                closestDistance = player.getLocation().distanceSquared(location);
            }
        }
        return closestPlayer;
    }


    /**
     * Finds the closest NPC to a particular location.
     *
     * @param location	The location to find the closest NPC to.
     * @param range	The maximum range to look for the NPC.
     *
     * @return	The closest NPC to the location, or null if no NPC was found
     * 					within the range specified.
     */
    
    public static dNPC getClosestNPC (Location location, int range) {
    	
        dNPC closestNPC = null;
        double closestDistance = Math.pow(range, 2);
        Iterator<dNPC> it = DenizenAPI.getSpawnedNPCs().iterator();
        while (it.hasNext()) {
            dNPC npc = it.next();
            Location loc = npc.getLocation();
            if (loc.getWorld().equals(location.getWorld())
                    && loc.distanceSquared(location) < closestDistance) {
                closestNPC = npc;
                closestDistance = npc.getLocation().distanceSquared(location);
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
    	
        maxRange = (int) Math.pow(maxRange, 2);
        Set<dNPC> closestNPCs = new HashSet<dNPC> ();
        Iterator<dNPC> it = DenizenAPI.getSpawnedNPCs().iterator();
        while (it.hasNext ()) {
            dNPC npc = it.next ();
            Location loc = npc.getLocation();
            if (loc.getWorld().equals(location.getWorld()) && loc.distanceSquared(location) < maxRange) {
                closestNPCs.add(npc);
            }
        }
        return closestNPCs;
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


    /**
     * Checks entity's location against a Location (with leeway). Should be faster than
     * bukkit's built in Location.distance(Location) since there's no sqrt math.
     *
     * Thanks chainsol :)
     *
     * @return true if within the specified location, false otherwise.
     */
    
    public static boolean checkLocation(LivingEntity entity, Location theLocation, int theLeeway) {
        if (entity.getWorld() != theLocation.getWorld())
            return false;

        Location entityLocation = entity.getLocation();

        if (Math.abs(entityLocation.getX() - theLocation.getX())
                > theLeeway) return false;
        if (Math.abs(entityLocation.getY() - theLocation.getY())
                > theLeeway) return false;
        if (Math.abs(entityLocation.getZ() - theLocation.getZ())
                > theLeeway) return false;

        return true;
    }

    /**
     * Checks entity's location against a Location (with leeway). Should be faster than
     * bukkit's built in Location.distance(Location) since there's no sqrt math.
     *
     * Thanks chainsol :)
     *
     * @return true if within the specified location, false otherwise.
     */
    
    public static boolean checkLocation(Location baseLocation, Location theLocation, int theLeeway) {

        if (!baseLocation.getWorld().getName().equals(theLocation.getWorld().getName()))
            return false;

        Location entityLocation = baseLocation;

        if (Math.abs(entityLocation.getX() - theLocation.getX())
                > theLeeway) return false;
        if (Math.abs(entityLocation.getY() - theLocation.getY())
                > theLeeway) return false;
        if (Math.abs(entityLocation.getZ() - theLocation.getZ())
                > theLeeway) return false;

        return true;
    }

    protected static FilenameFilter scriptsFilter;

    static {
        scriptsFilter = new FilenameFilter() {
            public boolean accept(File file, String fileName) {
                if(fileName.startsWith(".")) return false;

                String ext = fileName.substring(fileName.lastIndexOf('.') + 1);
                return ext.equalsIgnoreCase("YML") || ext.equalsIgnoreCase("DSCRIPT");
            }
        };
    }

    /**
     * Lists all files in the given directory.
     *
     * @param dir The directory to search in
     * @param recursive If true subfolders will also get checked
     * @return A {@link File} collection
     */
    
    public static List<File> listDScriptFiles(File dir, boolean recursive) {
        List<File> files = new ArrayList<File>();
        File[] entries = dir.listFiles();

        for (File file : entries) {
            // Add file
            if (scriptsFilter == null || scriptsFilter.accept(dir, file.getName())) {
                files.add(file);
            }

            // Add subdirectories
            if (recursive && file.isDirectory()) {
                files.addAll(listDScriptFiles(file, recursive));
            }
        }

        return files;
    }

}