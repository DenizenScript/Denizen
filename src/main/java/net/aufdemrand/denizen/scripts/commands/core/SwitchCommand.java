package net.aufdemrand.denizen.scripts.commands.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.material.Lever;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;
import net.aufdemrand.denizen.utilities.runnables.Runnable1;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;

/**
 * Switches a button or lever.
 * 
 * @author Jeremy Schroeder, Mason Adkins
 * @version 1.0 Last updated 12/3/2012 
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

    private Map<String, Integer> taskMap = new ConcurrentHashMap<String, Integer>();

    SwitchState switchState;
    Location interactLocation;
    int duration = -1;

    @Override
    public void parseArgs(ScriptEntry theEntry)  {

        /* Initialize variables */ 
        interactLocation = null;
        duration = -1;
        switchState = SwitchState.TOGGLE;

        for (String thisArg : theEntry.getArguments()) {
            if (aH.matchesDuration(thisArg)) {
                duration = Integer.valueOf(thisArg.split(":")[1]);
                dB.echoDebug(Messages.DEBUG_SET_DURATION, thisArg);
                continue;

            } else if (aH.matchesValueArg("STATE", thisArg, ArgumentType.Custom)) {
                if (aH.getStringFrom(thisArg).equalsIgnoreCase("ON")) {
                    switchState = SwitchState.ON;
                    dB.echoDebug("...set STATE: 'ON'.");
                } else if (aH.getStringFrom(thisArg).equalsIgnoreCase("OFF")) {
                    switchState = SwitchState.OFF;
                    dB.echoDebug("...set STATE: 'OFF'.");
                } else if (aH.getStringFrom(thisArg).equalsIgnoreCase("TOGGLE")) {
                    switchState = SwitchState.TOGGLE;
                    dB.echoDebug("...set STATE: 'TOGGLE'.");
                } else dB.echoError("Unknown STATE! Valid: ON, OFF, TOGGLE");
                continue;

 
   // LEFT OFF HERE
                
            } else if (aH.matchesBookmark(thisArg)) {
                interactLocation = aH.getBlockBookmarkModifier(thisArg, theEntry.getDenizen());
                if (interactLocation != null)	aH.echoDebug("...switch location now at bookmark '%s'", thisArg);
                else{
                    aH.echoDebug("... could not find block bookmark: '%s'", thisArg);
                    interactLocation = aH.getBookmarkModifier(thisArg, theEntry.getDenizen());
                    if (interactLocation != null) aH.echoDebug("...Found location bookmark matching '%s' using that.", thisArg);
                    // else	aH.echoDebug("... could not find any bookmark: '%s'", thisArg);
                }

            }		

            else {
                aH.echoError("...unable to match '%s'.", thisArg);
            }

        }	

        
            }



    @Override
    public void execute(String commandName) throws CommandExecutionException {

        /* Execute the command. */

        if (interactLocation == null) {
            aH.echoError("No interact location specified! Must use BOOKMARK:block to specify a location.");
            return false;
        }

        if (interactLocation.getBlock().getType() == Material.LEVER) {
            World theWorld = interactLocation.getWorld();
            boolean leverCurrentState = (interactLocation.getBlock().getData() & 0x8) > 0;
            if (leverCurrentState != switchState){
                net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                aH.echoDebug("...changing lever state");
            }
        }

        else if (interactLocation.getBlock().getType() == Material.STONE_BUTTON) {
            World theWorld = interactLocation.getWorld();
            boolean buttonCurrentState = (interactLocation.getBlock().getData() & 0x8) > 0;
            if (buttonCurrentState != switchState){
                net.minecraft.server.Block.STONE_BUTTON.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                aH.echoDebug("...changing button state");
            }
        }

        else if (interactLocation.getBlock().getType() == Material.STONE_PLATE) {
            World theWorld = interactLocation.getWorld();
            net.minecraft.server.Block.STONE_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
        }

        else if (interactLocation.getBlock().getType() == Material.WOOD_PLATE) {
            World theWorld = interactLocation.getWorld();
            net.minecraft.server.Block.WOOD_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
        }

        else {
            aH.echoError("Unusable block at this location! Found " + interactLocation.getBlock().getType().name() + ".");
            return false;
        }

        /* Make delayed task to reset step if duration is set */
        if (duration != null) {


            if (taskMap.containsKey(theEntry.getDenizen().getName())) {
                try {
                    plugin.getServer().getScheduler().cancelTask(taskMap.get(theEntry.getDenizen().getName()));
                } catch (Exception e) { }
            }
            aH.echoDebug("Setting delayed task: RESET LOOK");

            taskMap.put(theEntry.getDenizen().getName(), plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, 
                    new OneItemRunnable<Location>(interactLocation) {

                @Override
                public void run(Location interactLocation) { 
                    aH.echoDebug(ChatColor.YELLOW + "//DELAYED//" + ChatColor.WHITE + " Running delayed task: RESET LOOK.");
                    if (interactLocation != null) {
                        if (interactLocation.getBlock().getType() == Material.LEVER) {
                            World theWorld = interactLocation.getWorld();
                            net.minecraft.server.Block.LEVER.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                            return;
                        }

                        else if (interactLocation.getBlock().getType() == Material.STONE_BUTTON) {
                            World theWorld = interactLocation.getWorld();
                            net.minecraft.server.Block.STONE_BUTTON.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                            return;
                        }

                        else if (interactLocation.getBlock().getType() == Material.STONE_PLATE) {
                            World theWorld = interactLocation.getWorld();
                            net.minecraft.server.Block.STONE_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                            return;
                        }

                        else if (interactLocation.getBlock().getType() == Material.WOOD_PLATE) {
                            World theWorld = interactLocation.getWorld();
                            net.minecraft.server.Block.WOOD_PLATE.interact(((CraftWorld)theWorld).getHandle(), interactLocation.getBlockX(), interactLocation.getBlockY(), interactLocation.getBlockZ(), null, 0, 0f, 0f, 0f);
                            return;
                        }

                        else {
                            if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "...unusable block at this location! Found " + interactLocation.getBlock().getType().name() + ".");            
                        }
                    }
                }
            }, duration * 20));
        }

        return true;

    }


}