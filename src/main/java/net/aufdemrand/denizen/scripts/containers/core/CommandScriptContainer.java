package net.aufdemrand.denizen.scripts.containers.core;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptBuilder;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.core.DetermineCommand;
import net.aufdemrand.denizen.scripts.containers.ScriptContainer;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.tags.TagManager;
import net.aufdemrand.denizen.utilities.DenizenCommand;
import net.aufdemrand.denizencore.utilities.YamlConfiguration;

import java.util.*;

public class CommandScriptContainer extends ScriptContainer {

    // <--[language]
    // @name Command Script Containers
    // @group Script Container System
    // @description
    // Command script containers allow you to register commands with CraftBukkit via magic.
    // This, in turn, allows the command to show up in the '/help' command, with some info
    // on the command.
    //
    // Note that existing names or aliases from other plugins will be overridden.
    // If you want to run a script at the same time as an existing command, see <@link example on command event tutorial>.
    //
    // The following is the format for the container.
    //
    //
    // <code>
    // # The name of the script doesn't matter, and will not affect the command in any way.
    // Command Script Name:
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
    //   # The procedure-based script that will be checked when a player or the console
    //   # is trying to view help for this command. This must always be determined true
    //   # or false. If there is no script, it's assumed that all players and the console
    //   # should be allowed to view the help for this command.
    //   # Available context: <context.server> returns whether the server is viewing the help (a player if false).
    //   allowed help:
    //   - determine <player.is_op>
    //
    //   # The script that will run when the command is executed.
    //   # No, you do not need '- determine fulfilled' or anything of the sort, since
    //   # the command is fully registered.
    //   # Available context: <context.args> returns a list of input arguments.
    //   # <context.raw_args> returns all the arguments as raw text.
    //   # <context.server> returns whether the server is running the command (a player if false).
    //   script:
    //   - if !<player.is_op> {
    //     - narrate "<red>You do not have permission for that command."
    //     - queue clear
    //     }
    //   - narrate "Yay!"
    //   - narrate "My command worked!"
    // </code>
    //
    // -->

    public CommandScriptContainer(YamlConfiguration configurationSection, String scriptContainerName) {
        super(configurationSection, scriptContainerName);
        CommandScriptHelper.registerDenizenCommand(new DenizenCommand(this));
    }

    public String getCommandName() {
        return getString("NAME", null).toLowerCase();
    }

    public String getDescription() {
        // Replace new lines with a space and a new line, to allow full brief descriptions in /help.
        // Without this, "line<n>line"s brief description would be "lin", because who doesn't like
        // random cutoff-
        return TagManager.tag(null, null, getString("DESCRIPTION", "")).replace("\n", " \n");
    }

    public String getUsage() {
        return TagManager.tag(null, null, getString("USAGE", ""));
    }

    public List<String> getAliases() {
        List<String> aliases = getStringList("ALIASES");
        return aliases != null? aliases : new ArrayList<String>();
    }

    public ScriptQueue runCommandScript(dPlayer player, dNPC npc, Map<String, dObject> context) {
        ScriptQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId(getName())).addEntries(getBaseEntries(
                new BukkitScriptEntryData(player, npc)));
        if (context != null) {
            for (Map.Entry<String, dObject> entry : context.entrySet()) {
                queue.addContext(entry.getKey(), entry.getValue());
            }
        }
        queue.start();
        return queue;
    }

    public boolean runAllowedHelpProcedure(dPlayer player, dNPC npc, Map<String, dObject> context) {
        // Add the reqId to each of the entries for the determine command
        List<ScriptEntry> entries = getEntries(new BukkitScriptEntryData(player, npc), "ALLOWED HELP");
        long id = DetermineCommand.getNewId();
        ScriptBuilder.addObjectToEntries(entries, "ReqId", id);

        ScriptQueue queue = InstantQueue.getQueue(ScriptQueue.getNextId(getName())).setReqId(id).addEntries(entries);
        if (context != null) {
            for (Map.Entry<String, dObject> entry : context.entrySet()) {
                queue.addContext(entry.getKey(), entry.getValue());
            }
        }
        queue.start();
        return DetermineCommand.getOutcome(id).equalsIgnoreCase("true");
    }

    public boolean hasAllowedHelpProcedure() {
        return contains("ALLOWED HELP");
    }
}
