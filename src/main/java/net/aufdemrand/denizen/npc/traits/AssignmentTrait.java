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

        // Interact Scripts
        boolean scriptsPresent = false;
        paginator.addLine(ChatColor.GRAY + "Interact Scripts:");
        paginator.addLine("<e>Priority  <a>Name");
        if (!sH.getStringListIgnoreCase(assignment + ".INTERACT SCRIPTS").isEmpty()) {
            for (String scriptEntry : sH.getStringListIgnoreCase(assignment + ".INTERACT SCRIPTS"))
                paginator.addLine("<e>" + scriptEntry.split(" ")[0] + " <a> " + scriptEntry.split(" ")[1]);
        } if (!scriptsPresent) paginator.addLine(ChatColor.RED + "  No scripts assigned!");
        paginator.addLine("");
                
        if (!scriptsPresent) {
            if (!paginator.sendPage(sender, page))
                throw new CommandException(Messages.COMMAND_PAGE_MISSING);
            return;
        }

        // Scheduled Activities
        paginator.addLine(ChatColor.GRAY + "Scheduled Scripts:");
        paginator.addLine("<e>Time  <a>Name");
        if (!sH.getStringListIgnoreCase(assignment + ".INTERACT SCRIPTS").isEmpty()) {
            for (String scriptEntry : sH.getStringListIgnoreCase(assignment + ".INTERACT SCRIPTS"))
                paginator.addLine(ChatColor.GRAY + "- " + ChatColor.GREEN + scriptEntry);
        } if (!scriptsPresent) paginator.addLine(ChatColor.RED + "  No scripts assigned!");
        paginator.addLine("");

        // Linked Notable Locations/Blocks
        paginator.addLine(ChatColor.GRAY + "Linked Notable Locations:");
        paginator.addLine("<e>Name  <a>World  <b>Location");
        for (Notable notable : denizen.notableManager().getNotables()) {
            if (notable.hasLink(npc)) paginator.addLine(notable.describe());
        } paginator.addLine("");

        // TODO: Actions
        paginator.addLine(ChatColor.GRAY + "Actions:");
        paginator.addLine("<e>Name  <a>Script Size  <b>First Command");
        paginator.addLine("");
        
                if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING);
        }

}
