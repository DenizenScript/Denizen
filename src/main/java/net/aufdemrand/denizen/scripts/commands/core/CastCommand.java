package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;

/**
 * Switches a button or lever.
 * 
 * @author Jeremy Schroeder, Mason Adkins
 */

public class CastCommand extends AbstractCommand{

	@Override
	public void onEnable() {
		// nothing to do here
	}

	/* CAST [POTION_EFFECT] (DURATION:#) (POWER:#) (NPCID:#) (PLAYER:PlayerName) */

	/* 
	 * Arguments: [] - Required, () - Optional 
	 * [POTION_EFFECT] Uses bukkit enum for specifying the potion effect to use.
	 *   
	 * Example Usage:
	 * 
	 */
	
	//Initialize variables 
	String potionName = null;
	int duration = 60;
	int amplitude = 1;
	LivingEntity target;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
		target = scriptEntry.getPlayer();
		
		for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesDuration(arg)) {
                duration = Integer.valueOf(arg.split(":")[1]);
                dB.echoDebug(Messages.DEBUG_SET_DURATION, arg);
                continue;
                
            } 
            
            else if (aH.matchesValueArg("SPELL", arg, ArgumentType.String)) {
				potionName = aH.getStringFrom(arg);
				dB.echoDebug("...will cast effect '%s'.", arg);
				continue;
			}

			else if (aH.getStringFrom(arg).equalsIgnoreCase("POWER:")) {
				amplitude = aH.getIntegerFrom(arg);
				dB.echoDebug("...power set to '%s'.", arg);
				continue;
				
			}

// 			LEFT OUT DUE TO LACK OF .matchesNPCID()
//			else if (aH.matchesNPCID(arg)) {
//				target = aH.getNPCIDModifier(arg).getEntity();
//				if (target !=null)	dB.echoDebug("...now targeting '%s'.", arg);
//				continue;
//			}

			else if (aH.getStringFrom(arg).equalsIgnoreCase("PLAYER:")) {
				target = scriptEntry.getNPC().getEntity().getServer().getPlayer(aH.getStringFrom(arg));
				if (target !=null)	dB.echoDebug("...now targeting '%s'.", arg);
				continue;
				
			}

			else {
				potionName = aH.getStringFrom(arg);
				dB.echoDebug("...will cast effect '%s'.", arg);
				continue;
				
			}
		}
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {
		if (target == null) {
			dB.echoError("Could not find target! Perhaps you specified a non-existing NPCID?");
			return;
		}
		
		if (potionName !=null) {
			try {
				target.addPotionEffect(new PotionEffect(PotionEffectType.getByName(potionName), duration * 20, amplitude));
				return;
			} catch (Exception e) {
				dB.echoError("Invalid potion effect! Check syntax.");
				return;
			}
		} 
		
		else dB.echoError("Usage: CAST [SpellName] (DURATION:#) (POWER:#) (NPC:#) (PLAYER:PlayerName)");
		return;
	}	
}