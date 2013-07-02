package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.minecraft.server.v1_6_R1.Block;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R1.CraftWorld;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Switches a button or lever.
 * 
 * @author Jeremy Schroeder, Mason Adkins, David Cernat
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
                
            	String state = aH.getStringFrom(arg).toUpperCase(); 
            	
            	if (state.matches("ON|OPEN")) {
            		switchState = SwitchState.ON;
            	}
            	else if (state.matches("OFF|CLOSE")) {
            		switchState = SwitchState.OFF;
            	}
            	else if (state.matches("TOGGLE")) {
            		switchState = SwitchState.TOGGLE;
            	}
            	
            	dB.echoDebug("...set STATE: " + switchState.toString());
            	
            } else if (aH.matchesLocation(arg)) {
                interactLocation = aH.getLocationFrom(arg);
                if (interactLocation != null) dB.echoDebug("...switch LOCATION now: '%s'", arg);

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
                    new Runnable() {
                		public void run() {
                    // Check to see if the state of the block is what is expected. If switched during 
                    // the duration, the switchback is cancelled.
                    if (switchState == SwitchState.OFF && !((interactLocation.getBlock().getData() & 0x8) > 0))
                        switchBlock(interactLocation, SwitchState.ON);
                    else if (switchState == SwitchState.ON && ((interactLocation.getBlock().getData() & 0x8) > 0))
                        switchBlock(interactLocation, SwitchState.OFF);
                    else if (switchState == SwitchState.TOGGLE) switchBlock(interactLocation, SwitchState.TOGGLE);
                }
            }, duration * 20));
        }

    }
    
    // Break off this portion of the code from execute() so it can be used in both execute and the delayed runnable
    public void switchBlock(Location interactLocation, SwitchState switchState) {
        World world = interactLocation.getWorld();
        boolean currentState = (interactLocation.getBlock().getData() & 0x8) > 0;
        String state = switchState.toString();
        
        if ((state == "ON" && currentState == false) ||
        	(state == "OFF" && currentState == true) ||
        	 state == "TOGGLE") {
        	
        	try {

        		Block.byId[interactLocation.getBlock().getType().getId()]
        			.interact(((CraftWorld)world).getHandle(),
        					  interactLocation.getBlockX(),
        					  interactLocation.getBlockY(),
        					  interactLocation.getBlockZ(),
        					  null, 0, 0f, 0f, 0f);
        		
        		dB.echoDebug("Switched " + interactLocation.getBlock().getType().toString() + "! Current state now: " +
  					  ((interactLocation.getBlock().getData() & 0x8) > 0 ? "ON" : "OFF"));
        		
        	} catch (NullPointerException e) {
        		
        		dB.echoDebug("Cannot switch " + interactLocation.getBlock().getType().toString() + "!");
        	}
        }
    }
}