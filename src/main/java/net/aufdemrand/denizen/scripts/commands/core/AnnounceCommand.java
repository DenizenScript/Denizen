package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * Announces a message to the server.
 * 
 * @author Jeremy Schroeder
 * Version 1.0 Last Updated 11/29 12:21
 */

public class AnnounceCommand extends AbstractCommand {

    @Override
    public void onEnable() {
        // Nothing to do here.
    }

    /* 
     * Arguments: [] - Required, () - Optional 
     * ['Text to announce'] sets the text.
     * 
     * Example Usage:
     * ANNOUNCE 'ANNOUNCEMENT! Today is Christmas!'
     */

    String text = null;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        if (scriptEntry.getArguments().size() > 3) 
            throw new InvalidArgumentsException(Messages.ERROR_LOTS_OF_ARGUMENTS);

        for (String arg : scriptEntry.getArguments()) {
                text = aH.getStringFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_TEXT, aH.getStringFrom(arg));
            }

        if (text == null) throw new InvalidArgumentsException(Messages.ERROR_NO_TEXT);
    }

    @Override
    public void execute(String commandName) throws CommandExecutionException {
        denizen.getServer().broadcastMessage(text);
    }

}
