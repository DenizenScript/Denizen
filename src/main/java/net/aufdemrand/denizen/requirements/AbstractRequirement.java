package net.aufdemrand.denizen.requirements;

import java.rmi.activation.ActivationException;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.commands.ArgumentHelper;
import net.aufdemrand.denizen.npc.DenizenNPC;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public abstract class AbstractRequirement {

	public Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	
	public ArgumentHelper aH = plugin.getCommandRegistry().getArgumentHelper();
	
	/* Activates the command class as a Denizen Requirement. Should be called on startup. */
	
	public void activateAs(String requirementName) throws ActivationException {
	
		/* Use Bukkit to reference Denizen Plugin */
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		/* Register command with Registry */
		if (plugin.getRequirementRegistry().registerRequirement(requirementName, this)) return;
		else 
			throw new ActivationException("Error activating Command with Requirement Registry.");
	}


	
	/* Execute is the method called when the Denizen Requirement is called from a script.
	 * If the requirement is met, the method should return true. */

	public abstract boolean check(Player thePlayer, DenizenNPC theDenizen, String scriptName, String[] args, Boolean negativeRequirement) throws RequirementMissingException;

}
