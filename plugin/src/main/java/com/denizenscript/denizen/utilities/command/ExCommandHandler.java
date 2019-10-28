package com.denizenscript.denizen.utilities.command;

import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.utilities.FormattedTextHelper;
import com.denizenscript.denizen.utilities.Settings;
import com.denizenscript.denizen.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.depends.Depends;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizencore.DenizenCore;
import com.denizenscript.denizencore.objects.ArgumentHelper;
import com.denizenscript.denizencore.objects.ObjectFetcher;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.properties.PropertyParser;
import com.denizenscript.denizencore.scripts.ScriptBuilder;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.commands.AbstractCommand;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ExCommandHandler implements CommandExecutor, TabCompleter {

    public void enableFor(PluginCommand command) {
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String cmdName, String[] args) {

        // <--[language]
        // @name /ex command
        // @group Console Commands
        // @description
        // The '/ex' command is an easy way to run a single denizen script command in-game. Its syntax,
        // aside from '/ex' is exactly the same as any other command. When running a command, some context
        // is also supplied, such as '<player>' if being run by a player (versus the console), as well as
        // '<npc>' if a NPC is selected by using the '/npc sel' command.
        //
        // By default, ex command debug output is sent to the player that ran the ex command (if the command was ran by a player).
        // To avoid this, use '-q' at the start of the ex command.
        // Like: /ex -q narrate "wow no output"
        //
        // Examples:
        // /ex flag <player> test_flag:!
        // /ex run 's@npc walk script' as:<npc>
        //
        // Need to '/ex' a command as a different player or NPC? No problem. Just use the 'npc' and 'player'
        // value arguments, or utilize the object fetcher.
        //
        // Examples:
        // /ex narrate player:p@NLBlackEagle 'Your health is <player.health.formatted>.'
        // /ex walk npc:n@fred <player.location.cursor_on>
        //
        // -->

        if (cmdName.equalsIgnoreCase("ex")) {
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
                if (Debug.showDebug) {
                    sender.sendMessage(ChatColor.YELLOW + "Executing Denizen script command... check the console for full debug output!");
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
                    player.spigot().sendMessage(FormattedTextHelper.parse(s));
                };
            }
            queue.start();
            return true;
        }
        return false;
    }

    public HashSet<String> allTagsEver = new HashSet<>();

    public void processTagList() {
        allTagsEver.clear();
        for (ObjectFetcher.ObjectType<? extends ObjectTag> type : ObjectFetcher.objectsByClass.values()) {
            if (type.tagProcessor == null) {
                continue;
            }
            allTagsEver.addAll(type.tagProcessor.registeredObjectTags.keySet());
        }
        for (PropertyParser.ClassPropertiesInfo properties : PropertyParser.propertiesByClass.values()) {
            allTagsEver.addAll(properties.propertiesByTag.keySet());
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String cmdName, String[] rawArgs) {
        if (!cmdName.equalsIgnoreCase("ex") || !sender.hasPermission("denizen.ex")) {
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
                return new ArrayList<>(DenizenCore.getCommandRegistry().instances.keySet());
            }
            ArrayList<String> output = new ArrayList<>();
            String startOfName = CoreUtilities.toLowerCase(args[args.length - 1]);
            for (String command : DenizenCore.getCommandRegistry().instances.keySet()) {
                if (command.startsWith(startOfName)) {
                    output.add(command);
                }
            }
            return output;
        }
        if (!isNewArg) {
            String lastArg = rawArgs[rawArgs.length - 1];
            int tagStartIndex = lastArg.lastIndexOf('<');
            if (tagStartIndex > lastArg.lastIndexOf('>')) {
                String actualTag = lastArg.substring(tagStartIndex + 1);
                String beforeTag = lastArg.substring(0, tagStartIndex + 1);
                if (!actualTag.contains("[") && !actualTag.contains(".")) {
                    String tagText = CoreUtilities.toLowerCase(actualTag);
                    ArrayList<String> output = new ArrayList<>();
                    for (String tagBase : TagManager.properTagBases) {
                        if (tagBase.startsWith(tagText)) {
                            output.add(beforeTag + tagBase);
                        }
                    }
                    return output;
                }
                int lastDot = actualTag.lastIndexOf('.');
                if (lastDot <= 0) {
                    return new ArrayList<>();
                }
                beforeTag += actualTag.substring(0, lastDot + 1);
                String lastPart = CoreUtilities.toLowerCase(actualTag.substring(lastDot + 1));
                if (lastPart.contains("[") || lastPart.isEmpty()) {
                    return new ArrayList<>();
                }
                ArrayList<String> output = new ArrayList<>();
                for (String singleTag : allTagsEver) {
                    if (singleTag.startsWith(lastPart)) {
                        output.add(beforeTag + singleTag);
                    }
                }
                return output;
            }

        }
        AbstractCommand dcmd = DenizenCore.getCommandRegistry().get(args[0]);
        for (int i = args.length - 2; i >= 0; i--) {
            if (args[i].equals("-")) {
                dcmd = DenizenCore.getCommandRegistry().get(args[i + 1]);
            }
        }
        if (dcmd == null) {
            return null;
        }
        String lowArg = CoreUtilities.toLowerCase(rawArgs[rawArgs.length - 1]);
        ArrayList<String> output = new ArrayList<>();
        for (String flat : dcmd.getOptions().flatArgs) {
            if (flat.startsWith(lowArg)) {
                output.add(flat);
            }
        }
        for (String prefix : dcmd.getOptions().prefixes) {
            if (prefix.startsWith(lowArg)) {
                output.add(prefix + ":");
            }
        }
        return output;
    }
}
