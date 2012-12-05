package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;
import net.minecraft.server.Packet18ArmAnimation;

public class HealCommand extends AbstractCommand {

	/* HEAL/HARM */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * (DENIZEN)
	 * (AMOUNT:#)
	 *   
	 * Example Usage:
	 * HEAL
	 * HARM DENIZEN 1
	 *
	 */
	
	@Override
    public void onEnable() {
        // Nothing to do here
    }
	
	boolean hurts = false;
	LivingEntity target = null;
	Integer amount = null;
	
	@Override
    public void parseArgs(ScriptEntry theEntry)  {
		if (theEntry.getPlayer() == null) {
			target = theEntry.getDenizen().getEntity();			
		} else {
			target = theEntry.getPlayer();
		}
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		if (target != null) {
			if (hurts) {
				if (amount == null) amount = 1;
				target.setHealth(target.getHealth() - amount);		
				net.citizensnpcs.util.Util.sendPacketNearby(target.getLocation(), 
						new Packet18ArmAnimation(((CraftEntity)target).getHandle(),2) , 64); // hurt effect
				return;
			} else {
				if (amount == null) amount = target.getMaxHealth() - target.getHealth();
				target.setHealth(target.getHealth() + amount);			
				net.citizensnpcs.util.Util.sendPacketNearby(target.getLocation(),
						new Packet18ArmAnimation( ((CraftEntity)target).getHandle(),6) , 64); // white sparks
				return;
			}
		} 
	}
}
	
	/*	
	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {





		

		hurts = theEntry.getCommand().equalsIgnoreCase("HARM");

		if (theEntry.arguments() != null)
			for (String thisArg : theEntry.arguments()) {
				
				// Fill replaceables
				if (thisArg.contains("<")) thisArg = aH.fillReplaceables(theEntry.getPlayer(), theEntry.getDenizen(), thisArg, false);

				if (thisArg.toUpperCase().contains("DENIZEN")){
					if (theEntry.getDenizen() != null) {
					target = theEntry.getDenizen().getEntity();
					aH.echoDebug("...targeting '" + theEntry.getDenizen().getName() + "'.");
					} else {
						aH.echoError("Seems this was sent from a TASK-type script. Must use NPCID:# to specify a Denizen NPC.");
					}
				} 
				
				// If argument is a NPCID: modifier
				else if (aH.matchesNPCID(thisArg)) {
					target = aH.getNPCIDModifier(thisArg).getEntity();
					if (target != null)
						aH.echoDebug("...now targeting '%s'.", thisArg);
				}

				else if (thisArg.matches("(?:QTY|qty|Qty|AMT|Amt|amt|AMOUNT|Amount|amount)(:)(\\d+)")){
					amount = aH.getIntegerModifier(thisArg);
					aH.echoDebug("...amount set to '" + amount + "'.");
				}
			}

		
		// Execute the command



		return false;
	}

}
*/