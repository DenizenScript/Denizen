package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.craftbukkit.v1_4_5.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;
import net.minecraft.server.v1_4_5.Packet18ArmAnimation;

/**
 * Heals/Harms the Player/Denizen.
 * 
 * @author Jeremy Schroeder, Mason Adkins
 */

public class HealCommand3 extends AbstractCommand {

    /* HEAL|HARM (DENIZEN) (AMT|QTY:#) */

    /* 
     * Arguments: [] - Required, () - Optional 
     * (QTY|AMT:#) sets the amount to heal/harm the target.
     * (DENIZEN) selects the Denizen as the target of the command.
     * 
     * Example Usage:
     * HEAL
     * HEAL QTY:5
     * HARM DENIZEN AMT:12
     * 
     */
	
	boolean hurts = false;
	LivingEntity target = null;
	Integer amount = null;
	
	@Override
	public void onEnable() {
		// nothing needed here
	}

	@Override
	public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
		
		if (scriptEntry.getPlayer() == null) {
			target = scriptEntry.getNPC().getEntity();			
		} else {
			target = scriptEntry.getPlayer();
		}
		
		hurts = scriptEntry.getCommand().equalsIgnoreCase("HARM");
		
		for (String arg : scriptEntry.getArguments()) {
			if (aH.getStringFrom(arg).equalsIgnoreCase("DENIZEN")) {
				if (scriptEntry.getNPC() != null) {
					target = scriptEntry.getNPC().getEntity();
					dB.echoDebug("...targeting '" + scriptEntry.getNPC().getName() + "'.");
				} else dB.echoError("Seems this was sent from a TASK-type script. Must use NPCID:# to specify a Denizen NPC.");
				continue;

//			LEFT OUT DUE TO NO .matchesNPCID()	
//			} else if (aH.matchesNPCID(arg)) {
//				target = aH.getNPCIDModifier(arg).getEntity();
//				if (target != null) dB.echoDebug("...now targeting '%s'.", arg);
				
			} else if (arg.matches("(?:QTY|qty|Qty|AMT|Amt|amt|AMOUNT|Amount|amount)(:)(\\d+)")) {
				amount = aH.getIntegerFrom(arg);
				dB.echoDebug("...amount set to '" + amount + "'.");
				continue;
				
			} else throw new InvalidArgumentsException(Messages.ERROR_UNKNOWN_ARGUMENT, arg);
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