package net.aufdemrand.denizen.commands.core;

import net.citizensnpcs.command.exception.CommandException;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CastCommand extends AbstractCommand{

	/* CAST [POTION_EFFECT] (DURATION:#) (POWER:#) (NPCID:#) (PLAYER:PlayerName) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [POTION_EFFECT] Uses bukkit enum for specifying the potion effect to use.
	 *   
	 * Example Usage:
	 * 
	 */
	
	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {
		/* Initialize variables */ 
		String PotionName = null;
		int duration = 60;
		int amplitude = 1;

		LivingEntity target = theEntry.getEntity();

		if (theEntry.arguments() == null)
			throw new CommandException("Usage: CAST [SpellName] (DURATION:#) (POWER:#) (NPC:#) (PLAYER:PlayerName)");

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// If argument is a modifier.
			if (thisArg.toUpperCase().contains("SPELL:")) {
				PotionName = aH.getStringModifier(thisArg);
				aH.echoDebug("...spell name is '" + PotionName + "'.", thisArg);
			}

			// DURATION argument
			else if (aH.matchesDuration(thisArg)) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...Duration is '" + duration + "'.", thisArg);
			}

			// POWER argument
			else if (thisArg.toUpperCase().contains("POWER:")) {
				amplitude = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...Power is " + amplitude, thisArg);
			}

			// NPCID argument
			else if (aH.matchesNPCID(thisArg)) {
				target = aH.getNPCIDModifier(thisArg).getEntity();
				if (target !=null)	aH.echoDebug("...now targeting '" + target.toString() + "'.", thisArg);
			}

			// Would this ever be used? A hard-coded player in a script?
			else if (thisArg.toUpperCase().contains("PLAYER:")) {
				target = theEntry.getDenizen().getEntity().getServer().getPlayer(aH.getStringModifier(thisArg));
				if (target !=null)	aH.echoDebug("...now targeting '" + target.toString() + "'.", thisArg);
			}

			else {
				PotionName = aH.getStringModifier(thisArg);
				aH.echoDebug("...will cast effect '" + PotionName + "'.", thisArg);
			}

		}	

		if (target == null) {
			aH.echoError("Could not find target!");
			return false;
		}

		/* Execute the command, if all required variables are filled. */
		if (PotionName !=null) {
			try {
				target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(PotionName), duration * 20, amplitude));
				return true;
			} catch (Exception e) {
				aH.echoError("Invalid potion effect!");
				return false;
			}
		}
		
		else aH.echoError("Usage: CAST [SpellName] (DURATION:#) (POWER:#) (NPC:#) (PLAYER:PlayerName)");	

		return false;
	}


}