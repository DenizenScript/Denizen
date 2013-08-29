package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Duration;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.aH.ArgumentType;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import net.minecraft.server.v1_6_R2.Block;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_6_R2.CraftWorld;

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


    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException  {
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("location") &&
                    arg.matchesArgumentType(dLocation.class))
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            else if (!scriptEntry.hasObject("duration") &&
                    arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            else if (!scriptEntry.hasObject("state") &&
                    arg.matchesEnum(SwitchState.values()))
                scriptEntry.addObject("switchstate", new Element(arg.getValue().toUpperCase()));
            else
                dB.echoError("Unknown argument " + arg.raw_value);
        }

        if (!scriptEntry.hasObject("location"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_LOCATION, "location");
        if (!scriptEntry.hasObject("duration"))
            scriptEntry.addObject("duration", new Duration(0));
        if (!scriptEntry.hasObject("switchstate"))
            scriptEntry.addObject("switchstate", new Element("TOGGLE"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        final dLocation interactLocation = (dLocation)scriptEntry.getObject("location");
        int duration = ((Duration)scriptEntry.getObject("duration")).getSecondsAsInt();
        final SwitchState switchState = SwitchState.valueOf(scriptEntry.getElement("switchstate").asString());

        // Switch the Block
        switchBlock(interactLocation, switchState);

        // TODO: Rewrite the below code to not use freakin' NMS!
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
        
        if ((state.equals("ON") && !currentState) ||
            (state.equals("OFF") && currentState) ||
             state.equals("TOGGLE")) {
            
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
