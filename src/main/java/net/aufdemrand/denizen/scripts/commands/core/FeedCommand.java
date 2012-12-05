package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;
import net.minecraft.server.Packet18ArmAnimation;

/**
 * Feeds a (Player) entity.
 * 
 * @author Jeremy Schroeder, Mason Adkins
 */

public class FeedCommand extends AbstractCommand {

    @Override
    public void onEnable() {
        // nothing to do here
    }

    /* FEED (AMT:#) */

    /* 
     * Arguments: [] - Required, () - Optional 
     * (AMOUNT:#) 1-20, usually.
     *   
     * Example Usage:
     * FEED
     * FEED 5
     *
     */

    private int amount;
    private LivingEntity target;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        amount = 20;

    }
    // Default target is Player

    // Writing HUNGER trait, brb.
    //	    
    //	    for (String arg : scriptEntry.getArguments()) {
    //			
    //			if (scriptEntry.getPlayer() == null) {
    //				dB.echoError("Requires a Player!");
    //				return;
    //			} else {
    //				thePlayer = scriptEntry.getPlayer();
    //				theDenizen = scriptEntry.getDenizen();
    //			}
    //			
    //			else if (aH.matchesQuantity(arg) 
    //			        || aH.matchesValueArg("amt", arg, ArgumentType.Integer)) {
    //				amount = aH.getIntegerFrom(arg);
    //				dB.echoDebug(Messages.DEBUG_SET_QUANTITY, String.valueOf(amount));
    //				continue;
    //			}
    //		}
    //	}
    //
    //	@Override
    //	public void execute(String commandName) throws CommandExecutionException {
    //		thePlayer.setFoodLevel(thePlayer.getPlayer().getFoodLevel() + amount);
    //		net.citizensnpcs.util.Util.sendPacketNearby(theDenizen.getLocation(), 
    //				new Packet18ArmAnimation((theDenizen).getHandle(),6) , 64); 
    //		dB.echoDebug("...player fed.");
    //		return;
    //	}
    //}


    @Override
    public void execute(String commandName) throws CommandExecutionException {
        // TODO Auto-generated method stub

    }
}
