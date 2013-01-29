package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.arguments.aH;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.entity.LivingEntity;

/**
 * Instructs the NPC to follow a player.
 *
 * @author aufdemrand
 *
 */
public class AttackCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        Boolean stop = false;

        // Parse Arguments
        for (String arg : scriptEntry.getArguments()) {
            if (aH.matchesArg("STOP", arg))
                stop = true;

            else throw new InvalidArgumentsException(dB.Messages.ERROR_UNKNOWN_ARGUMENT, arg);
        }

        scriptEntry.addObject("stop", stop);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        // Get objects
        Boolean stop = (Boolean) scriptEntry.getObject("stop");

        // Report to dB
        dB.report(getName(),
                aH.debugObj("Player", scriptEntry.getPlayer().getName())
                        + (!stop ? aH.debugObj("Action", "ATTACK")
                        : aH.debugObj("Action", "STOP")));

        if (stop)
            scriptEntry.getNPC().getNavigator()
                    .cancelNavigation();
        else
            scriptEntry.getNPC().getNavigator()
                    .setTarget((LivingEntity) scriptEntry.getPlayer(), true);

    }

}