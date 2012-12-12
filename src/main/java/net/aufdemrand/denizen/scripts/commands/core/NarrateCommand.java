package net.aufdemrand.denizen.scripts.commands.core;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.Debugger.Messages;

/**
 * Sends a message to the Player.
 * 
 * @author Jeremy Schroeder
 * Version 1.0 Last Updated 11/29 1:11
 */

public class NarrateCommand extends AbstractCommand {

    @Override
    public void onEnable() {
        // Nothing to do here.
    }

    /* 
     * Arguments: [] - Required, () - Optional 
     * ['Text to announce'] sets the text.
     * (PLAYER:player_name)
     * 
     * Example Usage:
     * NARRATE 'Hello, world!'
     * NARRATE PLAYER:<NPC.OWNER> 'ALERT! Intruder! intruder! <PLAYER.NAME> has infiltrated your base!'
     */


    String text = null;
    Player player = null;

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        player = scriptEntry.getPlayer();
        
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
        player.sendMessage(text);
    }

}