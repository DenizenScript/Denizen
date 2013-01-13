package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.events.dScriptReloadEvent;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptEngine.QueueType;
import net.aufdemrand.denizen.scripts.ScriptHelper;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>The Proximity Trigger is used to execute a script when a player moves
 * within a certain radius of a location.  If the radius are not specified,
 * then the default for both entry and exit is 5 blocks.</p>
 *
 * Example Interact Script Usage:<br/>
 * This script will execute a script when the player walks within 5 blocks of
 * the NPC that this trigger is assigned to and they were not previously within
 * the 5 block range.
 * It will also execute a script when the player walks outside of a 10 block 
 * radius of the NPC when they were not previously outside the 10 block radius.<br/>
 * <ol>
 * <tt>
 * Proximity Trigger:<br/>
 * &nbsp;&nbsp;Entry Radius: 5<br/>
 * &nbsp;&nbsp;Exit Radius: 10<br/>
 * &nbsp;&nbsp;Entry:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Script:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;- CHAT "Hello <PLAYER.NAME>! Welcome to my shop!"<br/>
 * &nbsp;&nbsp;Exit:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Script:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;- CHAT "Thanks for visiting <PLAYER.NAME>"<br/>
 * </tt>
 * </ol>
 *
 * Example Action Usage:<br/>
 * Entering and exiting NPC proximities will also trigger a couple of Actions that can be utilized. If no
 * Actions are present in the NPC Assignment Script, no action will be taken. Normal cooldown and radius
 * conditions apply.<br/>
 * <ol>
 * <tt>
 * Actions:<br/>
 * &nbsp;&nbsp;On Enter Proximity:<br/>
 * &nbsp;&nbsp;- ...<br/>
 * <br/>
 * Actions:<br/>
 * &nbsp;&nbsp;On Exit Proximity:<br/>
 * &nbsp;&nbsp;- ...<br/>
 * </tt>
 * </ol>

 * @author dbixler, aufdemrand
 */
public class ProximityTrigger extends AbstractTrigger implements Listener {

    @Override
    public void onEnable() {
        denizen.getServer().getPluginManager().registerEvents(this, denizen);
    }

    //
    // Default to 25, but dynamically set by checkMaxProximities().
    // If a Player is further than this distance from an NPC, less
    // logic is run in checking.
    //
    private static int maxProximityDistance = 25;

    /**
     * <p> This is the trigger that fires when any player moves in the entire
     * world.  The trigger ONLY checks if the player moves to a new BLOCK in the
     * world</p>
     *
     * When the trigger determines that the player has moved to a different block
     * in the world, all of the NPCs are checked for the following criteria:
     * <ol>
     * <li>Does the NPC have the trigger trait?</li>
     * <li>Is the trigger enabled?</li>
     * <li>Is the NPC available (i.e. not busy)?</li>
     * <li>Is the NPC Spawned?</li>
     * <li>Is the NPC in the same World as the player</li>
     * </ol>
     *
     * If the NPC passes all of these criteria, there are two events that can
     * occur (one or the other):
     *
     * <ol>
     * <li>If the player was outside of the NPC's radius, and moved inside the
     * radius, and there's a SCRIPT or an ENTRY SCRIPT, then execute that entry
     * script.</li>
     * <li>If the player was INSIDE of the NPC's radius, and moved OUTSIDE the
     * radius, and there's an EXIT SCRIPT, then execute that exit script.
     * </ol>
     *
     * @param event	The player's move event (which includes their location).
     */
    @EventHandler
    public void proximityTrigger(PlayerMoveEvent event) {
        //
        // Make sure that the player actually moved to a different block.
        //
        if (!event.getTo ().getBlock ().equals (event.getFrom ().getBlock ())) {

            //
            // Get block location
            //
            Location toBlockLocation = event.getTo().getBlock().getLocation();

            //
            // Iterate over all of the NPCs
            //
            Iterator<NPC>	it = CitizensAPI.getNPCRegistry().iterator();
            while (it.hasNext ()) {
                NPC	npc = it.next ();

                //
                // If the NPC doesn't have triggers, or the Proximity Trigger is not enabled,
                // then just return.
                //
                if (!npc.hasTrait(TriggerTrait.class)) {
                    continue;
                }

                if (!npc.getTrait(TriggerTrait.class).isEnabled(name)) {
                    continue;
                }

                //
                // If this NPC is not spawned or in a different world, no need to check,
                // unless the Player hasn't yet triggered an Exit Proximity after Entering
                //
                if (!npc.isSpawned() ||
                        (!npc.getBukkitEntity().getLocation().getWorld().equals(event.getPlayer().getWorld())
                        && hasExitedProximityOf(event.getPlayer(), npc))) {
                    continue;
                }

                //
                // If this NPC is more than the maxProximityDistance, skip it, unless
                // the Player hasn't yet triggerd an 'Exit Proximity' after entering.
                //
                if (!isCloseEnough(event.getPlayer(), npc) && hasExitedProximityOf(event.getPlayer(), npc)) continue;

                boolean originalDebugState = dB.debugMode;
                dB.debugMode = false;
                DenizenNPC denizenNPC = denizen.getNPCRegistry().getDenizen(npc);
                String theScript = null;

                //
                // Check to make sure the NPC has an assignment. If no assignment, a script doesn't need to be parsed,
                // but it does still need to trigger for cooldown and action purposes.
                //
                if (denizenNPC.hasAssignment()) theScript = denizenNPC.getInteractScript(event.getPlayer(), this.getClass());

                //
                // Set default ranges with information from the TriggerTrait. This allows per-npc overrides and will
                // automatically check the config for defaults.
                //
                int	entryRadius = denizenNPC.getCitizen ().getTrait (TriggerTrait.class).getRadius (name);
                int exitRadius = denizenNPC.getCitizen ().getTrait (TriggerTrait.class).getRadius (name);

                dB.debugMode = originalDebugState;

                //
                // If a script was found, it might have custom ranges.
                //
                if (theScript != null) {
                    // Need to know the step!
                    String	theStep = sH.getCurrentStep(event.getPlayer(), theScript, false);
                    try {
                        if (denizen.getScripts().contains((theScript + ".STEPS." + theStep + ".PROXIMITY TRIGGER.ENTRY RADIUS").toUpperCase()))
                            entryRadius = denizen.getScripts().getInt(theScript + ".STEPS." + theStep + ".PROXIMITY TRIGGER.ENTRY RADIUS", entryRadius);
                    } catch (NumberFormatException nfe) {
                        dB.echoDebug("Entry Radius was not an integer.  Assuming " + entryRadius + " as the radius.");
                    }
                    try {
                        if (denizen.getScripts().contains((theScript + ".STEPS." + theStep + ".PROXIMITY TRIGGER.EXIT RADIUS").toUpperCase()))
                            exitRadius = denizen.getScripts().getInt(theScript + ".STEPS." + theStep + ".PROXIMITY TRIGGER.EXIT RADIUS", exitRadius);
                    } catch (NumberFormatException nfe) {
                        dB.echoDebug("Exit Radius was not an integer.  Assuming " + exitRadius + " as the radius.");
                    }
                }

                //
                // If the user entered the range and were not previously within the
                // range, then execute the "Entry" script.
                //
                // If the user is outside the range, and was previously within the
                // range, then execute the "Exit" script.
                //
                if (npc.getBukkitEntity().getLocation().distance(toBlockLocation) <= entryRadius	&&
                        hasExitedProximityOf(event.getPlayer(), npc)) {
                    // Cooldown
                    if (!npc.getTrait(TriggerTrait.class).triggerCooldownOnly(this, event.getPlayer()))
                        continue;
                    // Remember that Player has entered proximity of the NPC
                    enterProximityOf(event.getPlayer(), npc);
                    dB.echoDebug(ChatColor.GOLD + "FOUND! NPC is in ENTERING range: '" + npc.getName() + "'");
                    // Enter Proximity Action
                    denizenNPC.action("enter proximity", event.getPlayer());
                    // Parse Interact Script
                    this.parse(denizenNPC, event.getPlayer(), theScript, true);

                } else if (npc.getBukkitEntity().getLocation().distance(toBlockLocation) >= exitRadius	&&
                        !hasExitedProximityOf(event.getPlayer(), npc)) {
                    if (!npc.getTrait(TriggerTrait.class).triggerCooldownOnly(this, event.getPlayer()))
                        continue;
                    // Remember that NPC has exited proximity.
                    exitProximityOf(event.getPlayer(), npc);
                    dB.echoDebug(ChatColor.YELLOW + "FOUND! NPC is in EXITING range: '" + npc.getName() + "'");
                    // Exit Proximity Action
                    denizenNPC.action("exit proximity", event.getPlayer());
                    // Parse Interact Script
                    this.parse(denizenNPC, event.getPlayer(), theScript, false);
                }

                dB.debugMode = originalDebugState;
            }
        }
    }

    /**
     * This parses the ProximityTrigger's script.
     *
     * @param theDenizen	The Denizen that has the proximity trigger.
     * @param thePlayer	The Player that caused the trigger to fire.
     * @param theScriptName	The script that is being executed.
     * @param entry	True if this should fire an entry script, or false if this
     * 							should fire an exit script.
     *
     * @return true if an interact script has been successfully parsed
     */
    public boolean parse (DenizenNPC theDenizen, Player thePlayer, String theScriptName, boolean entry) {
        if (theScriptName == null) {
            return false;
        }

        //
        // Get the path to the step that the player is currently on.
        //
        String	theStep = sH.getCurrentStep(thePlayer, theScriptName, false);

        //
        // Determine which scripts need to be executed:  Either the entry scripts
        // or the exit scripts.  To maintain backwards compatibility, the entry
        // scripts can either be in a section called "Entry" or as a "Script" under
        // the "Proximity Trigger" setting.
        //
        List<String> scriptsToParse;
        if (entry) {
            scriptsToParse = Arrays.asList(
                    (theScriptName + ".Steps." + theStep + ".Proximity Trigger." + ScriptHelper.scriptKey).toUpperCase(),
                    (theScriptName + ".Steps." + theStep + ".Proximity Trigger.Entry." + ScriptHelper.scriptKey).toUpperCase()
            );
        } else {
            scriptsToParse = Arrays.asList(
                    (theScriptName + ".Steps." + theStep + ".Proximity Trigger.Exit" + ScriptHelper.scriptKey).toUpperCase()
            );
        }

        //
        // Parse the scripts.
        //
        for (String path : scriptsToParse) {

            List<String> theScript = sH.getScriptContents (path);
            if (theScript != null && !theScript.isEmpty()) {
                //
                // Queue the script in the player's queue.
                //
                sB.queueScriptEntries (
                        thePlayer,
                        sB.buildScriptEntries (
                                thePlayer,
                                theDenizen,
                                theScript,
                                theScriptName,
                                theStep),
                        QueueType.PLAYER);
            }
        }

        return true;
    }


    /**
     * Checks if the Player in Proximity is close enough to be calculated.
     *
     * @param player the Player
     * @param npc the NPC
     * @return true if within maxProximityDistance in all directions
     *
     */
    private boolean isCloseEnough(Player player, NPC npc) {
        Location pLoc = player.getLocation();
        Location nLoc = npc.getBukkitEntity().getLocation();
        if (Math.abs(pLoc.getBlockX() - nLoc.getBlockX()) > maxProximityDistance) return false;
        if (Math.abs(pLoc.getBlockY() - nLoc.getBlockY()) > maxProximityDistance) return false;
        if (Math.abs(pLoc.getBlockZ() - nLoc.getBlockZ()) > maxProximityDistance) return false;
        return true;
    }

    /**
     * Checks all proximity ranges in scripts and finds the largest number. No need to
     * calculate distances that exceed the largest number.
     *
     * @param event dScriptReloadEvent, fired upon server startup or '/denizen reload scripts'
     *
     */
    @EventHandler
    public void checkMaxProximities(dScriptReloadEvent event) {

        for (String interactScript : DenizenAPI.getCurrentInstance().getScripts().getConfigurationSection("").getKeys(false)) {
            //
            // TODO: Check interact scripts for proximity triggers and ranges.
            // Find largest number, add 10, and set it as maxProximityRange.
            // For now, let's assume 25.
            //
        }
    }

    private static Map<Player, Set<Integer>> proximityTracker = new ConcurrentHashMap<Player, Set<Integer>>();

    //
    // Ensures that a Player who has entered proximity of an NPC also fires Exit Proximity.
    //
    private boolean hasExitedProximityOf(Player player, NPC npc) {
        // If Player hasn't entered proximity, it's not in the Map. Return true, must be exited.
        if (!proximityTracker.containsKey(player)) return true;
        // If Player has no entry for this NPC, return true.
        if (!proximityTracker.get(player).contains(npc.getId())) return true;
        // Entry is present, NPC has not yet triggered exit proximity.
        return false;
    }

    /**
     * Called when a 'Enter Proximity' has been called to make sure an exit
     * proximity will be called.
     *
     * @param player the Player
     * @param npc the NPC
     */
    private void enterProximityOf(Player player, NPC npc) {
        Set<Integer> npcs = new HashSet<Integer>();
        if (proximityTracker.containsKey(player))
            npcs = proximityTracker.get(player);
        npcs.add(npc.getId());
        proximityTracker.put(player, npcs);
    }

    /**
     * Called when an 'Exit Proximity' has been called. Once successfully exited,
     * a Player can enter proximity again.
     *
     * @param player the Player
     * @param npc the NPC
     */
    private void exitProximityOf(Player player, NPC npc) {
        Set<Integer> npcs = new HashSet<Integer>();
        if (proximityTracker.containsKey(player))
            npcs = proximityTracker.get(player);
        if (npcs.contains(npc.getId()))
            npcs.remove(npc.getId());
        proximityTracker.put(player, npcs);
    }

}
