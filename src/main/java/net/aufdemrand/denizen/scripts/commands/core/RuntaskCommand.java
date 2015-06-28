package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;


@Deprecated
public class RuntaskCommand extends AbstractCommand {
    // TODO: REMOVE ENTIRE COMMAND IN 1.0
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Warn the console!
        dB.log("Notice: This command has been deprecated. Use instead 'run' or 'inject'!");
    }
}
