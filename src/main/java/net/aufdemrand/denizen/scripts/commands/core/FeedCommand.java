package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.minecraft.server.Packet18ArmAnimation;

/**
 * Switches a button or lever.
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
	
	Integer amount = 20;
	Player thePlayer;
	DenizenNPC theDenizen;
	
	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		for (String arg : scriptEntry.getArguments()) {
			
			if (scriptEntry.getPlayer() == null) {
				dB.echoError("Requires a Player!");
				return;
			} else {
				thePlayer = scriptEntry.getPlayer();
				theDenizen = scriptEntry.getDenizen();
			}
			
			if (arg.matches("(?:QTY|qty|Qty|AMT|Amt|amt|AMOUNT|Amount|amount)(:)(\\d+)")){
				amount = aH.getIntegerFrom(arg);
				dB.echoDebug("...amount set to '" + amount + "'.");
				continue;
			}
		}
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		thePlayer.setFoodLevel(thePlayer.getPlayer().getFoodLevel() + amount);
		net.citizensnpcs.util.Util.sendPacketNearby(theDenizen.getLocation(), 
				new Packet18ArmAnimation((theDenizen).getHandle(),6) , 64); 
		dB.echoDebug("...player fed.");
		return;
	}
}
