package net.aufdemrand.denizen.activities;

import java.rmi.activation.ActivationException;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.commands.ArgumentHelper;
import net.aufdemrand.denizen.npc.DenizenNPC;

import org.bukkit.Bukkit;

public abstract class AbstractActivity {

	public Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
	public String activityName;
	
	public ArgumentHelper aH = plugin.getCommandRegistry().getArgumentHelper();
		
	/* Activates the command class as a Denizen Command. Should be called on startup. */
	
	public void activateAs(String activityName) throws ActivationException {
	
		/* If more than one word, error. */
		if (activityName.split(" ").length > 1) throw new ActivationException("Activity names can only be one word.");
		
		/* Use Bukkit to reference Denizen Plugin */
		Denizen plugin = (Denizen) Bukkit.getPluginManager().getPlugin("Denizen");
		
		/* Register command with Registry */
		if (plugin.getActivityRegistry().registerActivity(activityName, this)) return;
		else 
			throw new ActivationException("Error activating Activity with Activity Registry.");
	}
	
	abstract public void addGoal(DenizenNPC npc, String[] arguments, int priority);
	
	abstract public void removeGoal(DenizenNPC npc, boolean verbose);
	
}
