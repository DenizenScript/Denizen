package net.aufdemrand.denizen.npc.traits;

import net.aufdemrand.denizen.events.ScriptReloadEvent;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.command.exception.CommandException;
import net.citizensnpcs.api.util.Paginator;
import net.citizensnpcs.util.Messages;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class ConstantsTrait extends Trait {

    // Saved to C2 saves.yml
    @Persist(value="", collectionType = ConcurrentHashMap.class)
    private Map<String, String> constants = new HashMap<String, String>();

    // Used internally
    private Map<String, String> assignmentConstants = new HashMap<String, String>();
    private String assignment = null;

    public ConstantsTrait() {
        super("constants");
    }


    /**
     * Returns the value of the specified Constant, unique to this NPC. Note:
     * Returns tags filled with the currently assigned NPC. See: Denizen TagManager
     *
     * @param name name of constant, case in-sensitive
     * @return value of the constant
     */
    public String getConstant(String name) {

        getAssignmentConstants();

        if (constants.containsKey(name.toLowerCase()))
            return TagManager.tag(null, DenizenAPI.getDenizenNPC(npc), constants.get(name.toLowerCase()), false);
        else if (getAssignmentConstants().containsKey(name.toLowerCase()))
            return TagManager.tag(null, DenizenAPI.getDenizenNPC(npc), assignmentConstants.get(name.toLowerCase()), false);
        return null;
    }


    /**
     * Gets a map of the NPCs constants. Note: Does not include any constants
     * inherited by the NPCs Assignment. To grab a comprehensive map of both,
     * use {@link #getAllConstants()}.
     *
     * @return a map of constants, keyed by constant name.
     */
    public Map<String, String> getNPCConstants() {
        return constants;
    }


    /**
     * Gets a map of the NPCs constants, including those inherited by the Assignment.
     * Any constants that are overridden by this NPC are taken into account, so this
     * map may differ from the constants found in the Assignment. To get a map of
     * constants from an Assignment, use {@link #getAssignmentConstants()).
     *
     * @return a map of constants, keyed by constant name.
     */
    public Map<String, String> getAllConstants() {
        Map<String, String> allConstants = new HashMap<String, String>();
        getAssignmentConstants().putAll(allConstants);
        getNPCConstants().putAll(allConstants);
        return allConstants;
    }


    /**
     * Sets the value of a constant, as identified by the name. This will
     * override any constants inherited from the NPCs Assignment.
     *
     * @param name  name of the constant, case in-sensitive
     * @param value value of the constant
     */
    public void setConstant(String name, String value) {
        constants.put(name.toLowerCase(), value);
    }


    /**
     * Removes an NPC-specific constant, as identified by the name. This will
     * not remove any values inherited from an NPCs Assignment, only constants
     * unique to this NPC.
     *
     * @param name name of the constant, case in-sensitive
     */
    public void removeConstant(String name) {
        if (constants.containsKey(name.toLowerCase()))
            constants.remove(name.toLowerCase());
    }


    /**
     * Checks if this NPC has any unique constants, beyond what is inherited from
     * the NPCs Assignment.
     *
     * @return true if NPC constants are present
     */
    public boolean hasNPCConstants() {
        return !constants.isEmpty();
    }


    public Map<String, String> getAssignmentConstants() {
        // Check to make sure NPC has an assignment
        if (npc.hasTrait(AssignmentTrait.class) && npc.getTrait(AssignmentTrait.class).hasAssignment()) {
            // Check to make sure assignment hasn't changed.. if it has, the assignmentConstants map will be rebuilt
            if (assignment != null && assignment.equalsIgnoreCase(npc.getTrait(AssignmentTrait.class).getAssignment().getName()))
                return assignmentConstants;
            else return rebuildAssignmentConstants();
        }
        return assignmentConstants;
    }


    public Map<String, String> rebuildAssignmentConstants() {
        // Builds a map of constants inherited from the NPCs current Assignment
        if (!npc.hasTrait(AssignmentTrait.class) || !npc.getTrait(AssignmentTrait.class).hasAssignment()) {
            assignmentConstants.clear();
            return assignmentConstants;
        }

        if (npc.getTrait(AssignmentTrait.class).getAssignment() != null) {
            assignment = npc.getTrait(AssignmentTrait.class).getAssignment().getName();
            assignmentConstants.clear();
        } else return assignmentConstants;

        if (ScriptRegistry.getScriptContainer(assignment).contains("DEFAULT CONSTANTS"))
            for (String constant : ScriptRegistry.getScriptContainer(assignment).getConfigurationSection("DEFAULT CONSTANTS").getKeys(false))
                assignmentConstants.put(constant.toLowerCase(),
                        ScriptRegistry.getScriptContainer(assignment)
                                .getString("DEFAULT CONSTANTS." + constant.toUpperCase(), ""));

        return assignmentConstants;
    }


    /**
     * Rebuilds assignment constants on a script reload
     *
     * @param event
     */
    @EventHandler
    public void onScriptsReload(ScriptReloadEvent event) {
        rebuildAssignmentConstants();
    }


    public void describe(CommandSender sender, int page) throws CommandException {
        Paginator paginator = new Paginator().header("Constants for " + npc.getName());
        paginator.addLine("<e>NPC-specific constants: " + (hasNPCConstants() ? "" : "None.") + "");
        if (hasNPCConstants()) paginator.addLine("<e>Key: <a>Name  <b>Value");
        for (Entry<String, String> constant : constants.entrySet()) {
            paginator.addLine("<a> " + String.valueOf(constant.getKey().charAt(0)).toUpperCase() + constant.getKey().substring(1) + "<b>  " + constant.getValue());
        }
        paginator.addLine("");

        if (npc.hasTrait(AssignmentTrait.class) && npc.getTrait(AssignmentTrait.class).hasAssignment()) {
            getAssignmentConstants();
            // List constants inherited from an Assignment.
            paginator.addLine("<e>Constants for assignment '" + assignment.toUpperCase() + "':");
            paginator.addLine("<e>Key: <a>Name  <b>Value");
            for (Entry<String, String> constant : getAssignmentConstants().entrySet()) {
                // If a constant from the Assignment has been overridden by a NPC constant,
                // change formatting to indicate so.
                if (constants.containsKey(constant.getKey()))
                    paginator.addLine("<m>" + String.valueOf(constant.getKey().charAt(0)).toUpperCase() + constant.getKey().substring(1) + "<r>  <m>" + constant.getValue());
                else paginator.addLine("<a>" + String.valueOf(constant.getKey().charAt(0)).toUpperCase() + constant.getKey().substring(1) + "<b>  " + constant.getValue());
            }
            paginator.addLine("");
        }

        if (!paginator.sendPage(sender, page))
            throw new CommandException(Messages.COMMAND_PAGE_MISSING, page);
    }

}