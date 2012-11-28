package net.aufdemrand.denizen.npc;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.npc.actions.ActionHandler;
import net.aufdemrand.denizen.utilities.debugging.Debugger;

import net.citizensnpcs.api.ai.event.NavigationCompleteEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import net.citizensnpcs.api.event.NPCSpawnEvent;
import net.citizensnpcs.api.npc.NPC;

public class DenizenNPCRegistry implements Listener {

    private Map<NPC, DenizenNPC> denizenNPCs = new ConcurrentHashMap<NPC, DenizenNPC>();

    private Denizen plugin;
    private Debugger dB;
    private ActionHandler actionHandler;
    
    public ActionHandler getActionHandler() {
        return actionHandler;
    }

    public DenizenNPCRegistry(Denizen denizen) {
        plugin = denizen;
        dB = plugin.getDebugger();
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        actionHandler = new ActionHandler(plugin);
    }

    public void registerNPC(NPC npc) {
        if (!denizenNPCs.containsKey(npc)) {
            denizenNPCs.put(npc, new DenizenNPC(npc));
        }
        dB.log("Constructing Denizen NPC " + getDenizen(npc).toString() + 
                "  List size now: " + denizenNPCs.size());
    }

    public DenizenNPC getDenizen(NPC npc) {
        if (!denizenNPCs.containsKey(npc))
            registerNPC(npc);
        return denizenNPCs.get(npc);
    }

    public boolean isDenizenNPC (NPC npc) {
        if (denizenNPCs.containsKey(npc)) 
            return true ;
        else return false; 
    }

    public Map<NPC, DenizenNPC> getDenizens() {
        Iterator<Entry<NPC, DenizenNPC>> it = denizenNPCs.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<NPC, DenizenNPC> npc = (Map.Entry<NPC, DenizenNPC>)it.next();
            try {
                npc.getKey().getBukkitEntity();
            } catch (NullPointerException e) {
                denizenNPCs.remove(npc.getKey());
                dB.log(ChatColor.RED + "Removed NPC from DenizenRegistry. " + ChatColor.WHITE + "The bukkit entity has been removed.");
            }
        }
        return denizenNPCs;
    }

    @EventHandler 
    public void onSpawn(NPCSpawnEvent event) {
        registerNPC(event.getNPC());
        // On Spawn action
        plugin.getNPCRegistry().getDenizen(event.getNPC()).action("spawn", null);
    }

    @EventHandler 
    public void onRemove(NPCRemoveEvent event) {
        plugin.getNPCRegistry().getDenizen(event.getNPC()).action("remove", null);
        if (isDenizenNPC(event.getNPC()))
            denizenNPCs.remove(event.getNPC());
        dB.log(ChatColor.RED + "Deconstructing Denizen NPC " + event.getNPC().getName() + "/" + event.getNPC().getId() + 
            "  List size now: " + denizenNPCs.size());
    }
    
    @EventHandler
    public void onNavigationComplete(NavigationCompleteEvent event) {
        
    }

    public DenizenNPC getClosestDenizen (Player thePlayer, int Range) {
        Double closestDistance = Double.valueOf(String.valueOf(Range));
        DenizenNPC closestDenizen = null;
        if (getDenizens().isEmpty()) return null;
        for (DenizenNPC aDenizen : getDenizens().values()) {
            if (aDenizen.isSpawned()
                    && aDenizen.getWorld().equals(thePlayer.getWorld())
                    && aDenizen.getLocation().distance(thePlayer.getLocation()) < closestDistance ) {
                closestDenizen = aDenizen; 
                closestDistance = aDenizen.getLocation().distance(thePlayer.getLocation());
            }
        }
        return closestDenizen;
    }

    public List<DenizenNPC> getDenizensInRange (Player thePlayer, int theRange) {
        List<DenizenNPC> DenizensWithinRange = new ArrayList<DenizenNPC>();
        if (plugin.getNPCRegistry().getDenizens().isEmpty()) return DenizensWithinRange;
        for (DenizenNPC aDenizenList : plugin.getNPCRegistry().getDenizens().values()) {
            if (aDenizenList.isSpawned()
                    && aDenizenList.getWorld().equals(thePlayer.getWorld()) 
                    && aDenizenList.getLocation().distance(thePlayer.getLocation()) < theRange)
                DenizensWithinRange.add(aDenizenList);
        }
        return DenizensWithinRange;
    }

    public List<Player> getPlayersInRange (LivingEntity theEntity, int theRange) {
        List<Player> PlayersWithinRange = new ArrayList<Player>();
        Player[] DenizenPlayers = plugin.getServer().getOnlinePlayers();
        for (Player aPlayer : DenizenPlayers) {
            if (aPlayer.isOnline() 
                    && aPlayer.getWorld().equals(theEntity.getWorld()) 
                    && aPlayer.getLocation().distance(theEntity.getLocation()) < theRange)
                PlayersWithinRange.add(aPlayer);
        }
        return PlayersWithinRange;
    }

    public List<Player> getPlayersInRange (LivingEntity theEntity, int theRange, Player excludePlayer) {
        List<Player> PlayersWithinRange = getPlayersInRange(theEntity, theRange);
        if (excludePlayer != null) PlayersWithinRange.remove(excludePlayer);
        return PlayersWithinRange;
    }

    /**
     * Checks entity's location against a Location (with leeway). Should be faster than
     * bukkit's built in Location.distance(Location) since there's no sqrt math.
     * 
     * Thanks chainsol :)
     */

    public boolean checkLocation(LivingEntity entity, Location theLocation, int theLeeway) {

        if (!entity.getWorld().getName().equals(theLocation.getWorld().getName()))
            return false;

        if (Math.abs(entity.getLocation().getBlockX() - theLocation.getBlockX()) 
                > theLeeway) return false;
        if (Math.abs(entity.getLocation().getBlockY() - theLocation.getBlockY()) 
                > theLeeway) return false;
        if (Math.abs(entity.getLocation().getBlockZ() - theLocation.getBlockZ()) 
                > theLeeway) return false;

        return true;
    }


    // TODO: Use paginator on info-click

    //    public void showInfo(Player thePlayer, DenizenNPC theDenizen) {
    //
    //        thePlayer.sendMessage(ChatColor.GOLD + "------ Denizen Info ------");
    //
    //        /* Show Citizens NPC info. */
    //
    //        thePlayer.sendMessage(ChatColor.GRAY + "C2 NPCID: " + ChatColor.GREEN + theDenizen.getId() + ChatColor.GRAY + "   Name: " + ChatColor.GREEN + theDenizen.getName() + ChatColor.GRAY + "   HPs: " + ChatColor.GREEN + theDenizen.getEntity().getHealth() + ChatColor.GRAY + "   GOAL CNTRLR: " + ChatColor.GREEN + String.valueOf(!Boolean.valueOf(theDenizen.getCitizen().getDefaultGoalController().isPaused())));
    //        thePlayer.sendMessage(ChatColor.GRAY + "PF RANGE: " + ChatColor.GREEN + theDenizen.getNavigator().getDefaultParameters().range() +  "   " + ChatColor.GRAY + "SPEED: " + ChatColor.GREEN + String.valueOf(theDenizen.getNavigator().getDefaultParameters().speed()) + "    " + ChatColor.GRAY + "AVOID WATER: " + ChatColor.GREEN + theDenizen.getNavigator().getDefaultParameters().avoidWater());
    //        thePlayer.sendMessage(ChatColor.GRAY + "NAVIGATING: " + ChatColor.GREEN + theDenizen.getNavigator().isNavigating() +  "   " + ChatColor.GRAY + "STATIONARY TICKS: " + ChatColor.GREEN + theDenizen.getNavigator().getDefaultParameters().stationaryTicks() +  "   " + ChatColor.GRAY + "PUSHABLE: " + ChatColor.GREEN + theDenizen.getCitizen().getTrait(PushableTrait.class).isToggled());
    //
    //        thePlayer.sendMessage("");
    //
    //        thePlayer.sendMessage(ChatColor.GRAY + "Trigger Status:");
    //        // for (String line : plugin.getSpeechEngine().getMultilineText(theDenizen.getCitizensEntity().getTrait(DenizenTrait.class).listTriggers()))
    //        //	thePlayer.sendMessage(line);
    //        thePlayer.sendMessage("");
    //
    //        /* Show Assigned Scripts. */
    //
    //        boolean scriptsPresent = false;
    //        thePlayer.sendMessage(ChatColor.GRAY + "Interact Scripts:");
    //        if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Interact Scripts")) {
    //            if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts").isEmpty()) scriptsPresent = true;
    //            for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Interact Scripts"))
    //                thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + scriptEntry);
    //        }
    //        if (plugin.getAssignments().contains("Denizens." + theDenizen.getId() + ".Interact Scripts")) {
    //            if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getId() + ".Interact Scripts").isEmpty()) scriptsPresent = true;
    //            for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getId() + ".Interact Scripts"))
    //                thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.YELLOW + scriptEntry);
    //        }
    //        if (!scriptsPresent) thePlayer.sendMessage(ChatColor.RED + "  No scripts assigned!");
    //
    //        thePlayer.sendMessage("");
    //
    //        /* Show Scheduled Activities */
    //        boolean activitiesPresent = false;
    //        thePlayer.sendMessage(ChatColor.GRAY + "Scheduled Activities:");
    //        if (plugin.getAssignments().contains("Denizens." + theDenizen.getName() + ".Scheduled Activities")) {
    //            if (!plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities").isEmpty()) activitiesPresent = true;
    //            for (String scriptEntry : plugin.getAssignments().getStringList("Denizens." + theDenizen.getName() + ".Scheduled Activities"))
    //                thePlayer.sendMessage(ChatColor.GRAY + "- " + ChatColor.GREEN + scriptEntry);
    //        }
    //        if (!activitiesPresent) thePlayer.sendMessage(ChatColor.RED + "  No activities scheduled!");
    //        thePlayer.sendMessage("");
    //
    //        /* Show Bookmarks */
    //
    //        DecimalFormat lf = new DecimalFormat("###.##");
    //        boolean bookmarksPresent = false;
    //        thePlayer.sendMessage(ChatColor.GRAY + "Bookmarks:");
    //
    //        /* Location Bookmarks */
    //        if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
    //            if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location").isEmpty()) bookmarksPresent = true;
    //            for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Location")) {
    //                if (bookmarkEntry.split(";").length >= 6) {
    //                    thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.GREEN + "LOCATION " + ChatColor.GRAY + "Name: " + ChatColor.GREEN + bookmarkEntry.split(" ")[0]
    //                            + ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
    //                    thePlayer.sendMessage(" "
    //                            + ChatColor.GRAY + "  at X: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
    //                            + ChatColor.GRAY + " Y: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
    //                            + ChatColor.GRAY + " Z: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
    //                            + ChatColor.GRAY + " Pitch: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[4]))
    //                            + ChatColor.GRAY + " Yaw: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[5])));
    //                }
    //            }
    //        }
    //
    //        if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
    //            if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location").isEmpty()) bookmarksPresent = true;
    //            for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Location")) {
    //                if (bookmarkEntry.split(";").length >= 6) {
    //                    thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.YELLOW + "LOCATION " + ChatColor.GRAY + "Name: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[0]
    //                            + ChatColor.GRAY + " in World: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[1].split(";")[0]);
    //                    thePlayer.sendMessage(" "
    //                            + ChatColor.GRAY + "  at X: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
    //                            + ChatColor.GRAY + " Y: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
    //                            + ChatColor.GRAY + " Z: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
    //                            + ChatColor.GRAY + " Pitch: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[4]))
    //                            + ChatColor.GRAY + " Yaw: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[5])));
    //                }
    //            }
    //        }
    //
    //        /* Block Bookmarks */
    //        if (plugin.getSaves().contains("Denizens." + theDenizen.getName() + ".Bookmarks.Block")) {
    //            if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block").isEmpty()) bookmarksPresent = true;
    //            for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getName() + ".Bookmarks.Block")) {
    //                if (bookmarkEntry.split(";").length >= 4) {
    //                    thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.GREEN + "BLOCK " + ChatColor.GRAY + "Name: " + ChatColor.GREEN + bookmarkEntry.split(" ")[0]
    //                            + ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
    //                    thePlayer.sendMessage(" "
    //                            + ChatColor.GRAY + "  at X: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
    //                            + ChatColor.GRAY + " Y: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
    //                            + ChatColor.GRAY + " Z: " + ChatColor.GREEN + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
    //                            + ChatColor.GRAY + " Material: " + ChatColor.GREEN + plugin.bookmarks.get(theDenizen, bookmarkEntry.split(" ")[0], BookmarkType.BLOCK).getBlock().getType().toString());
    //                }
    //            }
    //        }
    //
    //        if (plugin.getSaves().contains("Denizens." + theDenizen.getId() + ".Bookmarks.Block")) {
    //            if (!plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Block").isEmpty()) bookmarksPresent = true;
    //            for (String bookmarkEntry : plugin.getSaves().getStringList("Denizens." + theDenizen.getId() + ".Bookmarks.Block")) {
    //                if (bookmarkEntry.split(";").length >= 4) {
    //                    thePlayer.sendMessage(ChatColor.GRAY + "- Type: " + ChatColor.YELLOW + "BLOCK " + ChatColor.GRAY + "Name: " + ChatColor.YELLOW + bookmarkEntry.split(" ")[0]
    //                            + ChatColor.GRAY + " in World: " + ChatColor.GREEN + bookmarkEntry.split(" ")[1].split(";")[0]);
    //                    thePlayer.sendMessage(" "
    //                            + ChatColor.GRAY + "  at X: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[1]))
    //                            + ChatColor.GRAY + " Y: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[2]))
    //                            + ChatColor.GRAY + " Z: " + ChatColor.YELLOW + lf.format(Double.valueOf(bookmarkEntry.split(";")[3]))
    //                            + ChatColor.GRAY + " Material: " + ChatColor.YELLOW + plugin.bookmarks.get(theDenizen, bookmarkEntry.split(" ")[0], BookmarkType.BLOCK).getBlock().getType().toString());
    //                }
    //            }
    //        }
    //
    //        if (!bookmarksPresent) thePlayer.sendMessage(ChatColor.RED + "  No bookmarks defined!");
    //        thePlayer.sendMessage("");		
    //    }

}
