package net.aufdemrand.denizen.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.DenizenCharacter;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class GetDenizen {

    private Denizen plugin;


    /**
     * Checks against the interactCooldown for a Player to see if it has allowed
     * enough time to interact.
     */

    public boolean checkCooldown(Player thePlayer) {
        if (!Denizen.interactCooldown.containsKey(thePlayer)) return true;
        if (System.currentTimeMillis() >= Denizen.interactCooldown.get(thePlayer)) return true;

        return false;
    }

    public boolean checkLocationCooldown(Player thePlayer) {
        if (!Denizen.locationCooldown.containsKey(thePlayer)) return true;
        if (System.currentTimeMillis() >= Denizen.locationCooldown.get(thePlayer)) return true;
        return false;
    }

    /**
     * Gets a NPC object of the closest Denizen to the specified Player.
     */

    public NPC getClosest (Player thePlayer, int Range) {
        double closestDistance = Double.valueOf(String.valueOf(Range));
        NPC closestDenizen = null;

        Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
        if (DenizenNPCs.isEmpty()) return null;

        List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
        for (NPC aDenizen : DenizenList) {
            if (!aDenizen.isSpawned() || aDenizen.getBukkitEntity().getWorld() != thePlayer.getWorld())
                continue;
            double distance = aDenizen.getBukkitEntity().getLocation().distance(thePlayer.getLocation());
            if (distance < closestDistance) {
                closestDenizen = aDenizen; 
                closestDistance = distance;
            }
        }

        return closestDenizen;
    }





    /*
     * getInRange
     * 
     * Gets a List<NPC> of Denizens within a range of the specified Player.
     * 
     */

    public List<NPC> getInRange (Player thePlayer, int theRange) {

        Collection<NPC> DenizenNPCs = CitizensAPI.getNPCRegistry().getNPCs(DenizenCharacter.class);
        if (DenizenNPCs.isEmpty())
            return Collections.emptyList();

        List<NPC> DenizensWithinRange = new ArrayList<NPC>();
        List<NPC> DenizenList = new ArrayList<NPC>(DenizenNPCs);
        for (NPC aDenizenList : DenizenList) {
            if (aDenizenList.isSpawned()
                    && aDenizenList.getBukkitEntity().getWorld().equals(thePlayer.getWorld()) 
                    && aDenizenList.getBukkitEntity().getLocation().distance(thePlayer.getLocation()) < theRange)

                DenizensWithinRange.add(aDenizenList);
        }

        return DenizensWithinRange;
    }





    /**
     * Retrieves a Location from the Denizen's stored location or block
     * bookmark.
     */

    public Location getBookmark(String theDenizen, String nameOfLocation, String BlockOrLocation) {
        plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");		
        List<String> locationList = null;
        String[] theLocation = null;
        Location locationBookmark = null;

        try {

            if (BlockOrLocation.equalsIgnoreCase("block")) locationList = plugin.getSaves().getStringList("Denizens." + theDenizen + ".Bookmarks.Block");	
            else if (BlockOrLocation.equalsIgnoreCase("location")) locationList = plugin.getSaves().getStringList("Denizens." + theDenizen + ".Bookmarks.Location");

            for (String thisLocation : locationList) {
                String theName = thisLocation.split(" ", 2)[0];
                if (theName.equalsIgnoreCase(nameOfLocation)) theLocation = thisLocation.split(" ", 2)[1].split(";");
            }

            if (theLocation != null && BlockOrLocation.equalsIgnoreCase("location")) {			
                locationBookmark = 
                        new Location(plugin.getServer().getWorld(theLocation[0]),
                                Double.parseDouble(theLocation[1]), Double.parseDouble(theLocation[2] + 1),
                                Double.parseDouble(theLocation[3]), Float.parseFloat(theLocation[4]),
                                Float.parseFloat(theLocation[5]));
            }

            else if (theLocation != null && BlockOrLocation.equalsIgnoreCase("block")) {
                locationBookmark = 
                        new Location(plugin.getServer().getWorld(theLocation[0]),
                                Double.parseDouble(theLocation[1]), Double.parseDouble(theLocation[2]),
                                Double.parseDouble(theLocation[3]));
            }

        } catch(Throwable error) {
            Bukkit.getLogger().info("Denizen method getBookmark: An error has occured.");
            Bukkit.getLogger().info("--- Error follows: " + error);
        }

        return locationBookmark;		

    }




    /**
     * Makes a Denizen talk to a Player.
     *
     * @param  theDenizen  the Citizens2 NPC object that is doing the talking.
     * @param  thePlayer  the Bukkit Player object to talk to.
     * @param  commandArgs 
     */

    public void talkToPlayer(NPC theDenizen, Player thePlayer, String thePlayerMessage, String theBystanderMessage, String messageType) {

        int theRange = 0;
        // can use enum here
        if (messageType.equalsIgnoreCase("SHOUT")) {
            theRange = Denizen.settings.NpcToPlayerShoutRangeInBlocks();
        }

        else if (messageType.equalsIgnoreCase("WHISPER")) {
            theRange = Denizen.settings.NpcToPlayerWhisperRangeInBlocks();
        }

        else if (messageType.equalsIgnoreCase("EMOTE")) {
            theRange = Denizen.settings.NpcEmoteRangeInBlocks();
            thePlayer.sendMessage(theBystanderMessage);
        }

        else {
            theRange = Denizen.settings.NpcToPlayerChatRangeInBlocks();
        }

        if (thePlayerMessage != null && !thePlayerMessage.equals("shhh...don't speak!")) thePlayer.sendMessage(thePlayerMessage);

        if (theRange > 0 && (Denizen.settings.BystandersHearNpcToPlayerChat() || thePlayerMessage == null)
                && theBystanderMessage != null) {
            for (Player otherPlayer : Denizen.getPlayer.getInRange(theDenizen.getBukkitEntity(), theRange, thePlayer)) {
                otherPlayer.sendMessage(theBystanderMessage);
            }
        }
    }
}
