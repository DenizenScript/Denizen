package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.flags.FlagManager;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.FormatScriptContainer;
import net.aufdemrand.denizen.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * <p>Announces a message to the server.</p>
 *
 * <b>dScript Usage:</b><br>
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
 *
 * @author Jeremy Schroeder
 *
 */
public class AnnounceCommand extends AbstractCommand {

    enum AnnounceType { ALL, TO_OPS, TO_FLAGGED }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        // Initialize fields
        String text = null;
        AnnounceType announceType = AnnounceType.ALL;
        FormatScriptContainer format = null;
        String flag = null;

        // Users tend to forget quotes sometimes on commands like this, so
        // let's check if there are more argument than usual.
        if (scriptEntry.getArguments().size() > 3)
            throw new InvalidArgumentsException(Messages.ERROR_LOTS_OF_ARGUMENTS);

        // Should only be one argument, since PlAYER: and NPCID: are handled
        // internally. Let's get that argument and set it as the text.
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("TO_OPS", arg)) {
                announceType = AnnounceType.TO_OPS;

            } else if (aH.matchesArg("FORMAT", arg)) {
                String formatStr = aH.getStringFrom(arg);
                format = ScriptRegistry.getScriptContainerAs(formatStr, FormatScriptContainer.class);

                if(format == null) dB.echoError("Invalid format: " + formatStr);

            } else if (aH.matchesValueArg("TO_FLAGGED", arg, aH.ArgumentType.Custom)) {
                flag = aH.getStringFrom(arg);
                announceType = AnnounceType.TO_FLAGGED;
            }

            else text = aH.getStringFrom(arg);
        }

        // If text is missing, alert the console.
        if (text == null)
            throw new InvalidArgumentsException(Messages.ERROR_NO_TEXT);

        // Add objects that need to be passed to execute() to the scriptEntry
        scriptEntry.addObject("text", text)
                .addObject("type", announceType)
                .addObject("format", format)
                .addObject("flag", flag);

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Fetch objects
        String text = (String) scriptEntry.getObject("text");
        AnnounceType type = (AnnounceType) scriptEntry.getObject("type");
        FormatScriptContainer format = (FormatScriptContainer) scriptEntry.getObject("format");
        String flag = (String) scriptEntry.getObject("flag");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Message", text)
                        + (format != null ? aH.debugObj("Format", format.getName()) : "")
                        + aH.debugObj("Type", type.name())
                        + (flag != null? aH.debugObj("Flag_Name", flag) : ""));

        String message = format != null ? format.getFormattedText(scriptEntry) : text;

        // Use Bukkit to broadcast the message to everybody in the server.
        if (type == AnnounceType.ALL) {
            denizen.getServer().broadcastMessage(message);
        }

        else if (type == AnnounceType.TO_OPS) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.isOp()) player.sendMessage(message);
            }
        }

        else if (type == AnnounceType.TO_FLAGGED) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (FlagManager.playerHasFlag(player, flag))
                    player.sendMessage(message);
            }
        }
    }

}


