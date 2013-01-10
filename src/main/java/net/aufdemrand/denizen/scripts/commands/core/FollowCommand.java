package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Configures the Navigator for this NPC
 *
 * @author Jeremy Schroeder
 */

public class FollowCommand extends AbstractCommand {

	/*   */

	/* 
	 * Arguments: [] - Required, () - Optional
	 * 
	 */

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {



        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {

            if (aH.matchesArg("STOP", arg)) {
                scriptEntry.addObject("stop", true);
            }

        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        if (scriptEntry.getObject("stop") != null) scriptEntry.getNPC().getNavigator().cancelNavigation();
        else
            scriptEntry.getNPC().getNavigator().setTarget(scriptEntry.getPlayer(), false);

        dB.echoApproval("");
    }

    @Override
    public void onEnable() {

    }
}