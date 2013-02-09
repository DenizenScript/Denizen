package net.aufdemrand.denizen.scripts.requirements.core;

import net.aufdemrand.denizen.exceptions.RequirementCheckException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.requirements.AbstractRequirement;
import net.aufdemrand.denizen.scripts.requirements.RequirementsContext;
import net.aufdemrand.denizen.utilities.arguments.Script;
import net.aufdemrand.denizen.utilities.arguments.aH;

import java.util.List;

public class ProcedureRequirement extends AbstractRequirement{


    @Override
    public boolean check(RequirementsContext context, List<String> args) throws RequirementCheckException {

		boolean outcome = false;
        Script script = null;

        for (String arg : args) {

            if (aH.matchesScript(arg))
                script = aH.getScriptFrom(arg);

        }

        // Build script entries
        List<ScriptEntry> entries = script.getContainer().getBaseEntries(context.getPlayer(), context.getNPC());

        if (entries.isEmpty()) return outcome;

        long id = DetermineCommand.getNewId();

        // Execute scriptEntries
        for (ScriptEntry scriptEntry : entries) {
            scriptEntry.addObject("reqId", id);
            plugin.getScriptEngine().getScriptExecuter().execute(scriptEntry);
            if (DetermineCommand.outcomes.containsKey(id)) {
                outcome = DetermineCommand.outcomes.get(id);
                break;
            }
        }

		return outcome;
	}
}
