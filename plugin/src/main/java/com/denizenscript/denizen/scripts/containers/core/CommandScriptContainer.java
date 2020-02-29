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
    //   # The name of the command. This will be the default method for running the command, and will show in '/help'.
    //   # | All command scripts MUST have this key!
    //   name: mycmd
    //
    //   # The description of the command. This will be shown in the '/help' command.
    //   # Multiple lines are acceptable, via <&nl> (the tag for a new line), but you should
    //   # make the first line a brief summary of what your command does.
    //   # | All command scripts MUST have this key!
    //   description: My command.
    //
    //   # Correct usage for the command. This will show in the '/help' command.
    //   # This is NOT the name of the command, and it is NOT used to control input parsing. It is EXCLUSIVELY for '/help'.
    //   # | All command scripts MUST have this key!
    //   usage: /mycmd <&lt>myArg1<&gt>
    //
    //   # A list of aliases for the command. These will be used as alternative ways to trigger the command, and will show in the '/help' command.
    //   # | Some command scripts might have this key, but it's optional.
    //   aliases:
    //   - myalias
    //   - mycommand
    //
    //   # The permission node to check for permissions plugins. This will automatically
    //   # block players without the permission from accessing the command and help for
    //   # the command.
    //   # Note that you can include multiple permission nodes (a player only needs to have any one permission from the list)
    //   # by separating them with a semicolon, like: perm.one;perm.two;third.perm
    //   # | Most command scripts should have this key!
    //   permission: my.permission.node
    //
    //   # The message to send to the player when they try to use the command without
    //   # permission. If this is not specified, the default Bukkit message will be sent.
    //   # | Most command scripts should NOT have this key, but it's available.
    //   permission message: Sorry, <player.name>, you can't use my command because you don't have the permission '<permission>'!
    //
    //   # The procedure-based script that will be checked when a player or the console
    //   # is trying to view help for this command. This must always be determined true
    //   # or false. If there is no script, it's assumed that all players and the console
    //   # should be allowed to view the help for this command.
    //   # Available context: <context.server> returns whether the server is viewing the help (a player if false).
    //   # | Most command scripts should NOT have this key, but it's available.
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
    //   # | This key is great to have when used well, but if you're not going to take full advantage of it and write a complex handler, leave it off.
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
    //   # | All command scripts MUST have this key!
    //   script:
    //   - if !<player.is_op||<context.server>>:
    //     - narrate "<red>You do not have permission for that command."
    //     - stop
    //   - narrate Yay!
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
        return CoreUtilities.toLowerCase(getString("name", null));
    }

    public String getDescription() {
        // Replace new lines with a space and a new line, to allow full brief descriptions in /help.
        // Without this, "line<n>line"s brief description would be "lin", because who doesn't like
        // random cutoff-
        return TagManager.tag((getString("description", "")).replace("\n", " \n"), new BukkitTagContext(null, null, new ScriptTag(this)));
    }

    public String getUsage() {
        return TagManager.tag((getString("usage", "")), new BukkitTagContext(null, null, new ScriptTag(this)));
    }

    public List<String> getAliases() {
        List<String> aliases = getStringList("aliases");
        return aliases != null ? aliases : new ArrayList<>();
    }

    public String getPermission() {
        return getString("permission");
    }

    public String getPermissionMessage() {
        return getString("permission message");
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
        List<ScriptEntry> entries = getEntries(new BukkitScriptEntryData(player, npc), "allowed help");

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
        List<ScriptEntry> entries = getEntries(new BukkitScriptEntryData(player, npc), "tab complete");

        ScriptQueue queue = new InstantQueue(getName()).addEntries(entries);
        if (context != null) {
            OldEventManager.OldEventContextSource oecs = new OldEventManager.OldEventContextSource();
            oecs.contexts = context;
            queue.setContextSource(oecs);
        }
        queue.start();
        if (queue.determinations != null && queue.determinations.size() > 0) {
            BukkitTagContext tagContext = new BukkitTagContext(player, npc, new ScriptTag(this));
            return ListTag.getListFor(queue.determinations.getObject(0), tagContext);
        }
        else {
            return new ArrayList<>();
        }
    }

    public boolean hasAllowedHelpProcedure() {
        return contains("allowed help");
    }

    public boolean hasTabCompleteProcedure() {
        return contains("tab complete");
    }
}
