package net.aufdemrand.denizen.commands.core;

import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;

import net.aufdemrand.denizen.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.citizensnpcs.command.exception.CommandException;
import net.minecraft.server.Packet18ArmAnimation;

public class HealCommand extends AbstractCommand {

	/* HEAL/HARM */
	/* 
	 * Arguments: [] - Required, () - Optional 
	 * (DENIZEN) (AMOUNT:#)
	 *   
	 * Example Usage:
	HEAL
	HARM DENIZEN 1
	 */
	@Override
	public boolean execute(ScriptEntry theEntry) throws CommandException {

		boolean hurt = false;
		LivingEntity target =null;

		if (theEntry.getPlayer() ==null){
			target = theEntry.getPlayer();			
		}

		Integer  amount = null;

		hurt =theEntry.getCommand().equalsIgnoreCase("HARM");

		for (String thisArg : theEntry.arguments()) {

			if (thisArg.toUpperCase().contains("DENIZEN")){
				target = theEntry.getDenizen().getEntity();
				aH.echoDebug("targeting Denizen");
			}

			if (thisArg.toUpperCase().contains("AMOUNT:")){
				amount = aH.getIntegerModifier(thisArg);
				aH.echoDebug("amount set to " + amount);
			}
		}

		//execute
		if (target !=null) {
			if (hurt) {
				if (amount == null) amount = 1;
				target.setHealth(target.getHealth() - amount);		
				net.citizensnpcs.util.Util.sendPacketNearby(target.getLocation(),new Packet18ArmAnimation( ((CraftEntity)target).getHandle(),2) , 64); //hurt
				return true;
			}
			else{
				if (amount == null) amount = target.getMaxHealth() - target.getHealth();
				target.setHealth(target.getHealth() + amount);			
				net.citizensnpcs.util.Util.sendPacketNearby(target.getLocation(),new Packet18ArmAnimation( ((CraftEntity)target).getHandle(),6) , 64); //white sparks
				return true;
			}

		}


		return false;

	}

}
