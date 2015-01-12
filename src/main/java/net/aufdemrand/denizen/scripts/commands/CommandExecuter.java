package net.aufdemrand.denizen.scripts.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.Denizen;
import net.aufdemrand.denizen.tags.BukkitTagContext;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.objects.dNPC;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.DebugElement;

import org.bukkit.ChatColor;

public class CommandExecuter {

    private final Denizen plugin;
    private static final Pattern definition_pattern = Pattern.compile("%(.+?)%");


    public CommandExecuter(Denizen denizen) {
        plugin = denizen;
    }

    /*
     * Executes a command defined in scriptEntry
     */

    public boolean execute(ScriptEntry scriptEntry) {
        StringBuilder output = new StringBuilder();
        output.append(scriptEntry.getCommandName());
        if (scriptEntry.getOriginalArguments() == null) {
            dB.echoError("Original Arguments null for " + scriptEntry.getCommandName());
        }
        else {
            for (String arg: scriptEntry.getOriginalArguments())
                output.append(" \"").append(arg).append("\"");
        }

        dB.echoDebug(scriptEntry, "Queue '" + scriptEntry.getResidingQueue().id + "' Executing: " + output.toString());

        Matcher m;
        StringBuffer sb;
        if (scriptEntry.getCommandName().indexOf('%') != -1) {
            m = definition_pattern.matcher(scriptEntry.getCommandName());
            sb = new StringBuffer();
            while (m.find()) {
                String definition = scriptEntry.getResidingQueue().getDefinition(m.group(1));
                if (definition == null) {
                    dB.echoError("Unknown definition %" + m.group(1) + "%.");
                    definition = "null";
                }
                dB.echoDebug(scriptEntry, "Filled definition %" + m.group(1) + "% with '" + definition + "'.");
                m.appendReplacement(sb, Matcher.quoteReplacement(definition));
            }
            m.appendTail(sb);
            scriptEntry.setCommandName(sb.toString());
        }

        // Get the command instance ready for the execution of the scriptEntry
        AbstractCommand command = scriptEntry.getCommand();
        if (command == null) {
            command = (AbstractCommand)DenizenAPI.getCurrentInstance().getCommandRegistry().get(scriptEntry.getCommandName());
        }

        if (command == null) {
            dB.echoDebug(scriptEntry, DebugElement.Header, "Executing command: " + scriptEntry.getCommandName());
            dB.echoError(scriptEntry.getResidingQueue(), scriptEntry.getCommandName() + " is an invalid dCommand! Are you sure it loaded?");
            dB.echoDebug(scriptEntry, DebugElement.Footer);
            return false;
        }

        if (((BukkitScriptEntryData)scriptEntry.entryData).hasNPC() && ((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getCitizen() == null)
            ((BukkitScriptEntryData)scriptEntry.entryData).setNPC(null);

        // Debugger information
        if (scriptEntry.getOriginalArguments() == null ||
                scriptEntry.getOriginalArguments().size() == 0 ||
                !scriptEntry.getOriginalArguments().get(0).equals("\0CALLBACK")) {
            if (((BukkitScriptEntryData)scriptEntry.entryData).getPlayer() != null)
                dB.echoDebug(scriptEntry, DebugElement.Header, "Executing dCommand: " + scriptEntry.getCommandName() + "/p@" + ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().getName());
            else
                dB.echoDebug(scriptEntry, DebugElement.Header, "Executing dCommand: " +
                    scriptEntry.getCommandName() + (((BukkitScriptEntryData)scriptEntry.entryData).getNPC() != null ? "/n@" + ((BukkitScriptEntryData)scriptEntry.entryData).getNPC().getName() : ""));
        }

        // Don't execute() if problems arise in parseArgs()
        boolean keepGoing = true;

        try {

            // Throw exception if arguments are required for this command, but not supplied.
            if (command.getOptions().REQUIRED_ARGS > scriptEntry.getArguments().size()) throw new InvalidArgumentsException("");

            if (scriptEntry.has_tags) {
                scriptEntry.setArguments(TagManager.fillArguments(scriptEntry.getArguments(),
                        new BukkitTagContext(scriptEntry != null ? ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer(): null,
                                scriptEntry != null ? ((BukkitScriptEntryData)scriptEntry.entryData).getNPC(): null,
                                true, scriptEntry, scriptEntry != null ? scriptEntry.shouldDebug(): true,
                                scriptEntry != null ? scriptEntry.getScript(): null
                                ))); // Replace tags
            }

            /*  If using NPC:# or PLAYER:Name arguments, these need to be changed out immediately because...
             *  1) Denizen/Player flags need the desired NPC/PLAYER before parseArgs's getFilledArguments() so that
             *     the Player/Denizen flags will read from the correct Object. If using PLAYER or NPCID arguments,
             *     the desired Objects are obviously not the same objects that were sent with the ScriptEntry.
             *  2) These arguments should be valid for EVERY ScriptCommand, so why not just take care of it
             *     here, instead of requiring each command to take care of the argument.
             */

            List<String> newArgs = new ArrayList<String>();

            // Don't fill in tags if there were brackets detected..
            // This means we're probably in a nested if.
            int nested_depth = 0;
            // Watch for IF command to avoid filling player and npc arguments
            // prematurely
            boolean if_ignore = false;

            for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
                if (arg.getValue().equals("{")) nested_depth++;
                if (arg.getValue().equals("}")) nested_depth--;

                // If nested, continue.
                if (nested_depth > 0) {
                    newArgs.add(arg.raw_value);
                    continue;
                }

                if (arg.raw_value.indexOf('%') != -1) {
                    m = definition_pattern.matcher(arg.raw_value);
                    sb = new StringBuffer();
                    while (m.find()) {
                        String def = m.group(1);
                        boolean dynamic = false;
                        if (def.startsWith("|")) {
                            def = def.substring(1, def.length() - 1);
                            dynamic = true;
                        }
                        String definition;
                        String defval = scriptEntry.getResidingQueue().getDefinition(def);
                        if (dynamic)
                            definition = scriptEntry.getResidingQueue().getDefinition(def);
                        else
                            definition = TagManager.escapeOutput(scriptEntry.getResidingQueue().getDefinition(def));
                        if (defval == null) {
                            dB.echoError("Unknown definition %" + m.group(1) + "%.");
                            definition = "null";
                        }
                        dB.echoDebug(scriptEntry, "Filled definition %" + m.group(1) + "% with '" + definition + "'.");
                        m.appendReplacement(sb, Matcher.quoteReplacement(definition));
                    }
                    m.appendTail(sb);
                    arg = aH.Argument.valueOf(sb.toString());
                }

                // If using IF, check if we've reached the command + args
                // so that we don't fill player: or npc: prematurely
                if (command.getName().equalsIgnoreCase("if")
                        && DenizenAPI.getCurrentInstance().getCommandRegistry().get(arg.getValue()) != null)
                    if_ignore = true;

                // Fill player/off-line player
                if (arg.matchesPrefix("player") && !if_ignore) {
                    dB.echoDebug(scriptEntry, "...replacing the linked player with " + arg.getValue());
                    String value = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
                    dPlayer player = dPlayer.valueOf(value);
                    if (player == null || !player.isValid()) {
                        dB.echoError(scriptEntry.getResidingQueue(), value + " is an invalid player!");
                        return false;
                    }
                    ((BukkitScriptEntryData)scriptEntry.entryData).setPlayer(player);
                }

                // Fill NPCID/NPC argument
                else if (arg.matchesPrefix("npc, npcid") && !if_ignore) {
                    dB.echoDebug(scriptEntry, "...replacing the linked NPC with " + arg.getValue());
                    String value = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
                    dNPC npc = dNPC.valueOf(value);
                    if (npc == null || !npc.isValid()) {
                        dB.echoError(scriptEntry.getResidingQueue(), value + " is an invalid NPC!");
                        return false;
                    }
                    ((BukkitScriptEntryData)scriptEntry.entryData).setNPC(npc);
                }

                // Save the scriptentry if needed later for fetching scriptentry context
                else if (arg.matchesPrefix("save") && !if_ignore) {
                    String saveName = TagManager.tag(arg.getValue(), new BukkitTagContext(scriptEntry, false));
                    dB.echoDebug(scriptEntry, "...remembering this script entry as '" + saveName + "'!");
                    scriptEntry.getResidingQueue().holdScriptEntry(saveName, scriptEntry);
                }

                else newArgs.add(arg.raw_value);
            }

            // Add the arguments back to the scriptEntry.
            scriptEntry.setArguments(newArgs);

            // Now process non-instant tags.
            scriptEntry.setArguments(TagManager.fillArguments(scriptEntry.getArguments(),
                    new BukkitTagContext(scriptEntry != null ? ((BukkitScriptEntryData)scriptEntry.entryData).getPlayer(): null,
                            scriptEntry != null ? ((BukkitScriptEntryData)scriptEntry.entryData).getNPC(): null,
                            false, scriptEntry, scriptEntry != null ? scriptEntry.shouldDebug(): true,
                            scriptEntry != null ? scriptEntry.getScript(): null
                    ))); // Replace tags

            // Parse the rest of the arguments for execution.
            command.parseArgs(scriptEntry);

        } catch (InvalidArgumentsException e) {

            keepGoing = false;
            // Give usage hint if InvalidArgumentsException was called.
            dB.echoError(scriptEntry.getResidingQueue(), "Woah! Invalid arguments were specified!");
            if (e.getMessage() != null && e.getMessage().length() > 0)
                dB.log(ChatColor.YELLOW + "+> MESSAGE follows: " + ChatColor.WHITE + "'" + e.getMessage() + "'");
            dB.log("Usage: " + command.getUsageHint());
            dB.echoDebug(scriptEntry, DebugElement.Footer);
            scriptEntry.setFinished(true);

        } catch (Exception e) {

            keepGoing = false;
            dB.echoError(scriptEntry.getResidingQueue(), "Woah! An exception has been called with this command!");
            dB.echoError(scriptEntry.getResidingQueue(), e);
            dB.echoDebug(scriptEntry, DebugElement.Footer);
            scriptEntry.setFinished(true);

        } finally {

            if (keepGoing)
                try {
                    // Run the execute method in the command
                    command.execute(scriptEntry);
                } catch (Exception e) {
                    dB.echoError(scriptEntry.getResidingQueue(), "Woah!! An exception has been called with this command!");
                    dB.echoError(scriptEntry.getResidingQueue(), e);
                    scriptEntry.setFinished(true);
                }

        }

        return true;
    }
}
