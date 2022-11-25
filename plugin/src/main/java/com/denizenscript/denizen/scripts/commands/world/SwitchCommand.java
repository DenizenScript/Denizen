package com.denizenscript.denizen.scripts.commands.world;

import com.denizenscript.denizen.Denizen;
import com.denizenscript.denizen.nms.NMSHandler;
import com.denizenscript.denizen.objects.MaterialTag;
import com.denizenscript.denizen.objects.properties.material.MaterialSwitchable;
import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.objects.LocationTag;
import com.denizenscript.denizencore.exceptions.InvalidArgumentsException;
import com.denizenscript.denizencore.objects.*;
import com.denizenscript.denizencore.objects.core.DurationTag;
import com.denizenscript.denizencore.objects.core.ElementTag;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Bell;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.TrapDoor;

import java.util.HashMap;
import java.util.Map;

public class SwitchCommand extends AbstractCommand {

    public SwitchCommand() {
        setName("switch");
        setSyntax("switch [<location>|...] (state:{toggle}/on/off) (duration:<value>) (no_physics)");
        setRequiredArguments(1, 4);
        isProcedural = false;
    }

    // <--[command]
    // @Name Switch
    // @Syntax switch [<location>|...] (state:{toggle}/on/off) (duration:<value>) (no_physics)
    // @Required 1
    // @Maximum 4
    // @Short Switches state of the block.
    // @Synonyms Toggle,Lever,Activate,Power,Redstone
    // @Group world
    //
    // @Description
    // Changes the state of a block at the given location, or list of blocks at the given locations.
    //
    // Optionally specify "state:on" to turn a block on (or open it, or whatever as applicable) or "state:off" to turn it off (or close it, etc).
    // By default, will toggle the state (on to off, or off to on).
    //
    // Optionally specify the "duration" argument to set a length of time after which the block will return to the original state.
    //
    // Works on any interactable blocks, including:
    // - the standard toggling levers, doors, gates...
    // - Single-use interactables like buttons, note blocks, dispensers, droppers, ...
    // - Redstone interactables like repeaters, ...
    // - Special interactables like tripwires, ...
    // - Bells as a special case will ring when you use 'switch' on them, ...
    // - Almost any other block with an interaction handler.
    //
    // This will generally (but not always) function equivalently to a user right-clicking the block
    // (so it will open and close doors, flip levers on and off, press and depress buttons, ...).
    //
    // Optionally specify 'no_physics' to not apply a physics update after switching.
    //
    // @Tags
    // <LocationTag.switched>
    // <MaterialTag.switched>
    //
    // @Usage
    // At the player's location, switch the state of the block to on, no matter what state it was in before.
    // - switch <player.location> state:on
    //
    // @Usage
    // Opens a door that the player is looking at.
    // - switch <player.cursor_on> state:on
    //
    // @Usage
    // Toggle a block at the player's foot location.
    // - switch <player.location>
    //
    // -->

    private enum SwitchState {ON, OFF, TOGGLE}

    private Map<Location, Integer> taskMap = new HashMap<>(32);

    @Override
    public void addCustomTabCompletions(TabCompletionsBuilder tab) {
        tab.addNotesOfType(LocationTag.class);
    }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        for (Argument arg : scriptEntry) {
            if (!scriptEntry.hasObject("no_physics") &&
                    arg.matches("no_physics")) {
                scriptEntry.addObject("no_physics", new ElementTag(true));
            }
            else if (!scriptEntry.hasObject("locations") &&
                    arg.matchesArgumentList(LocationTag.class)) {
                scriptEntry.addObject("locations", arg.asType(ListTag.class));
            }
            else if (!scriptEntry.hasObject("duration") &&
                    arg.matchesArgumentType(DurationTag.class)) {
                scriptEntry.addObject("duration", arg.asType(DurationTag.class));
            }
            else if (!scriptEntry.hasObject("state") &&
                    arg.matchesEnum(SwitchState.class)) {
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
        DurationTag duration = scriptEntry.getObjectTag("duration");
        final SwitchState switchState = SwitchState.valueOf(scriptEntry.getElement("switchstate").asString());
        ElementTag noPhysics = scriptEntry.getElement("no_physics");
        // Switch the Block
        if (scriptEntry.dbCallShouldDebug()) {
            Debug.report(scriptEntry, getName(), interactLocations, duration, noPhysics, db("switchstate", switchState.name()));
        }
        final boolean physics = noPhysics == null || !noPhysics.asBoolean();
        for (final LocationTag interactLocation : interactLocations.filter(LocationTag.class, scriptEntry)) {
            switchBlock(scriptEntry, interactLocation, switchState, physics);
            // If duration set, schedule a delayed task.
            if (duration.getTicks() > 0) {
                // If this block already had a delayed task, cancel it.
                if (taskMap.containsKey(interactLocation)) {
                    try {
                        Bukkit.getScheduler().cancelTask(taskMap.get(interactLocation));
                    }
                    catch (Exception e) {
                    }
                }
                Debug.echoDebug(scriptEntry, "Setting delayed task 'SWITCH' for " + interactLocation.identify());
                // Store new delayed task ID, for checking against, then schedule new delayed task.
                taskMap.put(interactLocation, Bukkit.getScheduler().scheduleSyncDelayedTask(Denizen.getInstance(), () -> switchBlock(scriptEntry, interactLocation, SwitchState.TOGGLE, physics), duration.getTicks()));
            }
        }
    }

    public static boolean switchState(Block b) {
        MaterialSwitchable switchable = MaterialSwitchable.getFrom(new MaterialTag(b.getBlockData()));
        if (switchable == null) {
            return false;
        }
        return switchable.getState();
    }

    // Break off this portion of the code from execute() so it can be used in both execute and the delayed runnable
    public void switchBlock(ScriptEntry scriptEntry, Location interactLocation, SwitchState switchState, boolean physics) {
        Block block = interactLocation.getBlock();
        BlockData data1 = block.getBlockData();
        MaterialTag materialTag = new MaterialTag(data1);
        MaterialSwitchable switchable = MaterialSwitchable.getFrom(materialTag);
        if (switchable == null) {
            Debug.echoError("Cannot switch block of type '" + materialTag.getMaterial().name() + "'");
            return;
        }
        if (materialTag.getMaterial() == Material.BELL) {
            NMSHandler.blockHelper.ringBell((Bell) block.getState());
            return;
        }
        boolean currentState = switchable.getState();
        if ((switchState.equals(SwitchState.ON) && !currentState) || (switchState.equals(SwitchState.OFF) && currentState) || switchState.equals(SwitchState.TOGGLE)) {
            switchable.setState(!currentState);
            if (physics) {
                block.setBlockData(switchable.material.getModernData());
            }
            else {
                ModifyBlockCommand.setBlock(block.getLocation(), materialTag, false, null);
            }
            if (data1 instanceof Bisected && !(data1 instanceof TrapDoor)) { // TrapDoor implements Bisected, but is not actually bisected???
                Location other = interactLocation.clone();
                if (((Bisected) data1).getHalf() == Bisected.Half.TOP) {
                    other = other.add(0, -1, 0);
                }
                else {
                    other = other.add(0, 1, 0);
                }
                BlockData data2 = other.getBlock().getBlockData();
                if (data2.getMaterial() == data1.getMaterial()) {
                    MaterialSwitchable switchable2 = MaterialSwitchable.getFrom(new MaterialTag(data2));
                    switchable2.setState(!currentState);
                    other.getBlock().setBlockData(switchable2.material.getModernData());
                    if (physics) {
                        AdjustBlockCommand.applyPhysicsAt(other);
                    }
                }
            }
            if (physics) {
                AdjustBlockCommand.applyPhysicsAt(interactLocation);
            }
            if (scriptEntry.dbCallShouldDebug()) {
                Debug.echoDebug(scriptEntry, "Switched " + block.getType() + "! Current state now: " + (switchState(block) ? "ON" : "OFF"));
            }
        }
    }
}
