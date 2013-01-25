package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEngine;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

/**
 * Clears queue(s)
 *
 * @author aufdemrand
 */

public class BreakCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        List<ScriptEngine.QueueType> queues = new ArrayList<ScriptEngine.QueueType>();

        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesQueueType(arg))
                queues.add(aH.getQueueFrom(arg));

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        if (queues.isEmpty()) throw new InvalidArgumentsException("Must specify at least one queueType!");

        scriptEntry.addObject("queues", queues);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        List<ScriptEngine.QueueType> queues = (List<ScriptEngine.QueueType>) scriptEntry.getObject("queues");

        dB.echoApproval(ChatColor.DARK_GRAY + "Executing '" + ChatColor.YELLOW +  getName() + ChatColor.DARK_GRAY + "': "
                + "Queues=" + ChatColor.YELLOW + queues.toString() + ChatColor.DARK_GRAY + "'"
                + (scriptEntry.getPlayer() != null ? ", PLAYER='" + ChatColor.YELLOW + scriptEntry.getPlayer().getName()
                + ChatColor.DARK_GRAY + "'" : "")
                + (scriptEntry.getNPC() != null ? ", NPC='" + ChatColor.YELLOW + scriptEntry.getNPC().getName()
                + "/" + scriptEntry.getNPC().getId() + ChatColor.DARK_GRAY + "'" : ""));

        List<ScriptEntry> emptyList = new ArrayList<ScriptEntry>();

        for (ScriptEngine.QueueType queue : queues) {
            if (queue == ScriptEngine.QueueType.PLAYER)
                denizen.getScriptEngine().replaceQueue(scriptEntry.getPlayer(), emptyList, ScriptEngine.QueueType.PLAYER);
            else if (queue == ScriptEngine.QueueType.PLAYER_TASK)
                denizen.getScriptEngine().replaceQueue(scriptEntry.getPlayer(), emptyList, ScriptEngine.QueueType.PLAYER_TASK);
            else if (queue == ScriptEngine.QueueType.NPC)
                denizen.getScriptEngine().replaceQueue(scriptEntry.getNPC(), emptyList, ScriptEngine.QueueType.NPC);
        }
    }
}
