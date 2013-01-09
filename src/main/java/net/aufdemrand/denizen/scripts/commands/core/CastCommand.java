package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.arguments.aH.ArgumentType;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * <p>'Casts' a Bukkit PotionEffectType on a LivingEntity target.</p>
 * 
 * <br><b>dScript Usage:</b><br>
 * <pre>CAST [PotionEffectType] (TARGET:NPC|{PLAYER}|LivingEntity) 
 *     (CASTER:{NPC}|PLAYER|LivingEntity) (DURATION:#) (POWER:#)</pre>
 * 
 * <ol><tt>Arguments: [] - Required, () - Optional, {} - Default</ol></tt>
 * 
 * <ol><tt>[PotionEffectType]</tt><br> 
 *         Uses Bukkit's PotionEffectType for specifying the potion effect to use. 
 *         See below for a list of valid PotionEffectTypes.</ol>
 * 
 * <ol><tt>(TARGET:NPC|{PLAYER}|ENTITY.entity|NPC.npcid|PLAYER.player_name)</tt><br> 
 *         Optional. Defaults to the attached Player. The recipient of the PotionEffectType. </ol>
 * 
 * <ol><tt>(DURATION:#{60})</tt><br>
 *         Optional. Number of seconds that the PotionEffectType lasts. If not specified,
 *         assumed 60 seconds.</ol>
 * 
 * <ol><tt>(POWER:#{1})</tt><br>
 *         Optional. A higher amplifier means the potion effect happens more often over
 *         its duration and in some cases has more effect on its target. Usually effective 
 *         between 1-3.</ol>
 * 
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - CAST NIGHT_VISION DURATION:60 <br>
 *  - CAST WITHER TARGET:NPC NPCID:&#60;FLAG.P:enemy_NPCID> CASTER:PLAYER <br>
 *  - CAST REGENERATION DURATION:10 POWER:3 <br>
 *  - CAST CONFUSION TARGET:NPC.25 DURATION:60
 * </ol></tt>
 * 
 * <br><b>Extended Usage:</b><br>
 * <ol><tt>
 *  Script: <br>
 *  - ^ENGAGE NOW DURATION:10 <br>
 *  - ^LOOKCLOSE TOGGLE:TRUE DURATION:10 <br>
 *  - CHAT 'The night-time is blinding around here. Allow me to give you sight.' <br>
 *  - WAIT 2 <br>
 *  - ^ANIMATE ANIMATION:ARM_SWING <br>
 *  - CAST NIGHT_VISION DURATION:360 <br>
 *  - NARRATE 'You can see through the night!' <br>
 * </ol></tt>
 * 
 * <br><b>Valid PotionEffectTypes (as of 12/28/12)</b><br>
 * <ol><tt>
 * BLINDNESS - Blinds an entity. <br>
 * CONFUSION - Warps vision on the client. <br>
 * DAMAGE_RESISTANCE - Decreases damage dealt to an entity. <br>
 * FAST_DIGGING - Increases dig speed. <br>
 * FIRE_RESISTANCE - Stops fire damage. <br>
 * HARM - Hurts an entity. <br>
 * HEAL - Heals an entity. <br>
 * HUNGER - Increases hunger. <br>
 * INCREASE_DAMAGE - Increases damage dealt. <br>
 * INVISIBILITY - Grants invisibility. <br>
 * JUMP - Increases jump height. <br>
 * NIGHT_VISION - Allows an entity to see in the dark. <br>
 * POISON - Deals damage to an entity over time. <br>
 * REGENERATION - Regenerates health. <br>
 * SLOW - Decreases movement speed. <br>
 * SLOW_DIGGING - Decreases dig speed. <br>
 * SPEED - Increases movement speed. <br>
 * WATER_BREATHING - Allows breathing underwater. <br>
 * WEAKNESS - Decreases damage dealt by an entity. <br>
 * WITHER - Deals damage to an entity over time and gives the health to the shooter. <br>
 * </tt></ol>
 * 
 * @author Jeremy Schroeder, Mason Adkins
 * 
 */
public class CastCommand extends AbstractCommand{

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

		// Required fields
		PotionEffect potionEffect;
		LivingEntity target = null;

		int duration = 60;
		int amplifier = 1;
		PotionEffectType potion = null;

		// Default target as Player, if no Player, default target to NPC
		if (scriptEntry.getPlayer() != null) target = scriptEntry.getPlayer();
		else if (scriptEntry.getNPC() != null) target = scriptEntry.getNPC().getEntity();

		// Iterate through arguments
		for (String arg : scriptEntry.getArguments()) {
			
			if (aH.matchesDuration(arg)) {
				duration = Integer.valueOf(arg.split(":")[1]);
				dB.echoDebug(Messages.DEBUG_SET_DURATION, arg);
				continue;

			}	else if (aH.matchesValueArg("POWER", arg, ArgumentType.Integer)) {
				amplifier = aH.getIntegerFrom(arg);
				dB.echoDebug("...set POWER to '%s'.", String.valueOf(amplifier));
				continue;

			}   else if (aH.matchesValueArg("TARGET", arg, ArgumentType.LivingEntity)) {
				// If a scriptEntry LivingEntity (Player/NPC)
				if (aH.getStringFrom(arg).equalsIgnoreCase("PLAYER") && scriptEntry.getPlayer() != null) 
					target = scriptEntry.getPlayer();
				else if (aH.getStringFrom(arg).equalsIgnoreCase("NPC") && scriptEntry.getNPC() != null) 
					target = scriptEntry.getNPC().getEntity();
				// If a saved LivingEntity
				else if (aH.getLivingEntityFrom(arg) != null)
					target = aH.getLivingEntityFrom(arg); 
				else  {
					dB.echoError("Invalid TARGET type or unavailable TARGET object!");
					continue;
				}
				dB.echoDebug("...set TARGET to '%s'", aH.getStringFrom(arg));
		
			}   else if (potion == null) {
				try {
					potion = PotionEffectType.getByName(aH.getStringFrom(arg));
					dB.echoDebug("...set PotionEffectType: '%s'", aH.getStringFrom(arg));
				} catch (Exception e) {
					dB.echoError("Invalid PotionEffectType!");
				}
				continue;
				
			}   else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);		
		}

		if (potion == null) throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "PotionType");
		if (target == null) throw new InvalidArgumentsException("No target Object! Perhaps you specified a non-existing " +
				"Player or NPCID?");

		potionEffect = new PotionEffect(potion, duration, amplifier);
		
		// Save items in the scriptEntry
		scriptEntry.addObject("potion", potionEffect);
		scriptEntry.addObject("target", target);	
	}

	@Override
	public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
		// Apply the Potion_Effect!
		PotionEffect potion = (PotionEffect) scriptEntry.getObject("potion");
		if (!potion.apply((LivingEntity) scriptEntry.getObject("target")))
			dB.echoError("Bukkit was unable to apply '" + potion.getType().getName() + "'.");
	}	
	
}