package net.aufdemrand.denizen.utilities;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Set;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.TagManager;

import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * This class has utility methods for various tasks.
 *
 * @author aufdemrand, dbixler, AgentK
 */
public class Utilities {

    static Random random = new Random();

    public static Location getWalkableLocationNear(Location location, int range) {
        Location returnable;

        int selected_x = random.nextInt(range * 2);
        int selected_z = random.nextInt(range * 2);
        returnable = location.add(selected_x - range, 1, selected_z - range);

        if (!isWalkable(returnable)) return getWalkableLocationNear(location, range);
        else return returnable;
    }

    public static boolean isWalkable(Location location) {
        return ((location.getBlock().getType() == Material.AIR
                || location.getBlock().getType() == Material.GRASS)
                && (location.add(0, 1, 0).getBlock().getType() == Material.AIR));
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
    public static void talkToNPC(String message, dPlayer player, dNPC npc, double range) {
        // Get formats from Settings, and fill in <TEXT>
        String talkFormat = Settings.ChatToNpcFormat()
                .replace("<TEXT>", message).replace("<text>", message).replace("<Text>", message);
        String bystanderFormat = Settings.ChatToNpcOverheardFormat()
                .replace("<TEXT>", message).replace("<text>", message).replace("<Text>", message);

        // Fill in tags
        talkFormat = TagManager.tag(player, npc, talkFormat, false);
        bystanderFormat = TagManager.tag(player, npc, bystanderFormat, false);

        // Send message to player
        player.getPlayerEntity().sendMessage(talkFormat);

        // Send message to bystanders
        for (Player target : Bukkit.getOnlinePlayers()) {
            if (target != player.getPlayerEntity())
                if (target.getWorld().equals(player.getPlayerEntity().getWorld())
                        && target.getLocation().distance(player.getPlayerEntity().getLocation()) <= range)
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

    /**
     * Checks if c is in between a and b.
     *
     * @param a
     * @param b
     * @param c
     * @return  true if c is in between.
     */
    public static boolean isBetween(double a, double b, double c) {
        return b > a ? c > a && c < b : c > b && c < a;
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
        try    {
            props.load(this.getClass().getResourceAsStream("/META-INF/maven/net.aufdemrand/denizen/pom.properties"));
        }
        catch(Exception e) {
            //Maybe log?
        }
        return props.getProperty("version");
    }


    public static List<Block> getRandomSolidBlocks(Location location, int range, int count) {
        List<Block> blocks = new ArrayList<Block>();

        int x = 0;
        int f = 0;

        while (x < count) {

            if (f > 1000) break;
            f++;

            Location loc = location.clone()
                    .add(Utilities.getRandom().nextInt(range * 2) - range,
                            Utilities.getRandom().nextInt(range * 2) - range,
                            Utilities.getRandom().nextInt(range * 2) - range);

            if (loc.getBlock().getType().isSolid()) {
                blocks.add(loc.getBlock());
                x++;
            }

        }

        dB.log(blocks.size() + " blocksize");

        return blocks;
    }


    /**
     * Finds the closest Player to a particular location.
     *
     * @param location    The location to find the closest Player to.
     * @param range    The maximum range to look for the Player.
     *
     * @return    The closest Player to the location, or null if no Player was found
     *                     within the range specified.
     */

    public static Player getClosestPlayer (Location location, int range) {

        Player closestPlayer = null;
        double closestDistance = Math.pow(range, 2);
        List<Player> playerList = new ArrayList<Player>(Arrays.asList(Bukkit.getOnlinePlayers()));
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
     * Finds the closest Players to a particular location.
     *
     * @param location    The location to find the closest Player to.
     * @param range    The maximum range to look for the Player.
     *
     * @return    The closest Player to the location, or null if no Player was found
     *                     within the range specified.
     */

    public static List<dPlayer> getClosestPlayers(Location location, int range) {

        List<dPlayer> closestPlayers = new ArrayList<dPlayer>();
        double closestDistance = Math.pow(range, 2);
        List<Player> playerList = new ArrayList<Player>(Arrays.asList(Bukkit.getOnlinePlayers()));
        Iterator<Player> it = playerList.iterator();
        while (it.hasNext()) {
            Player player = it.next();
            Location loc = player.getLocation();
            if (loc.getWorld().equals(location.getWorld())
                    && loc.distanceSquared(location) < closestDistance) {
                closestPlayers.add(dPlayer.mirrorBukkitPlayer(player));
            }
        }
        return closestPlayers;
    }


    /**
     * Finds the closest NPC to a particular location.
     *
     * @param location    The location to find the closest NPC to.
     * @param range    The maximum range to look for the NPC.
     *
     * @return    The closest NPC to the location, or null if no NPC was found
     *                     within the range specified.
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
     * @param location    The location to search.
     * @param maxRange    The maximum range of the NPCs
     *
     * @return    The list of NPCs within the max range.
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

        if (Math.abs(baseLocation.getX() - theLocation.getX())
                > theLeeway) return false;
        if (Math.abs(baseLocation.getY() - theLocation.getY())
                > theLeeway) return false;
        if (Math.abs(baseLocation.getZ() - theLocation.getZ())
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

    public static Random getRandom() {
        return random;
    }

    /**
     * Set the lines on a sign to the strings in a string array
     *
     * @param sign  The sign
     * @param lines  The string array
     */

    public static void setSignLines(Sign sign, String[] lines) {

        int n = 0;

        for (String line : lines) {
            sign.setLine(n, line);
            n++;
        }

        sign.update();
    }

    /**
     * Make a wall sign attach itself to an available surface
     *
     * @param signState  The sign's blockState
     */

    public static void setSignRotation(BlockState signState) {

        BlockFace[] blockFaces = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

        for (BlockFace blockFace : blockFaces) {

            Block block = signState.getBlock().getRelative(blockFace);

            if ((block.getType() != Material.AIR)
                    && block.getType() != Material.SIGN_POST
                    && block.getType() != Material.WALL_SIGN) {

                ((org.bukkit.material.Sign) signState.getData())
                    .setFacingDirection(blockFace.getOppositeFace());
                signState.update();
            }
        }
    }

    public static void setSignRotation(BlockState signState, String direction) {

        BlockFace[] blockFaces = {BlockFace.EAST, BlockFace.NORTH, BlockFace.WEST, BlockFace.SOUTH};

        for (BlockFace blockFace : blockFaces) {
            if (blockFace.name().startsWith(direction.toUpperCase().substring(0, 1)))
                ((org.bukkit.material.Sign) signState.getData())
                       .setFacingDirection(blockFace);
        }
        signState.update();
    }

    /**
     * Check if a block location equals another location.
     * @param block The block location to check for.
     * @param location The location to check against.
     * @return Whether or not the block location eqauls the location.
     */

    public static boolean isBlock(Location block, Location location) {

        if (!block.getWorld().getName().equals(location.getWorld().getName()))
            return false;

        if (Math.abs(block.getBlockX() - location.getBlockX())
                > 0) return false;
        if (Math.abs(block.getBlockY() - location.getBlockY())
                > 0) return false;
        if (Math.abs(block.getBlockZ() - location.getBlockZ())
                > 0) return false;

        return true;
    }
}
