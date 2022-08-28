package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizencore.utilities.CoreConfiguration;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.ObjectType;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.commands.core.FlagCommand;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.ObjectTagProcessor;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ExCommandHandler implements CommandExecutor, TabCompleter {

    public void enableFor(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {

        // <--[language]
        // @name /ex command
        // @group Console Commands
        // @description
        // The '/ex' command is an easy way to run a single denizen script command in-game.
        // 'Ex' is short for 'Execute'.
        // Its syntax, aside from '/ex' is exactly the same as any other Denizen script command.
        // When running a command, some context is also supplied, such as '<player>' if being run by a player (versus the console),
        // as well as '<npc>' if a NPC is selected by using the '/npc sel' command.
        //
        // By default, ex command debug output is sent to the player that ran the ex command (if the command was ran by a player).
        // To avoid this, use '-q' at the start of the ex command.
        // Like: /ex -q narrate "wow no output"
        //
        // The '/ex' command creates a new queue each time it's run,
        // meaning for example '/ex define' would do nothing, as the definition will be lost immediately.
        //
        // If you need to sustain a queue between multiple executions, use '/exs' ("Execute Sustained").
        // A sustained queue will use the same queue on every execution until the queue stops (normally due to '/exs stop').
        // Be warned that waits will block the sustained queue - eg '/exs wait 10m' will make '/exs' effectively unusable for 10 minutes.
        //
        // Examples:
        // /ex flag <player> test_flag:!
        // /ex run npc_walk_script
        //
        // Need to '/ex' a command as a different player or NPC? Use <@link language The Player and NPC Arguments>.
        //
        // Examples:
        // /ex narrate player:<[aplayer]> 'Your health is <player.health.formatted>.'
        // /ex walk npc:<[some_npc]> <player.cursor_on>
        //
        // -->

        if (cmd.getName().equalsIgnoreCase("ex")) {
            List<Object> entries = new ArrayList<>();
            String entry = String.join(" ", args);
            boolean quiet = !Settings.showExDebug();
            if (entry.length() > 3 && entry.startsWith("-q ")) {
                quiet = !quiet;
                entry = entry.substring("-q ".length());
            }
            if (entry.length() < 2) {
                sender.sendMessage("/ex (-q) <denizen script command> (arguments)");
                return true;
            }
            if (Settings.showExHelp()) {
                if (CoreConfiguration.shouldShowDebug) {
                    if (quiet) {
                        sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command... check the console for full debug output!");
                    }
                    else {
                        //sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command...");
                    }
                }
                else {
                    sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command... to see debug, use /denizen debug");
                }
            }
            entries.add(entry);
            InstantQueue queue = new InstantQueue("EXCOMMAND");
            NPCTag npc = null;
            if (Depends.citizens != null && Depends.citizens.getNPCSelector().getSelected(sender) != null) {
                npc = new NPCTag(Depends.citizens.getNPCSelector().getSelected(sender));
            }
            List<ScriptEntry> scriptEntries = ScriptBuilder.buildScriptEntries(entries, null,
                    new BukkitScriptEntryData(sender instanceof Player ? new PlayerTag((Player) sender) : null, npc));
            queue.addEntries(scriptEntries);
            if (!quiet && sender instanceof Player) {
                final Player player = (Player) sender;
                queue.debugOutput = (s) -> {
                    player.spigot().sendMessage(FormattedTextHelper.parse(s.replace("<FORCE_ALIGN>", ""), net.md_5.bungee.api.ChatColor.WHITE));
                };
            }
            queue.start();
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String cmdName, String[] rawArgs) {
        if ((!cmdName.equalsIgnoreCase("ex") && !cmdName.equalsIgnoreCase("exs")) || !sender.hasPermission("denizen.ex")) {
            return null;
        }
        String entry = String.join(" ", rawArgs);
        if (entry.length() > 3 && entry.startsWith("-q ")) {
            entry = entry.substring("-q ".length());
        }
        String[] args = ArgumentHelper.buildArgs(entry);
        boolean isNewArg = rawArgs.length == 0 || rawArgs[rawArgs.length - 1].isEmpty();
        boolean isCommandArg = args.length == 0 || (args.length == 1 && !isNewArg) || args[args.length - (isNewArg ? 1 : 2)].equals("-");
        if (isCommandArg) {
            if (isNewArg || args.length == 0) {
                return new ArrayList<>(DenizenCore.commandRegistry.instances.keySet());
            }
            ArrayList<String> output = new ArrayList<>();
            String startOfName = CoreUtilities.toLowerCase(args[args.length - 1]);
            for (String command : DenizenCore.commandRegistry.instances.keySet()) {
                if (command.startsWith(startOfName)) {
                    output.add(command);
                }
            }
            return output;
        }
        if (!isNewArg) {
            String lastArg = rawArgs[rawArgs.length - 1];
            int argStart = 0;
            for (int i = 0; i < lastArg.length(); i++) {
                if (lastArg.charAt(i) == '"' || lastArg.charAt(i) == '\'') {
                    char quote = lastArg.charAt(i++);
                    while (i < lastArg.length() && lastArg.charAt(i) != quote) {
                        i++;
                    }
                }
                else if (lastArg.charAt(i) == ' ') {
                    argStart = i + 1;
                }
            }
            String arg = lastArg.substring(argStart);
            if (CoreUtilities.contains(arg, '<')) {
                int tagBits = 0;
                int relevantTagStart = -1;
                for (int i = arg.length() - 1; i >= 0; i--) {
                    if (arg.charAt(i) == '>') {
                        tagBits++;
                    }
                    else if (arg.charAt(i) == '<') {
                        if (tagBits == 0) {
                            relevantTagStart = i + 1;
                            break;
                        }
                        tagBits--;
                    }
                }
                if (relevantTagStart != -1) {
                    String fullTag = CoreUtilities.toLowerCase(arg.substring(relevantTagStart));
                    int components = 0;
                    int subTags = 0;
                    int squareBrackets = 0;
                    int lastDot = 0;
                    int bracketStart = -1;
                    Collection<Class<? extends ObjectTag>> typesApplicable = null;
                    for (int i = 0; i < fullTag.length(); i++) {
                        char c = fullTag.charAt(i);
                        if (c == '<') {
                            subTags++;
                        }
                        else if (c == '>') {
                            subTags--;
                        }
                        else if (c == '[' && subTags == 0) {
                            squareBrackets++;
                            bracketStart = i;
                        }
                        else if (c == ']' && subTags == 0) {
                            squareBrackets--;
                        }
                        else if (c == '.' && subTags == 0 && squareBrackets == 0) {
                            Class<? extends ObjectTag> type = null;
                            String part = fullTag.substring(lastDot, bracketStart == -1 ? i : bracketStart);
                            if (components == 0) {
                                TagManager.TagBaseData baseType = TagManager.baseTags.get(part);
                                if (baseType != null) {
                                    type = baseType.returnType;
                                }
                            }
                            else if (typesApplicable != null) {
                                for (Class<? extends ObjectTag> possibleType : typesApplicable) {
                                    ObjectType<? extends ObjectTag> typeData = ObjectFetcher.getType(possibleType);
                                    if (typeData != null && typeData.tagProcessor != null) {
                                        ObjectTagProcessor.TagData data = typeData.tagProcessor.registeredObjectTags.get(part);
                                        if (data != null && data.returnType != null) {
                                            type = data.returnType;
                                            break;
                                        }
                                    }
                                }
                            }
                            if (type != null) {
                                typesApplicable = ObjectFetcher.getAllApplicableSubTypesFor(type);
                            }
                            else {
                                typesApplicable = ObjectFetcher.objectsByClass.keySet();
                            }
                            components++;
                            lastDot = i + 1;
                            bracketStart = -1;
                        }
                    }
                    String beforeDot = arg.substring(0, relevantTagStart) + fullTag.substring(0, lastDot);
                    if (components == 0 && !CoreUtilities.contains(fullTag, '[')) {
                        ArrayList<String> output = new ArrayList<>();
                        for (String tagBase : TagManager.baseTags.keySet()) {
                            if (tagBase.startsWith(fullTag)) {
                                output.add(beforeDot + tagBase);
                            }
                        }
                        return output;
                    }
                    String subComponent = fullTag.substring(lastDot);
                    if (lastDot > 0 && !CoreUtilities.contains(subComponent, '[')) {
                        ArrayList<String> output = new ArrayList<>();
                        for (Class<? extends ObjectTag> possibleType : typesApplicable) {
                            ObjectType<? extends ObjectTag> typeData = ObjectFetcher.getType(possibleType);
                            if (typeData != null && typeData.tagProcessor != null) {
                                for (String tag : typeData.tagProcessor.registeredObjectTags.keySet()) {
                                    if (tag.startsWith(subComponent)) {
                                        output.add(beforeDot + tag);
                                    }
                                }
                            }
                        }
                        return output;
                    }
                }
            }
        }
        AbstractCommand dcmd = DenizenCore.commandRegistry.get(args[0]);
        for (int i = args.length - 2; i >= 0; i--) {
            if (args[i].equals("-")) {
                dcmd = DenizenCore.commandRegistry.get(args[i + 1]);
            }
        }
        if (dcmd == null) {
            return null;
        }
        String lowArg = CoreUtilities.toLowerCase(rawArgs[rawArgs.length - 1]);
        AbstractCommand.TabCompletionsBuilder completionsBuilder = new AbstractCommand.TabCompletionsBuilder();
        completionsBuilder.arg = lowArg;
        for (String flat : dcmd.docFlagArgs) {
            completionsBuilder.add(flat);
        }
        for (String prefix : dcmd.docPrefixes) {
            completionsBuilder.add(prefix + ":");
        }
        dcmd.addCustomTabCompletions(completionsBuilder);
        if (dcmd instanceof FlagCommand) {
            if (sender instanceof Player) {
                for (String flagName : new PlayerTag((Player) sender).getFlagTracker().listAllFlags()) {
                    if (!flagName.startsWith("__")) {
                        completionsBuilder.add(flagName);
                    }
                }
            }
            if (Depends.citizens != null && Depends.citizens.getNPCSelector().getSelected(sender) != null) {
                for (String flagName : new NPCTag(Depends.citizens.getNPCSelector().getSelected(sender)).getFlagTracker().listAllFlags()) {
                    if (!flagName.startsWith("__")) {
                        completionsBuilder.add(flagName);
                    }
                }
            }
        }
        return completionsBuilder.completions;
    }
}
