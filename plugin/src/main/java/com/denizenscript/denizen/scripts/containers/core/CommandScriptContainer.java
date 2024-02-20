package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizencore.utilities.debugging.Debug;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ContextSource;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;
import com.denizenscript.denizencore.utilities.text.StringHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Command Script Containers
    // @group Script Container System
    // @description
    // Command script containers allow you to register your own custom commands to the server.
    // This also allows the command to show up in the '/help' command, with some info on the command.
    //
    // Note that existing names or aliases from other plugins will be overridden.
    // If you want to run a script at the same time as an existing command, see <@link event on command>.
    //
    // The following is the format for the container.
    //
    // The required keys are 'name:', 'description:', 'usage:', and 'script:'
    // All other keys can be excluded if unneeded.
    // If you are not intentionally setting a specific value for the other keys, it is
    // strongly recommended that you simply not include them at all.
    //
    // Please note that 'name:' is the true name of the command (written by users),
    // and 'usage:' is for documentation in the '/help' command.
    // These two options should almost always show the same name.
    //
    //
    // Command scripts can be automatically disabled by adding "enabled: false" as a root key (supports any load-time-parseable tags).
    //
    // <code>
    // # The name of the script doesn't matter, and will not affect the command in any way.
    // Command_Script_Name:
    //
    //     type: command
    //
    //     # The name of the command. This will be the default method for running the command, and will show in '/help'.
    //     # | All command scripts MUST have this key!
    //     name: mycmd
    //
    //     # The description of the command. This will be shown in the '/help' command.
    //     # Multiple lines are acceptable, via <&nl> (the tag for a new line), but you should
    //     # make the first line a brief summary of what your command does.
    //     # | All command scripts MUST have this key!
    //     description: My command.
    //
    //     # Correct usage for the command. This will show in the '/help' command.
    //     # This is NOT the name of the command, and it is NOT used to control input parsing. It is EXCLUSIVELY for '/help'.
    //     # | All command scripts MUST have this key!
    //     usage: /mycmd <&lt>myArg1<&gt>
    //
    //     # A list of aliases for the command. These will be used as alternative ways to trigger the command, and will show in the '/help' command.
    //     # | Some command scripts might have this key, but it's optional.
    //     aliases:
    //     - myalias
    //     - mycommand
    //
    //     # The permission node to check for permissions plugins. This will automatically
    //     # block players without the permission from accessing the command and help for
    //     # the command.
    //     # Note that you can include multiple permission nodes (a player only needs to have any one permission from the list)
    //     # by separating them with a semicolon, like: perm.one;perm.two;third.perm
    //     # | Most command scripts should have this key!
    //     permission: my.permission.node
    //
    //     # The message to send to the player when they try to use the command without
    //     # permission. If this is not specified, the default Bukkit message will be sent.
    //     # Has "permission" def available.
    //     # | Most command scripts should NOT have this key, but it's available.
    //     permission message: Sorry, <player.name>, you can't use my command because you don't have the permission '<[permission]>'!
    //
    //     # The procedure-based script that will be checked when a player or the console
    //     # is trying to view help for this command. This must always be determined true
    //     # or false. If there is no script, it's assumed that all players and the console
    //     # should be allowed to view the help for this command.
    //     # Available context: <context.server> returns whether the server is viewing the help (a player if false).
    //     # | Most command scripts should NOT have this key, but it's available.
    //     allowed help:
    //     - determine <player.has_flag[special_allowed_help_flag]||<context.server>>
    //
    //     # You can optionally specify tab completions on a per-argument basis.
    //     # Available context:
    //     # <context.args> returns a list of input arguments.
    //     # <context.raw_args> returns all the arguments as raw text.
    //     # <context.server> returns whether the server is using tab completion (a player if false).
    //     # <context.alias> returns the command alias being used.
    //     # | This key is great to have when used well, but is not required.
    //     tab completions:
    //         # This will complete "alpha" and "beta" for the first argument
    //         1: alpha|beta
    //         # This will complete any online player name for the second argument
    //         2: <server.online_players.parse[name]>
    //         # This will allow flags "-a", "-b", or "-c" to be entered in the third, fourth, or fifth argument.
    //         3 4 5: -a|-b|-c
    //         # Any argument other than the ones explicitly listed will be handled here with a tab complete that just says 'StopTyping'.
    //         default: StopTyping
    //
    //     # You can also optionally use the 'tab complete' key to build custom procedure-style tab complete logic
    //     # if the simply numeric argument basis isn't sufficient.
    //     # Has the same context available as 'tab completions'.
    //     # | Most scripts should leave this key off, though it can be useful to some.
    //     tab complete:
    //     - determine some|dynamic|logic|here
    //
    //     # The script that will run when the command is executed.
    //     # No, you do not need '- determine fulfilled' or anything of the sort, since the command is fully registered.
    //     # Available context:
    //     # <context.args> returns a list of input arguments.
    //     # <context.raw_args> returns all the arguments as raw text.
    //     # <context.source_type> returns the source of the command. Can be: PLAYER, SERVER, COMMAND_BLOCK, or COMMAND_MINECART.
    //     # <context.alias> returns the command alias being used.
    //     # <context.command_block_location> returns the command block's location (if the command was run from one).
    //     # <context.command_minecart> returns the EntityTag of the command minecart (if the command was run from one).
    //     # | All command scripts MUST have this key!
    //     script:
    //     - narrate Yay!
    //     - narrate "My command worked!"
    //     - narrate "And I typed '/<context.alias> <context.raw_args>'!"
    // </code>
    //
    // -->

    public CommandScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        CommandScriptHelper.init();
        CommandScriptHelper.commandScripts.put(getName(), this);
        if (containsScriptSection("tab complete")) {
            hasProcStyleTabComplete = true;
        }
        if (contains("tab completions", Map.class)) {
            tabCompletionTaggables = new HashMap<>();
            YamlConfiguration section = getConfigurationSection("tab completions");
            for (StringHolder key : section.getKeys(false)) {
                String val = section.getString(key.str);
                if (key.str.equals("default")) {
                    tabCompletionTaggables.put(-1, val);
                }
                else {
                    try {
                        for (String num : key.str.split(" ")) {
                            tabCompletionTaggables.put(Integer.parseInt(num), val);
                        }
                    }
                    catch (NumberFormatException ex) {
                        Debug.echoError("Invalid tab completion argument number key '" + key.str + "'.");
                    }
                }
            }
        }
    }

    public boolean hasProcStyleTabComplete = false;

    public HashMap<Integer, String> tabCompletionTaggables;

    public String getCommandName() {
        String name = getString("name", null);
        if (name == null) {
            Debug.echoError("Command script '" + getName() + "' is missing required 'name'' key!");
            return null;
        }
        return CoreUtilities.toLowerCase(name);
    }

    public String getDescription() {
        // Replace new lines with a space and a new line, to allow full brief descriptions in /help.
        // Without this, "line<n>line"s brief description would be "lin", because who doesn't like
        // random cutoff-
        return getString("description", "", true).replace("\n", " \n");
    }

    public String getUsage() {
        return getString("usage", "", true);
    }

    public List<String> getAliases() {
        List<String> aliases = getStringList("aliases", true);
        return aliases != null ? aliases : new ArrayList<>();
    }

    public String getPermission() {
        return getString("permission");
    }

    public String getPermissionMessage() {
        return getString("permission message");
    }

    public ScriptQueue runCommandScript(PlayerTag player, NPCTag npc, Map<String, ObjectTag> context) {
        ScriptQueue queue = new InstantQueue(getName());
        queue.addEntries(getBaseEntries(new BukkitScriptEntryData(player, npc)));
        if (context != null) {
            ContextSource.SimpleMap src = new ContextSource.SimpleMap();
            src.contexts = context;
            queue.setContextSource(src);
        }
        queue.start();
        return queue;
    }

    public boolean runAllowedHelpProcedure(PlayerTag player, NPCTag npc, Map<String, ObjectTag> context) {
        List<ScriptEntry> entries = getEntries(new BukkitScriptEntryData(player, npc), "allowed help");

        ScriptQueue queue = new InstantQueue(getName());
        queue.addEntries(entries);
        if (context != null) {
            ContextSource.SimpleMap src = new ContextSource.SimpleMap();
            src.contexts = context;
            queue.setContextSource(src);
        }
        queue.start();
        return queue.determinations != null && queue.determinations.size() > 0 && queue.determinations.get(0).equalsIgnoreCase("true");
    }

    public List<String> runTabCompleteProcedure(PlayerTag player, NPCTag npc, Map<String, ObjectTag> context, String[] originalArguments) {
        BukkitTagContext tagContext = new BukkitTagContext(player, npc, new ScriptTag(this));
        ContextSource contextSrc = null;
        if (context != null) {
            ContextSource.SimpleMap src = new ContextSource.SimpleMap();
            src.contexts = context;
            tagContext.contextSource = src;
            contextSrc = src;
        }
        List<String> list = new ArrayList<>();
        if (tabCompletionTaggables != null) {
            int argCount = Math.max(originalArguments.length, 1);
            String taggable = tabCompletionTaggables.get(argCount);
            if (taggable == null) {
                taggable = tabCompletionTaggables.get(-1);
            }
            if (taggable != null) {
                String argLow = originalArguments.length == 0 ? "" : CoreUtilities.toLowerCase(originalArguments[originalArguments.length - 1]);
                for (String value : ListTag.getListFor(TagManager.tagObject(taggable, tagContext), tagContext)) {
                    if (CoreUtilities.toLowerCase(value).startsWith(argLow)) {
                        list.add(value);
                    }
                }
            }
        }
        if (hasProcStyleTabComplete) {
            List<ScriptEntry> entries = getEntries(new BukkitScriptEntryData(player, npc), "tab complete");
            ScriptQueue queue = new InstantQueue(getName());
            queue.addEntries(entries);
            if (contextSrc != null) {
                queue.setContextSource(contextSrc);
            }
            queue.start();
            if (queue.determinations != null && queue.determinations.size() > 0) {
                list.addAll(ListTag.getListFor(queue.determinations.getObject(0), tagContext));
            }
        }
        return list;
    }

    public boolean hasAllowedHelpProcedure() {
        return containsScriptSection("allowed help");
    }

    public boolean hasTabCompleteProcedure() {
        return hasProcStyleTabComplete || tabCompletionTaggables != null;
    }
}
