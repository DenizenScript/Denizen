package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.LivingEntity;

/**
 *
 * TODO: Document usage
 *
 * Instructs the NPC to follow a player.
 *
 * @author aufdemrand
 *
 */
public class AttackCommand extends AbstractCommand {

    private enum Action { START, STOP }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Action action = Action.START;

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("STOP", arg))
                action = Action.STOP;

           // TODO: Add TARGET: argument.

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        // Stash objects into ScriptEntry
        scriptEntry.addObject("action", action);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Action action = (Action) scriptEntry.getObject("action");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Player", scriptEntry.getPlayer().getName())
                        + aH.debugObj("Action", action.toString()));

        // Stop fighting
        if (action == Action.STOP)
            scriptEntry.getNPC().getNavigator()
                    .cancelNavigation();

        // Start attacking
        else
            scriptEntry.getNPC().getNavigator()
                    .setTarget(scriptEntry.getPlayer(), true);

    }

}