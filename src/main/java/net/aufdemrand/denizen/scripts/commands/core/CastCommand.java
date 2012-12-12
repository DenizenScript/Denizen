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
 * 'Casts' a Minecraft Potion_Effect
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
	
	// Initialize variables 
	PotionEffect potionEffect;
	LivingEntity target;

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
		int duration = 60;
		int amplifier = 1;
		PotionEffectType potion = null;
		
		// Default target as Player, if no Player, default target to NPC
		if (scriptEntry.getPlayer() != null) target = scriptEntry.getPlayer();
		else if (scriptEntry.getNPC() != null) target = scriptEntry.getNPC().getEntity();

		for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesDuration(arg)) {
                duration = Integer.valueOf(arg.split(":")[1]);
                dB.echoDebug(Messages.DEBUG_SET_DURATION, arg);
                continue;
                
            }   else if (aH.matchesValueArg("TYPE", arg, ArgumentType.Custom)) {
				try {
					potion = PotionEffectType.getByName(aH.getStringFrom(arg));
					dB.echoDebug(Messages.DEBUG_SET_TYPE, aH.getStringFrom(arg));
				} catch (Exception e) {
					dB.echoError("Invalid PotionEffectType!");
				}
				continue;

            }	else if (aH.matchesValueArg("POWER",  arg,  ArgumentType.Integer)) {
				amplifier = aH.getIntegerFrom(arg);
				dB.echoDebug("...set POWER to '%s'.", String.valueOf(amplifier));
				continue;
				
			}   else if (aH.matchesValueArg("TARGET", arg, ArgumentType.Custom)) {
				if (aH.getStringFrom(arg).equalsIgnoreCase("PLAYER")
						&& scriptEntry.getPlayer() != null) target = scriptEntry.getPlayer();
				else if (aH.getStringFrom(arg).equalsIgnoreCase("NPC")
					&& scriptEntry.getNPC() != null) target = scriptEntry.getNPC().getEntity();
				else dB.echoError("Invalid TARGET type or unavailable TARGET object! Valid: PLAYER, NPC");
				continue;
				
			}   else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
		}

        if (potion == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "TYPE");
        if (target == null) throw new InvalidArgumentsException("No target Object! Perhaps you specified a non-existing Player or NPCID? Use PLAYER:player_name or NPCID:#.");
        
		potionEffect = new PotionEffect(potion, duration, amplifier);
	}

	@Override
	public void execute(String commandName) throws CommandExecutionException {

		// Apply the Potion_Effect!
		potionEffect.apply(target);
		
	}	
}