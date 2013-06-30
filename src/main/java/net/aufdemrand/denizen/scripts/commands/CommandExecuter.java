package net.aufdemrand.denizen.scripts.commands;

import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.events.ScriptEntryExecuteEvent;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;
import net.citizensnpcs.api.CitizensAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandExecuter {

    private Denizen plugin;
    final Pattern definition_pattern = Pattern.compile("%(.+?)%");


    public CommandExecuter(Denizen denizen) {
        plugin = denizen;
    }

	/*
	 * Executes a command defined in scriptEntry 
	 */

    public boolean execute(ScriptEntry scriptEntry) {
        if (plugin.getCommandRegistry().get(scriptEntry.getCommandName()) == null) {
            dB.echoDebug(DebugElement.Header, "Executing command: " + scriptEntry.getCommandName());
            dB.echoError(scriptEntry.getCommandName() + " is an invalid dCommand! Are you sure it loaded?");
            dB.echoDebug(DebugElement.Footer);
            return false;
        }

        // Get the command instance ready for the execution of the scriptEntry
        AbstractCommand command = plugin.getCommandRegistry().get(scriptEntry.getCommandName());

        // Debugger information
        if (scriptEntry.getPlayer() != null)
            dB.echoDebug(DebugElement.Header, "Executing dCommand: " + scriptEntry.getCommandName() + "/" + scriptEntry.getPlayer().getName());
        else dB.echoDebug(DebugElement.Header, "Executing dCommand: " + scriptEntry.getCommandName() + (scriptEntry.getNPC() != null ? "/" + scriptEntry.getNPC().getName() : ""));

        // Don't execute() if problems arise in parseArgs()
        boolean keepGoing = true;

        try {

            // Throw exception if arguments are required for this command, but not supplied.
            if (command.getOptions().REQUIRED_ARGS > scriptEntry.getArguments().size()) throw new InvalidArgumentsException("");

            if (scriptEntry.has_tags)
                scriptEntry.setArguments(TagManager.fillArguments(scriptEntry.getArguments(), scriptEntry, true)); // Replace tags

			/*  If using NPCID:# or PLAYER:Name arguments, these need to be changed out immediately because...
			 *  1) Denizen/Player flags need the desired NPC/PLAYER before parseArgs's getFilledArguments() so that
			 *     the Player/Denizen flags will read from the correct Object. If using PLAYER or NPCID arguments,
			 *     the desired Objects are obviously not the same objects that were sent with the ScriptEntry.
			 *  2) These arguments should be valid for EVERY ScriptCommand, so why not just take care of it
			 *     here, instead of requiring each command to take care of the argument.
			 */

            List<String> newArgs = new ArrayList<String>();

            for (String arg : scriptEntry.getArguments()) {

                Matcher m = definition_pattern.matcher(arg);
                StringBuffer sb = new StringBuffer();
                dB.log(scriptEntry.getResidingQueue().context.toString());
                while (m.find()) {
                    if (scriptEntry.getResidingQueue().context
                            .containsKey(m.group(1).toLowerCase()))
                        m.appendReplacement(sb,
                                scriptEntry.getResidingQueue().context.get(m.group(1)));

                    else m.appendReplacement(sb, "null");
                }
                m.appendTail(sb);
                arg = sb.toString();

                String[] split = arg.split(":");

                // Fill player/off-line player
                if (aH.matchesValueArg("player", arg, aH.ArgumentType.String))
                    scriptEntry.setPlayer(dPlayer.valueOf(split[1]));

                    // Fill NPCID/NPC argument
                else if (aH.matchesValueArg("npcid, npc", arg, aH.ArgumentType.String)) {
                    dB.echoDebug("...replacing the linked NPC.");
                    if (dNPC.matches(aH.getStringFrom(arg)))
                        scriptEntry.setNPC(dNPC.valueOf(aH.getStringFrom(arg)));
                }

                else newArgs.add(arg);
            }

            // Add the arguments back to the scriptEntry.
            scriptEntry.setArguments(newArgs);

            // Now process non-instant tags.
            if (scriptEntry.has_tags)
                scriptEntry.setArguments(TagManager.fillArguments(scriptEntry.getArguments(), scriptEntry, false));

            // dBug the filled arguments
            dB.echoDebug(ChatColor.AQUA + "+> " + ChatColor.DARK_GRAY + "Filled tags: " + scriptEntry.getArguments().toString());

            // Parse the rest of the arguments for execution.
            command.parseArgs(scriptEntry);

        }	catch (InvalidArgumentsException e) {

            keepGoing = false;
            // Give usage hint if InvalidArgumentsException was called.
            dB.echoError("Woah! Invalid arguments were specified!");
            dB.echoDebug(ChatColor.YELLOW + "+> MESSAGE follows: " + ChatColor.WHITE + "'" + e.getMessage() + "'");
            dB.echoDebug("Usage: " + command.getUsageHint());
            dB.echoDebug(DebugElement.Footer);

        } catch (Exception e) {

            keepGoing = false;
            dB.echoError("Woah! An exception has been called with this command!");
            if (!dB.showStackTraces)
                dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
            else e.printStackTrace();
            dB.echoDebug(DebugElement.Footer);

        } finally {

            if (keepGoing)
                try {
                    // Fire event for last minute cancellation/alterations
                    ScriptEntryExecuteEvent event = new ScriptEntryExecuteEvent(scriptEntry);
                    Bukkit.getServer().getPluginManager().callEvent(event);

                    // If event is altered, update the scriptEntry.
                    if (event.isAltered()) scriptEntry = event.getScriptEntry();

                    // Run the execute method in the command
                    if (!event.isCancelled()) command.execute(scriptEntry);

                    else dB.echoDebug("ScriptEntry has been cancelled.");
                } catch (Exception e) {
                    dB.echoError("Woah!! An exception has been called with this command!");
                    if (!dB.showStackTraces)
                        dB.echoError("Enable '/denizen stacktrace' for the nitty-gritty.");
                    else e.printStackTrace();
                }
        }

        return true;
    }

}