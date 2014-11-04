package net.aufdemrand.denizen.scripts.commands;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;

import net.aufdemrand.denizencore.scripts.commands.BaseAbstractCommand;

public abstract class AbstractCommand extends BaseAbstractCommand {

    public abstract void execute(ScriptEntry scriptEntry) throws CommandExecutionException;

    /**
     * Called by the CommandExecuter before the execute() method is called. Arguments
     * should be iterated through and checked before continuing to execute(). Note that
     * PLAYER:player_name and NPCID:# arguments are parsed automatically by the Executer
     * and should not be handled by this Command otherwise. Their output is stored in the
     * attached {@link ScriptEntry} and can be retrieved with scriptEntry.getPlayer(),
     * scriptEntry.getOfflinePlayer() (if the player specified is not online), and
     * scriptEntry.getNPC(). Remember that any of these have a possibility of being null
     * and should be handled accordingly if required by this Command.
     *
     * @param scriptEntry
     *         The {@link ScriptEntry}, which contains run-time context that may
     *         be utilized by this Command.
     * @throws InvalidArgumentsException
     *         Will halt execution of this Command and hint usage to the console to avoid
     *         unwanted behavior due to missing information.
     *
     */
    public abstract void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException;

}
