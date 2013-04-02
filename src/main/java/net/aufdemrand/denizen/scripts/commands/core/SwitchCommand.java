package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable2;
import net.minecraft.server.v1_5_R2.Block;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_5_R2.CraftWorld;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Switches a button or lever.
 * 
 * @author Jeremy Schroeder, Mason Adkins
 */

public class SwitchCommand extends AbstractCommand {

    @Override
    public void onEnable() {
        // Nothing to do here
    }

    /* SWITCH [LOCATION:x,y,z,world] (STATE:ON|OFF|TOGGLE) (DURATION:#) */

    /* 
     * Arguments: [] - Required, () - Optional 
     * [LOCATION:x,y,z,world] specifies location of a switch, lever, or pressure plate.
     * (STATE:ON|OFF|TOGGLE) can be used on locations with switches. Default: TOGGLE
     * (DURATION:#) Reverts to the previous head position after # amount of seconds.
     * 
     * Example Usage:
     * SWITCH LOCATION:<BOOKMARK:Lever_1> STATE:ON
     * SWITCH LOCATION:99,64,125,world 'DURATION:15' 
     * SWITCH LOCATION:<ANCHOR:button_location>
     * 
     */

    private enum SwitchState { ON, OFF, TOGGLE } 

    private Map<Location, Integer> taskMap = new ConcurrentHashMap<Location, Integer>(8, 0.9f, 1);

    SwitchState switchState;
    Location interactLocation;
    int duration = -1;

    @Override
    public void parseArgs(ScriptEntry theEntry) throws InvalidArgumentsException  {

        /* Initialize variables */ 
        interactLocation = null;
        duration = -1;
        switchState = SwitchState.TOGGLE;

        for (String arg : theEntry.getArguments()) {
            if (aH.matchesDuration(arg)) {
                duration = Integer.valueOf(arg.split(":")[1]);
                dB.echoDebug(Messages.DEBUG_SET_DURATION, arg);

            } else if (aH.matchesValueArg("STATE", arg, ArgumentType.Custom)) {
                if (aH.getStringFrom(arg).equalsIgnoreCase("ON") || aH.getStringFrom(arg).equalsIgnoreCase("OPEN")) {
                    switchState = SwitchState.ON;
                    dB.echoDebug("...set STATE: 'ON'.");
                } else if (aH.getStringFrom(arg).equalsIgnoreCase("OFF") || aH.getStringFrom(arg).equalsIgnoreCase("CLOSE")) {
                    switchState = SwitchState.OFF;
                    dB.echoDebug("...set STATE: 'OFF'.");
                } else if (aH.getStringFrom(arg).equalsIgnoreCase("TOGGLE")) {
                    switchState = SwitchState.TOGGLE;
                    dB.echoDebug("...set STATE: 'TOGGLE'.");
                } else dB.echoError("Unknown STATE! Valid: ON, OFF, TOGGLE");

            } else if (aH.matchesLocation(arg)) {
                interactLocation = aH.getLocationFrom(arg);
                if (interactLocation != null) dB.echoError("...switch LOCATION now: '%s'", arg);

            } else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }	

        if (interactLocation == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_LOCATION);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Switch the Block
        switchBlock(interactLocation, switchState);

        // If duration set, schedule a delayed task.
        if (duration > 0) {
            // If this block already had a delayed task, cancel it.
            if (taskMap.containsKey(interactLocation)) 
                try { denizen.getServer().getScheduler().cancelTask(taskMap.get(interactLocation)); } catch (Exception e) { }
            dB.echoDebug(Messages.DEBUG_RUNNING_DELAYED_TASK, "SWITCH");
            // Store new delayed task ID, for checking against, then schedule new delayed task.
            taskMap.put(interactLocation, denizen.getServer().getScheduler().scheduleSyncDelayedTask(denizen, 
                    new Runnable2<Location, SwitchState>(interactLocation, switchState) {
                @Override public void run(Location iLocation, SwitchState sState) { 
                    // Check to see if the state of the block is what is expected. If switched during 
                    // the duration, the switchback is cancelled.
                    if (sState == SwitchState.OFF && !((iLocation.getBlock().getData() & 0x8) > 0))
                        switchBlock(iLocation, SwitchState.ON);
                    else if (sState == SwitchState.ON && ((iLocation.getBlock().getData() & 0x8) > 0))
                        switchBlock(iLocation, SwitchState.OFF);
                    else if (sState == SwitchState.TOGGLE) switchBlock(iLocation, SwitchState.TOGGLE);
                }
            }, duration * 20));
        }

    }
    
    // Break off this portion of the code from execute() so it can be used in both execute and the delayed runnable
    public void switchBlock(Location interactLocation, SwitchState switchState) {
        World world = interactLocation.getWorld();
        boolean currentState = (interactLocation.getBlock().getData() & 0x8) > 0;

        // Might be a way to generalize this portion and negate the need for a switch all-together.
        switch (interactLocation.getBlock().getType()) {

        case LEVER:
            switch (switchState) {
            case TOGGLE:
                Block.LEVER.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case ON:
                if (currentState != true) 
                    Block.LEVER.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case OFF:
                if (currentState != false) 
                    Block.LEVER.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            }
            dB.echoDebug("SWITCHED! Current state now: " + ((interactLocation.getBlock().getData() & 0x8) > 0 ? "ON" : "OFF"));
            break;

        case STONE_BUTTON:
            switch (switchState) {
            case TOGGLE:
                Block.STONE_BUTTON.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case ON:
                if (currentState != true) 
                    Block.STONE_BUTTON.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case OFF:
                if (currentState != false) 
                    Block.STONE_BUTTON.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            }
            dB.echoDebug("SWITCHED! Current state now: " + ((interactLocation.getBlock().getData() & 0x8) > 0 ? "ON" : "OFF"));
            break;

        case STONE_PLATE:
            switch (switchState) {
            case TOGGLE:
                Block.STONE_PLATE.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case ON:
                if (currentState != true) 
                    Block.STONE_PLATE.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case OFF:
                if (currentState != false) 
                    Block.STONE_PLATE.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            }
            dB.echoDebug("SWITCHED! Current state now: " + ((interactLocation.getBlock().getData() & 0x8) > 0 ? "ON" : "OFF"));
            break;

        case WOOD_PLATE:
            switch (switchState) {
            case TOGGLE:
                Block.WOOD_PLATE.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case ON:
                if (currentState != true) 
                    Block.WOOD_PLATE.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case OFF:
                if (currentState != false) 
                    Block.WOOD_PLATE.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            }
            dB.echoDebug("SWITCHED! Current state now: " + ((interactLocation.getBlock().getData() & 0x8) > 0 ? "ON" : "OFF"));
            break;

        case WOODEN_DOOR:
            switch (switchState) {
            case TOGGLE:
                Block.WOODEN_DOOR.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case ON:
                if (currentState != true) 
                    Block.WOODEN_DOOR.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case OFF:
                if (currentState != false) 
                    Block.WOODEN_DOOR.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            }
            dB.echoDebug("SWITCHED! Current state now: " + ((interactLocation.getBlock().getData() & 0x8) > 0 ? "ON" : "OFF"));
            break;

        case IRON_DOOR_BLOCK:
            switch (switchState) {
            case TOGGLE:
                Block.IRON_DOOR_BLOCK.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case ON:
                if (currentState != true) 
                    Block.WOODEN_DOOR.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case OFF:
                if (currentState != false) 
                    Block.WOODEN_DOOR.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            }
            dB.echoDebug("SWITCHED! Current state now: " + ((interactLocation.getBlock().getData() & 0x8) > 0 ? "ON" : "OFF"));
            break;

        case TRAP_DOOR:
            switch (switchState) {
            case TOGGLE:
                Block.TRAP_DOOR.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case ON:
                if (currentState != true) 
                    Block.TRAP_DOOR.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            case OFF:
                if (currentState != false) 
                    Block.TRAP_DOOR.interact(((CraftWorld)world).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                break;
            }
            dB.echoDebug("SWITCHED! Current state now: " + ((interactLocation.getBlock().getData() & 0x8) > 0 ? "ON" : "OFF"));
            break;

        // If block isn't any of the above...
        default:
            dB.echoError("UNSWITCHABLE! Not a valid type of block!");
            break;
        }
    }
}