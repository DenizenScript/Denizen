package net.aufdemrand.denizen.scripts.triggers.core;

import net.aufdemrand.denizen.events.bukkit.ScriptReloadEvent;
import net.aufdemrand.denizen.npc.dNPCRegistry;
import net.aufdemrand.denizen.npc.traits.TriggerTrait;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.containers.core.InteractScriptContainer;
import net.aufdemrand.denizen.scripts.triggers.AbstractTrigger;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.scripts.ScriptRegistry;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>The Proximity Trigger is used to execute a script when a player moves
 * within a certain radius of a location.  If the radius are not specified,
 * then the default for entry, exit, and move is 5 blocks.</p>
 * <p/>
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
 * <p/>
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
 *
 * @author dbixler, aufdemrand
 */
public class ProximityTrigger extends AbstractTrigger implements Listener {
    /*
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
     */

    //
    // Default to 75, but dynamically set by checkMaxProximities().
    // If a Player is further than this distance from an NPC, less
    // logic is run in checking.
    //
    private static int maxProximityDistance = 75; // TODO: is this reasonable to have?


    // <--[action]
    // @Actions
    // enter proximity
    //
    // @Triggers when a player enters the NPC's proximity trigger's radius.
    //
    // @Context
    // None
    //
    // -->
    // <--[action]
    // @Actions
    // exit proximity
    //
    // @Triggers when a player exits the NPC's proximity trigger's radius.
    //
    // @Context
    // None
    //
    // -->
    // <--[action]
    // @Actions
    // move proximity
    //
    // @Triggers when a player moves inside the NPC's proximity trigger's radius.
    //
    // @Context
    // None
    //
    // -->
    // Technically defined in TriggerTrait, but placing here instead.
    // <--[action]
    // @Actions
    // proximity
    //
    // @Triggers when a player moves inside the NPC's proximity trigger's radius.
    //
    // @Context
    // None
    //
    // -->
    int taskID = -1;

    @Override
    public void onEnable() {
        Bukkit.getServer().getPluginManager().registerEvents(this, DenizenAPI.getCurrentInstance());

        final ProximityTrigger trigger = this;

        taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(DenizenAPI.getCurrentInstance(), new Runnable() {
            @Override
            public void run() {

                //
                // Iterate over all of the NPCs
                //
                Iterator<dNPC> it = dNPCRegistry.getSpawnedNPCs().iterator();
                while (it.hasNext()) {
                    dNPC npc = it.next();
                    if (npc == null) {
                        continue;
                    }
                    if (npc.getCitizen() == null) {
                        continue;
                    }

                    //
                    // If the NPC doesn't have triggers, or the Proximity Trigger is not enabled,
                    // then just return.
                    //
                    if (!npc.getCitizen().hasTrait(TriggerTrait.class)) {
                        continue;
                    }

                    if (!npc.getCitizen().getTrait(TriggerTrait.class).isEnabled(name)) {
                        continue;
                    }

                    if (!npc.isSpawned()) {
                        continue;
                    }

                    // Loop through all players
                    for (Player BukkitPlayer : Bukkit.getOnlinePlayers()) {

                        //
                        // If this NPC is not spawned or in a different world, no need to check,
                        // unless the Player hasn't yet triggered an Exit Proximity after Entering
                        //
                        if (!npc.getWorld().equals(BukkitPlayer.getWorld())
                                && hasExitedProximityOf(BukkitPlayer, npc)) {
                            continue;
                        }

                        //
                        // If this NPC is more than the maxProximityDistance, skip it, unless
                        // the Player hasn't yet triggered an 'Exit Proximity' after entering.
                        //
                        if (!isCloseEnough(BukkitPlayer, npc)
                                && hasExitedProximityOf(BukkitPlayer, npc)) {
                            continue;
                        }

                        // Get the player
                        dPlayer player = dPlayer.mirrorBukkitPlayer(BukkitPlayer);

                        //
                        // Check to make sure the NPC has an assignment. If no assignment, a script doesn't need to be parsed,
                        // but it does still need to trigger for cooldown and action purposes.
                        //
                        InteractScriptContainer script = npc.getInteractScriptQuietly(player, ProximityTrigger.class);

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
                                if (script.hasTriggerOptionFor(ProximityTrigger.class, player, null, "ENTRY RADIUS")) {
                                    entryRadius = Integer.valueOf(script.getTriggerOptionFor(ProximityTrigger.class, player, null, "ENTRY RADIUS"));
                                }
                            }
                            catch (NumberFormatException nfe) {
                                dB.echoDebug(script, "Entry Radius was not an integer.  Assuming " + entryRadius + " as the radius.");
                            }
                            try {
                                if (script.hasTriggerOptionFor(ProximityTrigger.class, player, null, "EXIT RADIUS")) {
                                    exitRadius = Integer.valueOf(script.getTriggerOptionFor(ProximityTrigger.class, player, null, "EXIT RADIUS"));
                                }
                            }
                            catch (NumberFormatException nfe) {
                                dB.echoDebug(script, "Exit Radius was not an integer.  Assuming " + exitRadius + " as the radius.");
                            }
                            try {
                                if (script.hasTriggerOptionFor(ProximityTrigger.class, player, null, "MOVE RADIUS")) {
                                    moveRadius = Integer.valueOf(script.getTriggerOptionFor(ProximityTrigger.class, player, null, "MOVE RADIUS"));
                                }
                            }
                            catch (NumberFormatException nfe) {
                                dB.echoDebug(script, "Move Radius was not an integer.  Assuming " + moveRadius + " as the radius.");
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
                        if (npcLocation.getWorld() != player.getWorld()) {
                            playerChangedWorlds = true;
                        }

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
                        boolean exitedProximity = hasExitedProximityOf(BukkitPlayer, npc);
                        double distance = 0;
                        if (!playerChangedWorlds) {
                            distance = npcLocation.distance(player.getLocation());
                        }

                        if (!exitedProximity
                                && (playerChangedWorlds || distance >= exitRadius)) {
                            if (!npc.getTriggerTrait().triggerCooldownOnly(trigger, player)) {
                                continue;
                            }
                            // Remember that NPC has exited proximity.
                            exitProximityOf(BukkitPlayer, npc);
                            // Exit Proximity Action
                            npc.action("exit proximity", player);
                            // Parse Interact Script
                            parse(npc, player, script, "EXIT");
                        }
                        else if (exitedProximity && distance <= entryRadius) {
                            // Cooldown
                            if (!npc.getTriggerTrait().triggerCooldownOnly(trigger, player)) {
                                continue;
                            }
                            // Remember that Player has entered proximity of the NPC
                            enterProximityOf(BukkitPlayer, npc);
                            // Enter Proximity Action
                            npc.action("enter proximity", player);
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
                            npc.action("move proximity", player);
                            // Parse Interact Script
                            parse(npc, player, script, "MOVE");
                        }
                    }
                }
            }
        }, 5, 5);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTask(taskID);
    }

    /**
     * Checks if the Player in Proximity is close enough to be calculated.
     *
     * @param player the Player
     * @param npc    the NPC
     * @return true if within maxProximityDistance in all directions
     */
    private boolean isCloseEnough(Player player, dNPC npc) {
        Location pLoc = player.getLocation();
        Location nLoc = npc.getLocation();
        if (Math.abs(pLoc.getX() - nLoc.getX()) > maxProximityDistance) {
            return false;
        }
        if (Math.abs(pLoc.getY() - nLoc.getY()) > maxProximityDistance) {
            return false;
        }
        if (Math.abs(pLoc.getZ() - nLoc.getZ()) > maxProximityDistance) {
            return false;
        }
        return true;
    }

    /**
     * Checks all proximity ranges in scripts and finds the largest number. No need to
     * calculate distances that exceed the largest number.
     *
     * @param event dScriptReloadEvent, fired upon server startup or '/denizen reload scripts'
     */
    @EventHandler // TODO: Does this have any point?
    public void checkMaxProximities(ScriptReloadEvent event) {

        for (String script : ScriptRegistry._getScriptNames()) {
            //
            // TODO: Check interact scripts for proximity triggers and ranges.
            // Find largest number, add 10, and set it as maxProximityRange.
            // For now, let's assume 25.
            //
        }
    }

    private static Map<UUID, Set<Integer>> proximityTracker = new ConcurrentHashMap<UUID, Set<Integer>>(8, 0.9f, 1);

    //
    // Ensures that a Player who has entered proximity of an NPC also fires Exit Proximity.
    //
    private boolean hasExitedProximityOf(Player player, dNPC npc) {
        // If Player hasn't entered proximity, it's not in the Map. Return true, must be exited.
        Set<Integer> existing = proximityTracker.get(player.getUniqueId());
        if (existing == null) {
            return true;
        }
        // If Player has no entry for this NPC, return true.
        if (!existing.contains(npc.getId())) {
            return true;
        }
        // Entry is present, NPC has not yet triggered exit proximity.
        return false;
    }

    /**
     * Called when a 'Enter Proximity' has been called to make sure an exit
     * proximity will be called.
     *
     * @param player the Player
     * @param npc    the NPC
     */
    private void enterProximityOf(Player player, dNPC npc) {
        Set<Integer> npcs = proximityTracker.get(player.getUniqueId());
        if (npcs == null) {
            npcs = new HashSet<Integer>();
            proximityTracker.put(player.getUniqueId(), npcs);
        }
        npcs.add(npc.getId());
    }

    /**
     * Called when an 'Exit Proximity' has been called. Once successfully exited,
     * a Player can enter proximity again.
     *
     * @param player the Player
     * @param npc    the NPC
     */
    private void exitProximityOf(Player player, dNPC npc) {
        Set<Integer> npcs = proximityTracker.get(player.getUniqueId());
        if (npcs == null) {
            npcs = new HashSet<Integer>();
            proximityTracker.put(player.getUniqueId(), npcs);
        }
        npcs.remove(npc.getId());
    }
}
