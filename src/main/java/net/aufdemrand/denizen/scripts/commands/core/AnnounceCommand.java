package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

/**
 * <p>Announces a message to the server.</p>
 * 
 * 
 * <br><b>dScript Usage:</b><br>
 * <pre>ANNOUNCE ['message to announce']</pre>
 * 
 * <ol><tt>Arguments: [] - Required</ol></tt>
 * 
 * <ol><tt>['message to announce']</tt><br> 
 *         The message to send to the server. This will be seen by all Players.</ol>
 * 
 * 
 * <br><b>Example Usage:</b><br>
 * <ol><tt>
 *  - ANNOUNCE 'Today is Christmas!' <br>
 *  - ANNOUNCE "&#60;PLAYER.NAME> has completed '&#60;FLAG.P:currentQuest>'!" <br>
 *  - ANNOUNCE "&#60;GOLD>$$$ &#60;WHITE>- Make some quick cash at our &#60;RED>MINEA-SINO&#60;WHITE>!" 
 * </ol></tt>

 * @author Jeremy Schroeder
 * 
 */
public class AnnounceCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

    	// Initialize fields
    	String text = null;
    	
    	// Users tend to forget quotes sometimes on commands like this, so
    	// let's check if there are more argument than usual.
        if (scriptEntry.getArguments().size() > 3) 
            throw new InvalidArgumentsException(Messages.ERROR_LOTS_OF_ARGUMENTS);

        // Should only be one argument, since PlAYER: and NPCID: are handled
        // internally. Let's get that argument and set it as the text.
        for (String arg : scriptEntry.getArguments()) {
                text = aH.getStringFrom(arg);
                dB.echoDebug(Messages.DEBUG_SET_TEXT, aH.getStringFrom(arg));
            }

        // If text is missing, alert the console.
        if (text == null) throw new InvalidArgumentsException(Messages.ERROR_NO_TEXT);
    
        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("text", text);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        
    	// Use Bukkit to broadcast the message to everybody in the server.
    	denizen.getServer().broadcastMessage((String) scriptEntry.getObject("text"));
    }

}
