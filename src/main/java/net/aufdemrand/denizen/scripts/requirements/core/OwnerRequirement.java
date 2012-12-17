package net.aufdemrand.denizen.scripts.requirements.core;

import java.util.List;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.citizensnpcs.api.trait.trait.Owner;

public class OwnerRequirement extends AbstractRequirement{

	@Override
	public void onEnable() {
		// nothing to do here
	}

	@Override
	public boolean check(Player player, DenizenNPC npc, String scriptName,
			List<String> args) throws RequirementCheckException {
		boolean outcome = false;

		if (npc.getCitizen().getTrait(Owner.class).getOwner().equalsIgnoreCase(player.getName())) {
			dB.echoDebug("...denizen owner: " + npc.getCitizen().getTrait(Owner.class).getOwner());
			outcome = true;
		}

		if (outcome == true) dB.echoDebug("...player is owner!");
		else dB.echoDebug("...player is not owner!");
		
		return outcome;
	}

}
