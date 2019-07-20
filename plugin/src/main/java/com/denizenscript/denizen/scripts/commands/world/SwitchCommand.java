package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.utilities.DenizenAPI;
import com.denizenscript.denizen.utilities.MaterialCompat;
import com.denizenscript.denizen.utilities.Utilities;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.nms.NMSVersion;
import com.denizenscript.denizen.nms.abstracts.ModernBlockData;
import com.denizenscript.denizen.nms.interfaces.BlockData;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SwitchCommand extends AbstractCommand {

    // <--[command]
    // @Name Switch
    // @Syntax switch [<location>|...] (state:[{toggle}/on/off]) (duration:<value>)
    // @Required 1
    // @Short Switches state of the block.
    // @Group world
    //
    // @Description
    // Changes the state of a block at the given location.
    // Can specify a duration before it returns to the previous state.
    // By default, will toggle the state (on to off, or off to on).
    // Works on any interactable blocks.
    //
    // @Tags
    // <LocationTag.switched>
    //
    // @Usage
    // At the player's location, switch the state of the block to on, no matter what state it was in before.
    // - switch <player.location> state:on
    //
    // @Usage
    // Opens a door that the player is looking at.
    // - switch <player.location.cursor_on> state:on
    //
    // @Usage
    // Toggle a block at the player's location.
    // - switch <player.location>
    //
    // -->

    private enum SwitchState {ON, OFF, TOGGLE}

    private Map<Location, Integer> taskMap = new ConcurrentHashMap<>(8, 0.9f, 1);

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry.getProcessedArgs()) {

            if (!scriptEntry.hasObject("locations") &&
                    arg.matchesArgumentList(LocationTag.class)) {
                scriptEntry.addObject("locations", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("duration") &&
                    arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("state") &&
                    arg.matchesEnum(SwitchState.values())) {
                scriptEntry.addObject("switchstate", new ElementTag(arg.getValue().toUpperCase()));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("locations")) {
            throw new InvalidArgumentsException("Must specify a location!");
        }

        scriptEntry.defaultObject("duration", new DurationTag(0));
        scriptEntry.defaultObject("switchstate", new ElementTag("TOGGLE"));
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) {
        final ListTag interactLocations = scriptEntry.getObjectTag("locations");
        long duration = ((DurationTag) scriptEntry.getObject("duration")).getTicks();
        final SwitchState switchState = SwitchState.valueOf(scriptEntry.getElement("switchstate").asString());

        final Player player = Utilities.entryHasPlayer(scriptEntry) ? Utilities.getEntryPlayer(scriptEntry).getPlayerEntity() : null;
        // Switch the Block
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), interactLocations.debug()
                    + ArgumentHelper.debugObj("duration", duration + "t")
                    + ArgumentHelper.debugObj("switchstate", switchState.name()));
        }

        for (final LocationTag interactLocation : interactLocations.filter(LocationTag.class, scriptEntry)) {
            switchBlock(scriptEntry, interactLocation, switchState, player);

            // If duration set, schedule a delayed task.
            if (duration > 0) {
                // If this block already had a delayed task, cancel it.
                if (taskMap.containsKey(interactLocation)) {
                    try {
                        Bukkit.getScheduler().cancelTask(taskMap.get(interactLocation));
                    }
                    catch (Exception e) {
                    }
                }
                Debug.log("Setting delayed task 'SWITCH' for " + interactLocation.identify());
                // Store new delayed task ID, for checking against, then schedule new delayed task.
                taskMap.put(interactLocation, Bukkit.getScheduler().scheduleSyncDelayedTask(DenizenAPI.getCurrentInstance(),
                        new Runnable() {
                            public void run() {
                                switchBlock(scriptEntry, interactLocation, SwitchState.TOGGLE, player);
                            }
                        }, duration));
            }
        }

    }

    public static boolean switchState(Block b) {
        if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
            ModernBlockData mbd = new ModernBlockData(b);
            Boolean switchState = mbd.getSwitchState();
            if (switchState != null) {
                return switchState;
            }
        }
        //return (b.getData() & 0x8) > 0;
        Material type = b.getType();
        if (type == MaterialCompat.IRON_DOOR
                || type == MaterialCompat.OAK_DOOR
                || type == Material.DARK_OAK_DOOR
                || type == Material.BIRCH_DOOR
                || type == Material.ACACIA_DOOR
                || type == Material.JUNGLE_DOOR
                || type == Material.SPRUCE_DOOR) {
            Location location = b.getLocation();
            int data = b.getData();
            if (data >= 8) {
                location = b.getLocation().clone().add(0, -1, 0);
            }
            return (location.getBlock().getData() & 0x4) > 0;
        }
        else if ((type == MaterialCompat.OAK_TRAPDOOR
                || type == Material.IRON_TRAPDOOR)
                || (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)
                && (type == Material.DARK_OAK_TRAPDOOR
                || type == Material.BIRCH_TRAPDOOR
                || type == Material.ACACIA_TRAPDOOR
                || type == Material.JUNGLE_TRAPDOOR
                || type == Material.SPRUCE_TRAPDOOR))) {
            return (b.getData() & 0x4) > 0;
        }
        else {
            return (b.getData() & 0x8) > 0;
        }
    }

    // Break off this portion of the code from execute() so it can be used in both execute and the delayed runnable
    public void switchBlock(ScriptEntry scriptEntry, Location interactLocation, SwitchState switchState, Player player) {
        boolean currentState = switchState(interactLocation.getBlock());

        if ((switchState.equals(SwitchState.ON) && !currentState) ||
                (switchState.equals(SwitchState.OFF) && currentState) ||
                switchState.equals(SwitchState.TOGGLE)) {

            if (NMSHandler.getVersion().isAtLeast(NMSVersion.v1_13_R2)) {
                ModernBlockData mbd = new ModernBlockData(interactLocation.getBlock());
                mbd.setSwitchState(interactLocation.getBlock(), !currentState);
            }
            else {
                try {
                    if (interactLocation.getBlock().getType() == MaterialCompat.IRON_DOOR) {
                        Location block;
                        if (interactLocation.clone().add(0, -1, 0).getBlock().getType() == MaterialCompat.IRON_DOOR) {
                            block = interactLocation.clone().add(0, -1, 0);
                        }
                        else {
                            block = interactLocation;
                        }
                        BlockData blockData = NMSHandler.getInstance().getBlockHelper().getBlockData(MaterialCompat.IRON_DOOR, (byte) (block.getBlock().getData() ^ 4));
                        blockData.setBlock(block.getBlock(), false);
                    }
                    else {
                        // Try for a linked player
                        if (player == null && Bukkit.getOnlinePlayers().size() > 0) {
                            // If there's none, link any player
                            if (Bukkit.getOnlinePlayers().size() > 0) {
                                player = (Player) Bukkit.getOnlinePlayers().toArray()[0];
                            }
                            else if (Depends.citizens != null) {
                                // If there are no players, link any Human NPC
                                for (NPC npc : CitizensAPI.getNPCRegistry()) {
                                    if (npc.isSpawned() && npc.getEntity() instanceof Player) {
                                        player = (Player) npc.getEntity();
                                        break;
                                    }
                                }
                                // TODO: backup if no human NPC available? (Fake EntityPlayer instance?)
                            }
                        }
                        NMSHandler.getInstance().getEntityHelper().forceInteraction(player, interactLocation);
                    }

                }
                catch (NullPointerException e) {
                    Debug.echoError("Cannot switch " + interactLocation.getBlock().getType().toString() + "!");
                    return;
                }
            }

            Debug.echoDebug(scriptEntry, "Switched " + interactLocation.getBlock().getType().toString() + "! Current state now: " +
                    (switchState(interactLocation.getBlock()) ? "ON" : "OFF"));
        }
    }
}
