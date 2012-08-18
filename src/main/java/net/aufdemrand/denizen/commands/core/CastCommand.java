package net.aufdemrand.denizen.commands.core;

import java.util.logging.Level;

import net.citizensnpcs.command.exception.CommandException;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CastCommand extends AbstractCommand{


	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {
		/* Initialize variables */ 
		String PotionName = null;
		int duration = 60;
		int amplitude = 1;

		LivingEntity target = theEntry.getEntity();

		if (theEntry.arguments() == null)
			throw new CommandException("...not enough arguments! Usage: CAST [SPELL:SpellName] (DURATION:#) (POWER:#) (NPC:#) (PLAYER:PlayerName)");

		/* Match arguments to expected variables */
		for (String thisArg : theEntry.arguments()) {

			// If argument is a modifier.
			if (thisArg.toUpperCase().contains("SPELL:")) {
				PotionName = aH.getStringModifier(thisArg);
				aH.echoDebug("...Spell name is " + PotionName, thisArg);
			}

			else if (thisArg.toUpperCase().contains("DURATION:")) {
				duration = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...Duation name is " + duration, thisArg);
			}

			else if (thisArg.toUpperCase().contains("POWER:")) {
				amplitude = aH.getIntegerModifier(thisArg);
				aH.echoDebug("...Power is " + amplitude, thisArg);
			}

			else if (thisArg.toUpperCase().contains("NPC:")) {
				target = net.citizensnpcs.api.CitizensAPI.getNPCRegistry().getById(aH.getIntegerModifier(thisArg)).getBukkitEntity();
				if (target !=null)	aH.echoDebug("...Target is " + target.toString(), thisArg);
			}

			else if (thisArg.toUpperCase().contains("PLAYER:")) {
				target = theEntry.getDenizen().getEntity().getServer().getPlayer(aH.getStringModifier(thisArg));
				if (target !=null)	aH.echoDebug("...Target is " + target.toString(), thisArg);
			}

			else {
				aH.echoDebug("...unable to match argument!", thisArg);
			}

		}	

		if (target == null){
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "Could not find target!");
			return false;
		}

		/* Execute the command, if all required variables are filled. */
		if (PotionName !=null){
			try {
				target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(PotionName), duration * 20, amplitude));
				return true;
			} catch (Exception e) {
				return false;
			}
		}
		else
		{
			if (plugin.debugMode) plugin.getLogger().log(Level.INFO, "No Spell Specified!");	
		}

		return false;
	}


}
