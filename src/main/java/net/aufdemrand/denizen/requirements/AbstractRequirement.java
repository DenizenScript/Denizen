package net.aufdemrand.denizen.requirements;

import java.rmi.activation.ActivationException;

import net.aufdemrand.denizen.Denizen;
import net.citizensnpcs.command.exception.RequirementMissingException;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;

public abstract class AbstractRequirement {

	public Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	
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

	public abstract boolean check(LivingEntity theEntity, String theScript, String[] strings, Boolean negativeRequirement) throws RequirementMissingException;

}
