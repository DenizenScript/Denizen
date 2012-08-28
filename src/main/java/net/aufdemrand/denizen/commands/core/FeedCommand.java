package net.aufdemrand.denizen.commands.core;

import org.bukkit.craftbukkit.entity.CraftEntity;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;
import net.minecraft.server.Packet18ArmAnimation;

public class FeedCommand extends AbstractCommand {

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


	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		if (theEntry.getPlayer() == null) {
			aH.echoError("Requires a Player!");
			return false;
		}

		Integer amount = 20;

		if (theEntry.arguments() != null)
			for (String thisArg : theEntry.arguments()) {

				if (thisArg.matches("(?:QTY|qty|Qty|AMT|Amt|amt|AMOUNT|Amount|amount)(:)(\\d+)")){
					amount = aH.getIntegerModifier(thisArg);
					aH.echoDebug("...amount set to '" + amount + "'.");
				}
			}

		theEntry.getPlayer().setSaturation(theEntry.getPlayer().getSaturation() + amount);
		theEntry.getPlayer().setFoodLevel(theEntry.getPlayer().getFoodLevel() + amount);
		net.citizensnpcs.util.Util.sendPacketNearby(theEntry.getDenizen().getLocation(), 
				new Packet18ArmAnimation(((CraftEntity)theEntry.getDenizen().getEntity()).getHandle(),6) , 64); 
		
		return true;
	}

}
