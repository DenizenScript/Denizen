package com.denizenscript.denizen.scripts.triggers.core;

import com.denizenscript.denizen.scripts.containers.core.InteractScriptContainer;
import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.debugging.dB;
import com.denizenscript.denizen.events.bukkit.ScriptReloadEvent;
import com.denizenscript.denizen.npc.dNPCRegistry;
import com.denizenscript.denizen.npc.traits.TriggerTrait;
import com.denizenscript.denizen.objects.dNPC;
import com.denizenscript.denizen.objects.dPlayer;
import com.denizenscript.denizen.scripts.triggers.AbstractTrigger;
import com.denizenscript.denizencore.scripts.ScriptRegistry;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ProximityTrigger extends AbstractTrigger implements Listener {

    // <--[language]
    // @name Proximity Triggers
    // @group NPC Interact Scripts
    // @description
    // Proximity Triggers are triggered when when a player moves in the area around the NPC.
    //
    // Proximity triggers must have a sub-key identifying what type of proximity trigger to use.
    // The three types are "entry", "exit", and "move".
    //
    // Entry and exit do exactly as the names imply: Entry fires when the NPC walks into range of the NPC, and exit fires when the NPC walks out of range.
    //
    // Move is a bit more subtle: it fires very rapidly so long as a player remains within range of the NPC.
    // This is useful for eg script logic that needs to be constantly updating whenever a player is nearby (eg a combat NPC script needs to constantly update its aim).
    //
    // The radius that the proximity trigger detects at is set by <@link command trigger>.
    //
    // -->

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

                Collection<? extends Player> allPlayers = Bukkit.getOnlinePlayers();
                //
                // Iterate over all of the NPCs
                //
                Iterator<dNPC> it = dNPCRegistry.getSpawnedNPCs().iterator();
                for (NPC citizensNPC : CitizensAPI.getNPCRegistry()) {
                    if (citizensNPC == null || !citizensNPC.isSpawned()) {
                        continue;
                    }
                    //
                    // If the NPC doesn't have triggers, or the Proximity Trigger is not enabled,
                    // then just return.
                    //
                    if (!citizensNPC.hasTrait(TriggerTrait.class) || !citizensNPC.getTrait(TriggerTrait.class).isEnabled(name)) {
                        continue;
                    }
                    dNPC npc = new dNPC(citizensNPC);
                    TriggerTrait triggerTrait = npc.getTriggerTrait();

                    // Loop through all players
                    for (Player bukkitPlayer : allPlayers) {

                        //
                        // If this NPC is not spawned or in a different world, no need to check,
                        // unless the Player hasn't yet triggered an Exit Proximity after Entering
                        //
                        if (!npc.getWorld().equals(bukkitPlayer.getWorld())
                                && hasExitedProximityOf(bukkitPlayer, npc)) {
                            continue;
                        }

                        //
                        // If this NPC is more than the maxProximityDistance, skip it, unless
                        // the Player hasn't yet triggered an 'Exit Proximity' after entering.
                        //
                        if (!isCloseEnough(bukkitPlayer, npc)
                                && hasExitedProximityOf(bukkitPlayer, npc)) {
                            continue;
                        }

                        // Get the player
                        dPlayer player = dPlayer.mirrorBukkitPlayer(bukkitPlayer);

                        //
                        // Check to make sure the NPC has an assignment. If no assignment, a script doesn't need to be parsed,
                        // but it does still need to trigger for cooldown and action purposes.
                        //
                        InteractScriptContainer script = npc.getInteractScriptQuietly(player, ProximityTrigger.class);

                        //
                        // Set default ranges with information from the TriggerTrait. This allows per-npc overrides and will
                        // automatically check the config for defaults.
                        //
                        double entryRadius = triggerTrait.getRadius(name);
                        double exitRadius = triggerTrait.getRadius(name);
                        double moveRadius = triggerTrait.getRadius(name);


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
                        boolean exitedProximity = hasExitedProximityOf(bukkitPlayer, npc);
                        double distance = 0;
                        if (!playerChangedWorlds) {
                            distance = npcLocation.distance(player.getLocation());
                        }

                        if (!exitedProximity
                                && (playerChangedWorlds || distance >= exitRadius)) {
                            if (!triggerTrait.triggerCooldownOnly(trigger, player)) {
                                continue;
                            }
                            // Remember that NPC has exited proximity.
                            exitProximityOf(bukkitPlayer, npc);
                            // Exit Proximity Action
                            npc.action("exit proximity", player);
                            // Parse Interact Script
                            parse(npc, player, script, "EXIT");
                        }
                        else if (exitedProximity && distance <= entryRadius) {
                            // Cooldown
                            if (!triggerTrait.triggerCooldownOnly(trigger, player)) {
                                continue;
                            }
                            // Remember that Player has entered proximity of the NPC
                            enterProximityOf(bukkitPlayer, npc);
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

    private static Map<UUID, Set<Integer>> proximityTracker = new ConcurrentHashMap<>(8, 0.9f, 1);

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
            npcs = new HashSet<>();
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
            npcs = new HashSet<>();
            proximityTracker.put(player.getUniqueId(), npcs);
        }
        npcs.remove(npc.getId());
    }
}
