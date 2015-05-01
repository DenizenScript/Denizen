package net.aufdemrand.denizen.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.tags.BukkitTagContext;

import net.aufdemrand.denizen.utilities.blocks.SafeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.tags.TagManager;
import net.aufdemrand.denizencore.utilities.CoreUtilities;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
 */
public class Utilities {


    /**
     * Gets a Location within a range that an entity can walk in.
     *
     * @param location the Location to check with
     * @param range the range around the Location
     * @return a random Location within range, or null if no Location within range is safe
     */
    public static Location getWalkableLocationNear(Location location, int range) {
        List<Location> locations = new ArrayList<Location>();
        location = location.getBlock().getLocation();

        // Loop through each location within the range
        for (double x = -(range); x <= range; x++) {
            for (double y = -(range); y <= range; y++) {
                for (double z = -(range); z <= range; z++) {
                    // Add each block location within range
                    Location loc = location.clone().add(x, y, z);
                    if (checkLocation(location, loc, range) && isWalkable(loc)) {
                        locations.add(loc);
                    }
                }
            }
        }

        // No safe Locations found
        if (locations.isEmpty())
            return null;

        // Return a random Location from the list
        return locations.get(CoreUtilities.getRandom().nextInt(locations.size()));
    }


    // TODO: Javadocs, comments
    //
    public static boolean isWalkable(Location location) {
        return !SafeBlock.blockIsSafe(location.clone().subtract(0, 1, 0).getBlock().getType())
                && SafeBlock.blockIsSafe(location.getBlock().getType())
                && SafeBlock.blockIsSafe(location.clone().add(0, 1, 0).getBlock().getType());
    }



    // TODO: Javadocs, comments
    //
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
        String replacer = String.valueOf((char)0x04);
        // Get formats from Settings, and fill in <TEXT>
        String talkFormat = Settings.chatToNpcFormat()
                .replaceAll("(?i)<TEXT>", replacer);
        String bystanderFormat = Settings.chatToNpcOverheardFormat()
                .replaceAll("(?i)<TEXT>", replacer);

        // Fill in tags // TODO: Debug option?
        talkFormat = TagManager.tag(talkFormat, new BukkitTagContext(player, npc, false, null, true, null)).replace(replacer, message);
        bystanderFormat = TagManager.tag(bystanderFormat, new BukkitTagContext(player, npc, false, null, true, null)).replace(replacer, message);

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
     * Checks if c is in between a and b, or equal to a or b.
     *
     * @param a first number
     * @param b second number
     * @param c number to check if between
     * @return  true if c is in between.
     */
    public static boolean isBetween(double a, double b, double c) {
        return b > a ?  (c > a && c < b): (c > b && c < a);
    }


    /**
     * Finds the closest NPC to a particular location.
     *
     * @param location    The location to find the closest NPC to.
     * @param range    The maximum range to look for the NPC.
     * @return    The closest NPC to the location, or null if no NPC was found
     *                     within the range specified.
     */
    public static dNPC getClosestNPC_ChatTrigger (Location location, int range) {
        dNPC closestNPC = null;
        double closestDistance = Math.pow(range, 2);
        // TODO: Why is this manually iterating?
        Iterator<dNPC> it = DenizenAPI.getSpawnedNPCs().iterator();
        while (it.hasNext()) {
            dNPC npc = it.next();
            Location loc = npc.getLocation();
            if (npc.getCitizen().hasTrait(TriggerTrait.class) && npc.getTriggerTrait().hasTrigger("CHAT") &&
                    loc.getWorld().equals(location.getWorld())
                    && loc.distanceSquared(location) < closestDistance) {
                closestNPC = npc;
                closestDistance = npc.getLocation().distanceSquared(location);
            }
        }
        return closestNPC;
    }


    /**
     * Checks entity's location against a Location (with leeway). Should be faster than
     * bukkit's built in Location.distance(Location) since there's no sqrt math.
     *
     * Thanks chainsol :)
     *
     * @return true if within the specified location, false otherwise.
     */
    public static boolean checkLocation(LivingEntity entity, Location theLocation, double theLeeway) {
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
     * @return true if within the specified location, false otherwise.
     */
    public static boolean checkLocation(Location baseLocation, Location theLocation, double theLeeway) {

        if (!baseLocation.getWorld().getName().equals(theLocation.getWorld().getName()))
            return false;

        return baseLocation.distanceSquared(theLocation) < theLeeway * theLeeway;
    }

    /**
     * Set the lines on a sign to the strings in a string array
     *
     * @param sign  The sign
     * @param lines  The string array
     */
    public static void setSignLines(Sign sign, String[] lines) {

        for (int n = 0; n < 4; n++) {
            sign.setLine(n, lines[n]);
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

    // TODO: Javadocs, comments
    //
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
     * @return Whether or not the block location equals the location.
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


    /**
     * Extract a file from a zip or jar.
     * @param jarFile The zip/jar file to use
     * @param fileName Which file to extract
     * @param destDir Where to extract it to
     */
    public static void extractFile(File jarFile, String fileName, String destDir) {
        java.util.jar.JarFile jar = null;

        try {
            jar = new java.util.jar.JarFile(jarFile);
            java.util.Enumeration myEnum = jar.entries();
            while (myEnum.hasMoreElements()) {
                java.util.jar.JarEntry file = (java.util.jar.JarEntry) myEnum.nextElement();
                if (file.getName().equalsIgnoreCase(fileName)) {
                    java.io.File f = new java.io.File(destDir + "/" + file.getName());
                    if (file.isDirectory()) {
                        continue;
                    }
                    java.io.InputStream is = jar.getInputStream(file);
                    java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
                    while (is.available() > 0)
                        fos.write(is.read());
                    fos.close();
                    is.close();
                    return;
                }
            }
            dB.echoError(fileName + " not found in the jar!");

        } catch (IOException e) {
            dB.echoError(e);

        } finally {
            if (jar != null) {
                try {
                    jar.close();
                } catch (IOException e) {
                    dB.echoError(e);
                }
            }
        }
    }

    private final static String colors = "0123456789abcdefklmnorABCDEFKLMNOR";

    public static String generateRandomColors(int count) {
        String ret = "";
        for (int i = 0; i < count; i++) {
            ret += String.valueOf(ChatColor.COLOR_CHAR) + colors.charAt(CoreUtilities.getRandom().nextInt(34));
        }
        return ret;
    }
}
