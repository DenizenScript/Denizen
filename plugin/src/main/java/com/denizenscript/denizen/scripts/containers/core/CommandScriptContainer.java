package com.denizenscript.denizen.scripts.containers.core;

import com.denizenscript.denizen.utilities.command.scripted.DenizenCommand;
import com.denizenscript.denizen.utilities.implementation.BukkitScriptEntryData;
import com.denizenscript.denizen.objects.NPCTag;
import com.denizenscript.denizen.objects.PlayerTag;
import com.denizenscript.denizen.tags.BukkitTagContext;
import com.denizenscript.denizencore.events.OldEventManager;
import com.denizenscript.denizencore.objects.core.ListTag;
import com.denizenscript.denizencore.objects.ObjectTag;
import com.denizenscript.denizencore.objects.core.ScriptTag;
import com.denizenscript.denizencore.scripts.ScriptEntry;
import com.denizenscript.denizencore.scripts.containers.ScriptContainer;
import com.denizenscript.denizencore.scripts.queues.ScriptQueue;
import com.denizenscript.denizencore.scripts.queues.core.InstantQueue;
import com.denizenscript.denizencore.tags.TagManager;
import com.denizenscript.denizencore.utilities.CoreUtilities;
import com.denizenscript.denizencore.utilities.YamlConfiguration;

import java.util.ArrayList;
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
    // <code>
    // # The name of the script doesn't matter, and will not affect the command in any way.
    // Command_Script_Name:
    //
    //   type: command
    //
    //   # The name of the command. This will show up in the default list in the '/help' command
    //   # and will be the default method for running the command.
    //   name: mycmd
    //
    //   # The description of the command. This will be shown in the '/help' command.
    //   # Multiple lines are acceptable, via <&nl> (the tag for a new line), but you should
    //   # make the first line a brief summary of what your command does.
    //   description: My command.
    //
    //   # Correct usage for the command. This will show in the '/help' command.
    //   usage: /mycmd <&lt>myArg1<&gt>
    //
    //   # A list of aliases for the command. These will show in the '/help' command, and
    //   # are alternatives to the default name.
    //   aliases:
    //   - myalias
    //   - mycommand
    //
    //   # The permission node to check for permissions plugins. This will automatically
    //   # block players without the permission from accessing the command and help for
    //   # the command.
    //   permission: my.permission.node
    //
    //   # The message to send to the player when they try to use the command without
    //   # permission. If this is not specified, the default Bukkit message will be sent.
    //   permission message: Sorry, <player.name>, you can't use my command because you don't have the permission '<permission>'!
    //
    //   # The procedure-based script that will be checked when a player or the console
    //   # is trying to view help for this command. This must always be determined true
    //   # or false. If there is no script, it's assumed that all players and the console
    //   # should be allowed to view the help for this command.
    //   # Available context: <context.server> returns whether the server is viewing the help (a player if false).
    //   allowed help:
    //   - determine <player.is_op||<context.server>>
    //
    //   # The procedure-based script that will run when a player uses tab completion to predict words.
    //   # This should return a ListTag of words that the player can tab through, based on the arguments they have already typed.
    //   #Leaving this node out will result in using Bukkit's built-in tab completion.
    //   # Available context:
    //   # <context.args> returns a list of input arguments.
    //   # <context.raw_args> returns all the arguments as raw text.
    //   # <context.server> returns whether the server is using tab completion (a player if false).
    //   # <context.alias> returns the command alias being used.
    //   tab complete:
    //   - if !<player.is_op||<context.server>>:
    //     - stop
    //   - determine <server.list_online_players.parse[name].include[pizza|potato|anchovy].filter[starts_with[<context.args.last>]]>
    //
    //   # The script that will run when the command is executed.
    //   # No, you do not need '- determine fulfilled' or anything of the sort, since the command is fully registered.
    //   # Available context:
    //   # <context.args> returns a list of input arguments.
    //   # <context.raw_args> returns all the arguments as raw text.
    //   # <context.source_type> returns the source of the command. Can be: PLAYER, SERVER, COMMAND_BLOCK, or COMMAND_MINECART.
    //   # <context.alias> returns the command alias being used.
    //   # <context.command_block_location> returns the command block's location (if the command was run from one).
    //   # <context.command_minecart> returns the EntityTag of the command minecart (if the command was run from one).
    //   script:
    //   - if !<player.is_op||<context.server>>:
    //     - narrate "<red>You do not have permission for that command."
    //     - stop
    //   - narrate "Yay!"
    //   - narrate "My command worked!"
    //   - narrate "And I typed '/<context.alias> <context.raw_args>'!"
    // </code>
    //
    // -->

    public CommandScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        CommandScriptHelper.registerDenizenCommand(new DenizenCommand(this));
    }

    public String getCommandName() {
        return CoreUtilities.toLowerCase(getString("NAME", null));
    }

    public String getDescription() {
        // Replace new lines with a space and a new line, to allow full brief descriptions in /help.
        // Without this, "line<n>line"s brief description would be "lin", because who doesn't like
        // random cutoff-
        return TagManager.tag((getString("DESCRIPTION", "")).replace("\n", " \n"), new BukkitTagContext(null, null, new ScriptTag(this)));
    }

    public String getUsage() {
        return TagManager.tag((getString("USAGE", "")), new BukkitTagContext(null, null, new ScriptTag(this)));
    }

    public List<String> getAliases() {
        List<String> aliases = getStringList("ALIASES");
        return aliases != null ? aliases : new ArrayList<>();
    }

    public String getPermission() {
        return getString("PERMISSION");
    }

    public String getPermissionMessage() {
        return getString("PERMISSION MESSAGE");
    }

    public ScriptQueue runCommandScript(PlayerTag player, NPCTag npc, Map<String, ObjectTag> context) {
        ScriptQueue queue = new InstantQueue(getName()).addEntries(getBaseEntries(
                new BukkitScriptEntryData(player, npc)));
        if (context != null) {
            OldEventManager.OldEventContextSource oecs = new OldEventManager.OldEventContextSource();
            oecs.contexts = context;
            queue.setContextSource(oecs);
        }
        queue.start();
        return queue;
    }

    public boolean runAllowedHelpProcedure(PlayerTag player, NPCTag npc, Map<String, ObjectTag> context) {
        List<ScriptEntry> entries = getEntries(new BukkitScriptEntryData(player, npc), "ALLOWED HELP");

        ScriptQueue queue = new InstantQueue(getName()).addEntries(entries);
        if (context != null) {
            OldEventManager.OldEventContextSource oecs = new OldEventManager.OldEventContextSource();
            oecs.contexts = context;
            queue.setContextSource(oecs);
        }
        queue.start();
        return queue.determinations != null && queue.determinations.size() > 0 && queue.determinations.get(0).equalsIgnoreCase("true");
    }

    public List<String> runTabCompleteProcedure(PlayerTag player, NPCTag npc, Map<String, ObjectTag> context) {
        List<ScriptEntry> entries = getEntries(new BukkitScriptEntryData(player, npc), "TAB COMPLETE");

        ScriptQueue queue = new InstantQueue(getName()).addEntries(entries);
        if (context != null) {
            OldEventManager.OldEventContextSource oecs = new OldEventManager.OldEventContextSource();
            oecs.contexts = context;
            queue.setContextSource(oecs);
        }
        queue.start();
        if (queue.determinations != null && queue.determinations.size() > 0) {
            return ListTag.getListFor(queue.determinations.getObject(0));
        }
        else {
            return new ArrayList<>();
        }
    }

    public boolean hasAllowedHelpProcedure() {
        return contains("ALLOWED HELP");
    }

    public boolean hasTabCompleteProcedure() {
        return contains("TAB COMPLETE");
    }
}
