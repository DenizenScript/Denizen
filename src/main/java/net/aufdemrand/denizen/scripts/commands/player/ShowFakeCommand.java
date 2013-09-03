package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.queues.core.Delayable;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.blocks.FakeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author aufdemrand
 *
 */
public class ShowFakeCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        dList locations = new dList("");

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesArgumentType(dLocation.class))
                locations.add(arg.getValue());

            else if (arg.matchesArgumentType(dList.class)) {
                for (String item : dList.valueOf(arg.getValue()))
                    if (dLocation.matches(item)) locations.add(item);
            }

            else if (arg.matchesPrefix("d, duration")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (arg.matchesArgumentType(dMaterial.class))
                scriptEntry.addObject("material", arg.asType(dMaterial.class));
        }

        if (locations.isEmpty())
            throw new InvalidArgumentsException("Must specify at least one valid location!");

        if (scriptEntry.getPlayer() == null || !scriptEntry.getPlayer().isOnline())
            throw new InvalidArgumentsException("Must have a valid, online player attached!");

        scriptEntry.addObject("locations", locations);
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Duration duration = (!scriptEntry.hasObject("duration")) ? new Duration(10) :
                (Duration) scriptEntry.getObject("duration");
        dMaterial material = (dMaterial) scriptEntry.getObject("material");
        dList list = (dList) scriptEntry.getObject("locations");

        dB.report(getName(), material.debug()
                + list.debug() + scriptEntry.getPlayer().debug() + duration.debug());

        for (dObject obj : list.filter(dLocation.class)) {
            new FakeBlock(scriptEntry.getPlayer(), (dLocation) obj, material, duration);
        }
    }

}
