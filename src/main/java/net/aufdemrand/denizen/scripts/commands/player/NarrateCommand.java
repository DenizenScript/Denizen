package net.aufdemrand.denizen.scripts.commands.player;

import org.bukkit.entity.Player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

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
     * (FORMAT:format)
     * 
     * Example Usage:
     * NARRATE 'Hello, world!'
     * NARRATE PLAYER:<NPC.OWNER> 'ALERT! Intruder! intruder! <PLAYER.NAME> has infiltrated your base!'
     */

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        String text = null;
        FormatScriptContainer format = null;

        if (scriptEntry.getArguments().size() > 3) 
            throw new InvalidArgumentsException(Messages.ERROR_LOTS_OF_ARGUMENTS);

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesValueArg("FORMAT", arg, aH.ArgumentType.String)) {
                String formatStr = aH.getStringFrom(arg);
                format = ScriptRegistry.getScriptContainerAs(formatStr, FormatScriptContainer.class);
                
                if(format != null) dB.echoDebug("... format set to: " + formatStr);
                else dB.echoError("... could not find format for: " + formatStr);
                
            } else {
                text = aH.getStringFrom(arg);
            }
        }
        
        if (scriptEntry.getPlayer() == null) throw new InvalidArgumentsException(Messages.ERROR_NO_PLAYER);
        if (text == null) throw new InvalidArgumentsException(Messages.ERROR_NO_TEXT);

        scriptEntry.addObject("text", text)
            .addObject("format", format);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Player player = scriptEntry.getPlayer().getPlayerEntity();
        String text = (String) scriptEntry.getObject("text");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Player", scriptEntry.getPlayer().getName())
                        + (format != null ? aH.debugObj("Format", format.getName()) : "")
                        + aH.debugObj("Text", text));

        player.sendMessage(format != null ? format.getFormattedText(scriptEntry) : text);
    }

}