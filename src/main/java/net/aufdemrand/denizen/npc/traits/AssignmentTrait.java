package net.aufdemrand.denizen.npc.traits;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.notables.Notable;
import net.aufdemrand.denizen.scripts.helpers.ScriptHelper;
import net.citizensnpcs.api.exception.NPCLoadException;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.util.DataKey;
import net.citizensnpcs.command.exception.CommandException;
import net.citizensnpcs.util.Messages;
import net.citizensnpcs.util.Paginator;


public class AssignmentTrait extends Trait {

	private Denizen denizen;
	private String assignment = "";

	public AssignmentTrait() {
		super("assignment");
		denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
	}

	@Override 
	public void load(DataKey key) throws NPCLoadException {
		if (denizen == null) denizen = (Denizen) Bukkit.getServer().getPluginManager().getPlugin("Denizen");
		assignment = key.getString("assignment");
		if (hasAssignment()) 
			if (!checkAssignment(this.assignment))
				denizen.getDebugger().echoError("Missing assignment '" + assignment + "' for NPC '" 
						+ npc.getName() + "/" + npc.getId() + "! Perhaps the script has been removed?");
	}

	@Override public void save(DataKey key) {
		key.setString("assignment", assignment);
	}

	public boolean hasAssignment() {
		if (assignment.equals(""))
			return false;
		else return true;
	}

	public String getAssignment() {
		return assignment;
	}

	public void removeAssignment (Player player) {
		assignment = "";
		denizen.getNPCRegistry().getDenizen(npc).action("remove assignment", player);
	}

	public void removeAssignment() {
		removeAssignment(null);
	}

	public boolean checkAssignment(String assignment) {
		if (denizen.getScriptEngine().getScriptHelper().getStringIgnoreCase(assignment + ".TYPE", "") != null &&
				denizen.getScriptEngine().getScriptHelper().getStringIgnoreCase(assignment + ".TYPE", "").equalsIgnoreCase("ASSIGNMENT")) 
			return true;
		return false;
	}

	public boolean setAssignment(String assignment) {
		return setAssignment(assignment, null);
	}

	public boolean setAssignment(String assignment, Player player) {
		if (checkAssignment(assignment)) {
			this.assignment = assignment.toUpperCase();
			denizen.getNPCRegistry().getDenizen(npc).action("assignment", player);
			return true;
		}   else return false;
	}

	public void describe(CommandSender sender, int page) throws CommandException {
		ScriptHelper sH = denizen.getScriptEngine().getScriptHelper();
		Paginator paginator = new Paginator().header("Assignment");
		paginator.addLine("<e>Current assignment: " + (hasAssignment() ? this.assignment : "None.") + "");
		paginator.addLine("");

		if (!hasAssignment()) {
			paginator.sendPage(sender, page);
			return;
		}
		
		// Interact Scripts
		boolean entriesPresent = false;
		paginator.addLine(ChatColor.GRAY + "Interact Scripts:");
		paginator.addLine("<e>Key: <a>Priority  <b>Name");
		if (!sH.getStringListIgnoreCase(assignment + ".INTERACT SCRIPTS").isEmpty()) {
			entriesPresent = true;
			for (String scriptEntry : sH.getStringListIgnoreCase(assignment + ".INTERACT SCRIPTS"))
				paginator.addLine("<a>" + scriptEntry.split(" ")[0] + "<b> " + scriptEntry.split(" ", 2)[1]);
		} if (!entriesPresent) paginator.addLine("<c>No Interact Scripts assigned.");
		paginator.addLine("");

		if (!entriesPresent) {
			if (!paginator.sendPage(sender, page))
				throw new CommandException(Messages.COMMAND_PAGE_MISSING);
			return;
		}

		// Scheduled Activities
		entriesPresent = false;
		paginator.addLine(ChatColor.GRAY + "Scheduled Scripts:");
		paginator.addLine("<e>Key: <a>Time  <b>Name");
		if (!sH.getStringListIgnoreCase(assignment + ".SCHEDULED ACTIVITIES").isEmpty()) {
			entriesPresent = true;
			for (String scriptEntry : sH.getStringListIgnoreCase(assignment + ".SCHEDULED ACTIVITIES"))
				paginator.addLine("<a>" + scriptEntry.split(" ")[0] + "<b> " + scriptEntry.split(" ", 2)[1]);
		} if (!entriesPresent) paginator.addLine("<c>No scheduled scripts activities.");
		paginator.addLine("");

		// Linked Notable Locations/Blocks
		entriesPresent = false;
		paginator.addLine(ChatColor.GRAY + "Linked Notable Locations:");
		paginator.addLine("<e>Key: <a>Name  <b>World  <c>Location");
		if (!denizen.notableManager().getNotables().isEmpty()) entriesPresent = true;
		for (Notable notable : denizen.notableManager().getNotables())
			if (notable.hasLink(npc.getId())) paginator.addLine(notable.describe());  
		if (!entriesPresent) paginator.addLine("<c>No notable locations linked to this NPC."); 
		paginator.addLine("");

		// Actions
		entriesPresent = false;
		paginator.addLine(ChatColor.GRAY + "Actions:");
		paginator.addLine("<e>Key: <a>Action name  <b>Script Size");
		if (!denizen.getScripts().contains(assignment.toUpperCase() + ".Actions")) entriesPresent = true;
		for (String constant : denizen.getScripts().getConfigurationSection(assignment.toUpperCase() + ".ACTIONS").getKeys(false))
			paginator.addLine("<a>" + constant + " <b>" + sH.getStringListIgnoreCase(assignment + ".ACTIONS." + constant).size());
		if (!entriesPresent) paginator.addLine("<c>No actions defined in the assignment."); 
		paginator.addLine("");


		if (!paginator.sendPage(sender, page))
			throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
	}

}
