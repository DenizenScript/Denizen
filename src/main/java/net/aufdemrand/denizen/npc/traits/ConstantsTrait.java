package net.aufdemrand.denizen.npc.traits;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ScriptsReloadEvent;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Paginator;


public class ConstantsTrait extends Trait {

	private Denizen denizen;
	private Map<String, String> constants = new HashMap<String, String>();
	private Map<String, String> assignmentConstants = new HashMap<String, String>();
	private String assignment = null;

	public ConstantsTrait() {
		super("constants");
		denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
	}

	@Override 
	public void load(DataKey key) throws NPCLoadException {
		if (denizen == null) denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		for (DataKey keyToLoad : key.getSubKeys()) {
			constants.put(keyToLoad.name().toLowerCase(), keyToLoad.getString(""));
		}
	}

	@Override public void save(DataKey key) {
		for (Entry<String, String> entry : constants.entrySet()) {
			key.setString(entry.getKey().toLowerCase(), entry.getValue());
		}
	}

	public String getConstant(String name) {
		if (constants.containsKey(name.toLowerCase()))
			return constants.get(name.toLowerCase());
		else if (getAssignmentConstants().containsKey(name.toLowerCase()))
			return assignmentConstants.get(name.toLowerCase());
		return null;
	}

	public void setConstant(String name, String value) {
		constants.put(name.toLowerCase(), value);
	}

	public void removeConstant(String name) {
		if (constants.containsKey(name.toLowerCase()))
			constants.remove(name.toLowerCase());
	}

	public boolean hasNPCConstants() {
		return !constants.isEmpty();
	}

	public void describe(CommandSender sender, int page) throws CommandException {
		Paginator paginator = new Paginator().header("Constants for " + npc.getName());
		paginator.addLine("<e>NPC-specific constants: " + (hasNPCConstants() ? "" : "None.") + "");
		if (hasNPCConstants()) paginator.addLine("<e>Key: <a>Name  <b>Value");
		int x = 0;
		for (Entry<String, String> constant : constants.entrySet()) {
			paginator.addLine("<a> " + String.valueOf(constant.getKey().charAt(0)).toUpperCase() + constant.getKey().substring(1) + "<b>  " + constant.getValue());
			x++;
		}
		paginator.addLine("");

		if (npc.hasTrait(AssignmentTrait.class) && npc.getTrait(AssignmentTrait.class).hasAssignment()) {
			getAssignmentConstants();
			paginator.addLine("<e>Constants for assignment '" + assignment.toUpperCase() + "':");
			paginator.addLine("<e>Key: <a>Name  <b>Value");
			for (Entry<String, String> constant : getAssignmentConstants().entrySet()) {
				if (constants.containsKey(constant.getKey())) 
					paginator.addLine("<m>" + String.valueOf(constant.getKey().charAt(0)).toUpperCase() + constant.getKey().substring(1) + "<r>  <m>" + constant.getValue());
				else paginator.addLine("<a>" + String.valueOf(constant.getKey().charAt(0)).toUpperCase() + constant.getKey().substring(1) + "<b>  " + constant.getValue());
			}
			paginator.addLine("");
		}

		if (!paginator.sendPage(sender, page))
			throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
	}

	public Map<String, String> getAssignmentConstants() {
		if (npc.hasTrait(AssignmentTrait.class) && npc.getTrait(AssignmentTrait.class).hasAssignment()) {
			// Has assignment
			if (assignment != null && assignment.equalsIgnoreCase(npc.getTrait(AssignmentTrait.class).getAssignment()))
				return assignmentConstants;
			else return rebuildAssignmentConstants();
		}
		return assignmentConstants;
	}

	private Map<String, String> rebuildAssignmentConstants() {

		if (!npc.hasTrait(AssignmentTrait.class)) {
			assignmentConstants.clear();
			return assignmentConstants;
		}
		
		assignment = npc.getTrait(AssignmentTrait.class).getAssignment();
		assignmentConstants.clear();

		if (denizen.getScripts().contains(assignment.toUpperCase() + ".DEFAULT CONSTANTS")) 

			for (String constant : denizen.getScripts().getConfigurationSection(assignment.toUpperCase() + ".DEFAULT CONSTANTS").getKeys(false))
				assignmentConstants.put(constant.toLowerCase(), denizen.getScripts().getString(assignment.toUpperCase() + ".DEFAULT CONSTANTS." + constant.toUpperCase(), ""));

		return assignmentConstants;
	}

	@EventHandler
	public void onScriptsReload(ScriptsReloadEvent event) {
		rebuildAssignmentConstants();
	}
	
}
