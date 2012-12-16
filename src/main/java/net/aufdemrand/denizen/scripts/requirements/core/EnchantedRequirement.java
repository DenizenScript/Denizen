package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.helpers.ArgumentHelper.ArgumentType;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;

public class EnchantedRequirement extends AbstractRequirement{
	private enum CheckType { ITEMINHAND };
	@Override
	public void onEnable() {
		
	}

	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		boolean outcome = false;
		CheckType checkType = null;

//	    HOW DO YOU WANT TO HANDLE NO ARGS IN REQUIREMENTS?
//		if (arguments == null)
//			throw new RequirementMissingException("Must provide arguments!");

		/* Get arguments */

		for (String thisArg : args) {

			if (aH.matchesValueArg("ITEMINHAND", thisArg, ArgumentType.Custom)) {
				checkType = CheckType.ITEMINHAND;
				dB.echoDebug("...will check item in hand");
			}

			else dB.echoError("Could not match argument '%s'!", thisArg);
		}


		if (checkType != null) {
			switch (checkType) {

			case ITEMINHAND:
				if (!player.getItemInHand().getEnchantments().isEmpty()) outcome = true;
				break;

			}
		}

		if (outcome == true) dB.echoDebug("...item is enchanted.");
		else dB.echoDebug("...item is not enchanted!");

		return outcome;
	}

}
