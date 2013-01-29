package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Instructs the NPC to follow a player.
 *
 * @author aufdemrand
 *
 */
public class AttackCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("STOP", arg))
                scriptEntry.addObject("stop", true);

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Boolean stop = (Boolean) scriptEntry.getObject("stop");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Player", scriptEntry.getPlayer().getName())
                        + (stop != null ? aH.debugObj("Action", "FOLLOW")
                        : aH.debugObj("Action", "STOP")));

        if (stop != null)
            scriptEntry.getNPC().getNavigator()
                    .cancelNavigation();
        else
            scriptEntry.getNPC().getNavigator()
                    .setTarget(scriptEntry.getPlayer(), false);
    }

}