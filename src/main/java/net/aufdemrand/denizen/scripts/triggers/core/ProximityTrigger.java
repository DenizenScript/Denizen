package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.events.dScriptReloadEvent;
import net.aufdemrand.denizen.npc.dNPC;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>The Proximity Trigger is used to execute a script when a player moves
 * within a certain radius of a location.  If the radius are not specified,
 * then the default for entry, exit, and move is 5 blocks.</p>
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
 * &nbsp;&nbsp;Move:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;Script:<br/>
 * &nbsp;&nbsp;&nbsp;&nbsp;- CHAT "Stop pacing <PLAYER.NAME>!"<br/>
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
 * &nbsp;&nbsp;On Move Proximity:<br/>
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
    private static int maxProximityDistance = 50;

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
     * If the NPC passes all of these criteria, there are three events that can
     * occur (only one of them):
     *
     * <ol>
     * <li>If the player was outside of the NPC's radius, and moved inside the
     * radius, and there's a SCRIPT or an ENTRY SCRIPT, then execute that entry
     * script.</li>
     * <li>If the player was INSIDE of the NPC's radius, and moved OUTSIDE the
     * radius, and there's an EXIT SCRIPT, then execute that exit script.
     * <li>If the player was INSIDE of the NPC's radius, and moved WITHIN the
     * radius, and there's an MOVE SCRIPT, then execute that move script.
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
            Iterator<dNPC>	it = DenizenAPI.getCurrentInstance().getNPCRegistry().getSpawnedNPCs().iterator();
            while (it.hasNext ()) {
                dNPC npc = it.next ();

                //
                // If the NPC doesn't have triggers, or the Proximity Trigger is not enabled,
                // then just return.
                //
                if (!npc.getCitizen().hasTrait(TriggerTrait.class)) continue;

                if (!npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name)) continue;

                //
                // If this NPC is not spawned or in a different world, no need to check,
                // unless the Player hasn't yet triggered an Exit Proximity after Entering
                //
                if (!npc.isSpawned() ||
                        (!npc.getWorld().equals(event.getPlayer().getWorld())
                        && hasExitedProximityOf(event.getPlayer(), npc))) continue;

                //
                // If this NPC is more than the maxProximityDistance, skip it, unless
                // the Player hasn't yet triggered an 'Exit Proximity' after entering.
                //
                if (!isCloseEnough(event.getPlayer(), npc)
                        && hasExitedProximityOf(event.getPlayer(), npc)) continue;

                // Get the player
                Player player = event.getPlayer();

                //
                // Check to make sure the NPC has an assignment. If no assignment, a script doesn't need to be parsed,
                // but it does still need to trigger for cooldown and action purposes.
                //
                InteractScriptContainer script = npc.getInteractScriptQuietly(event.getPlayer(), this.getClass());

                //
                // Set default ranges with information from the TriggerTrait. This allows per-npc overrides and will
                // automatically check the config for defaults.
                //
                double entryRadius = npc.getTriggerTrait().getRadius(name);
                double exitRadius = npc.getTriggerTrait().getRadius(name);
                double moveRadius = npc.getTriggerTrait().getRadius(name);


                //
                // If a script was found, it might have custom ranges.
                //
                if (script != null) {
                    try {
                        if (script.hasTriggerOptionFor(getClass(), player, null, "ENTRY RADIUS"))
                            entryRadius = Integer.valueOf(script.getTriggerOptionFor(getClass(), player, null, "ENTRY RADIUS"));
                    } catch (NumberFormatException nfe) {
                        dB.echoDebug("Entry Radius was not an integer.  Assuming " + entryRadius + " as the radius.");
                    }
                    try {
                        if (script.hasTriggerOptionFor(getClass(), player, null, "EXIT RADIUS"))
                            exitRadius = Integer.valueOf(script.getTriggerOptionFor(getClass(), player, null, "EXIT RADIUS"));
                    } catch (NumberFormatException nfe) {
                        dB.echoDebug("Exit Radius was not an integer.  Assuming " + exitRadius + " as the radius.");
                    }
                    try {
                        if (script.hasTriggerOptionFor(getClass(), player, null, "MOVE RADIUS"))
                            moveRadius = Integer.valueOf(script.getTriggerOptionFor(getClass(), player, null, "MOVE RADIUS"));
                    } catch (NumberFormatException nfe) {
                        dB.echoDebug("Move Radius was not an integer.  Assuming " + moveRadius + " as the radius.");
                    }
                }

                Location npcLocation = npc.getLocation();

                //
                // If the Player switches worlds while in range of an NPC, trigger still needs to
                // fire since technically they have exited proximity. Let's check that before
                // trying to calculate a distance between the Player and NPC, which will throw
                // an exception if worlds do not match.
                //
                boolean playerChangedWorlds = false;
                if (npcLocation.getWorld() != event.getPlayer().getWorld())
                    playerChangedWorlds = true;

                //
                // If the user is outside the range, and was previously within the
                // range, then execute the "Exit" script.
                //
                // If the user entered the range and were not previously within the
                // range, then execute the "Entry" script.
                //
                // If the user was previously within the range and moved, then execute
                // the "Move" script.
                //
                boolean exitedProximity = hasExitedProximityOf(event.getPlayer(), npc);
                double distance = 0;
                if (!playerChangedWorlds) distance = npcLocation.distance(toBlockLocation);

                if (!exitedProximity
                    && (playerChangedWorlds || distance >= exitRadius)) {
                    if (!npc.getTriggerTrait().triggerCooldownOnly(this, event.getPlayer()))
                        continue;
                    // Remember that NPC has exited proximity.
                    exitProximityOf(event.getPlayer(), npc);
                    dB.echoDebug(ChatColor.YELLOW + "FOUND! NPC is in EXITING range: '" + npc.getName() + "'");
                    // Exit Proximity Action
                    npc.action("exit proximity", event.getPlayer());
                    // Parse Interact Script
                    parse(npc, player, script, "EXIT");
                }
                else if (exitedProximity && distance <= entryRadius) {
                    // Cooldown
                    if (!npc.getTriggerTrait().triggerCooldownOnly(this, event.getPlayer()))
                        continue;
                    // Remember that Player has entered proximity of the NPC
                    enterProximityOf(event.getPlayer(), npc);
                    // Enter Proximity Action
                    npc.action("enter proximity", event.getPlayer());
                    // Parse Interact Script
                    parse(npc, player, script, "ENTRY");
                }
                else if (!exitedProximity && distance <= moveRadius) {
                    // TODO: Remove this? Constantly cooling down on move may make
                    // future entry/exit proximities 'lag' behind.  Temporarily removing
                    // cooldown on 'move proximity'.
                    // if (!npc.getTriggerTrait().triggerCooldownOnly(this, event.getPlayer()))
                    //     continue;
                   // Move Proximity Action
                   npc.action("move proximity", event.getPlayer());
                   // Parse Interact Script
                   parse(npc, player, script, "MOVE");
                }
            }
        }
    }

    /**
     * Checks if the Player in Proximity is close enough to be calculated.
     *
     * @param player the Player
     * @param npc the NPC
     * @return true if within maxProximityDistance in all directions
     *
     */
    private boolean isCloseEnough(Player player, dNPC npc) {
        Location pLoc = player.getLocation();
        Location nLoc = npc.getLocation();
        if (Math.abs(pLoc.getX() - nLoc.getX()) > maxProximityDistance) return false;
        if (Math.abs(pLoc.getY() - nLoc.getY()) > maxProximityDistance) return false;
        if (Math.abs(pLoc.getZ() - nLoc.getZ()) > maxProximityDistance) return false;
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

        for (String script : ScriptRegistry._getScriptNames()) {
            //
            // TODO: Check interact scripts for proximity triggers and ranges.
            // Find largest number, add 10, and set it as maxProximityRange.
            // For now, let's assume 25.
            //
        }
    }

    private static Map<Player, Set<Integer>> proximityTracker = new ConcurrentHashMap<Player, Set<Integer>>(8, 0.9f, 1);

    //
    // Ensures that a Player who has entered proximity of an NPC also fires Exit Proximity.
    //
    public boolean hasExitedProximityOf(Player player, dNPC npc) {
        // If Player hasn't entered proximity, it's not in the Map. Return true, must be exited.
        Set<Integer> existing = proximityTracker.get(player);
        if (existing == null) return true;
        // If Player has no entry for this NPC, return true.
        if (!existing.contains(npc.getId())) return true;
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
    private void enterProximityOf(Player player, dNPC npc) {
        Set<Integer> npcs = proximityTracker.get(player);
        if (npcs == null) {
            npcs = new HashSet<Integer>();
            proximityTracker.put(player, npcs);
        }
        npcs.add(npc.getId());
    }

    /**
     * Called when an 'Exit Proximity' has been called. Once successfully exited,
     * a Player can enter proximity again.
     *
     * @param player the Player
     * @param npc the NPC
     */
    private void exitProximityOf(Player player, dNPC npc) {
        Set<Integer> npcs = proximityTracker.get(player);
        if (npcs == null) {
            npcs = new HashSet<Integer>();
            proximityTracker.put(player, npcs);
        }
        npcs.remove(npc.getId());
    }

}
