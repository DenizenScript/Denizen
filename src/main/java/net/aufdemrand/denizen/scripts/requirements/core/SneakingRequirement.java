package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class SneakingRequirement extends AbstractRequirement {

	@Override
	public void onEnable() {
		// nothing to do here
	}

	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		boolean outcome = false;

		if (player.isSneaking()) {
			outcome = true;
		}

		if (outcome == true) dB.echoDebug("...player is sneaking!");
		else dB.echoDebug("...player is not sneaking!");

		return outcome;
	}
}
