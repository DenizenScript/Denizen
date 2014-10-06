package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.blocks.FakeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 *
 * @author aufdemrand
 *
 */
public class ShowFakeCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        dList locations = new dList();
        dList entities = new dList();
        boolean added_entities = false;

        // Iterate through arguments
        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matchesPrefix("to", "e", "entities")) {
                for (String entity : dList.valueOf(arg.getValue()))
                    if (dPlayer.matches(entity)) entities.add(entity);
                added_entities = true; // TODO: handle lists properly
            }

            else if (arg.matchesArgumentType(dList.class)) {
                for (String item : dList.valueOf(arg.getValue()))
                    if (dLocation.matches(item)) locations.add(item);
            }

            else if (arg.matchesArgumentType(dLocation.class))
                locations.add(arg.getValue());

            else if (arg.matchesPrefix("d", "duration")
                    && arg.matchesArgumentType(Duration.class))
                scriptEntry.addObject("duration", arg.asType(Duration.class));

            else if (arg.matchesArgumentType(dMaterial.class))
                scriptEntry.addObject("material", arg.asType(dMaterial.class));

            else
                arg.reportUnhandled();

        }

        if (entities.isEmpty() && scriptEntry.hasPlayer())
            entities.add(scriptEntry.getPlayer().identify());

        if (locations.isEmpty())
            throw new InvalidArgumentsException("Must specify at least one valid location!");

        if (!added_entities && (scriptEntry.getPlayer() == null || !scriptEntry.getPlayer().isOnline()))
            throw new InvalidArgumentsException("Must have a valid, online player attached!");

        if (entities.isEmpty() && added_entities)
            throw new InvalidArgumentsException("Must specify valid targets!");

        if (!scriptEntry.hasObject("material"))
            throw new InvalidArgumentsException("Must specify a valid material!");

        scriptEntry.addObject("entities", entities);
        scriptEntry.addObject("locations", locations);
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Duration duration = (!scriptEntry.hasObject("duration")) ? new Duration(10) :
                (Duration) scriptEntry.getObject("duration");
        dMaterial material = (dMaterial) scriptEntry.getObject("material");
        dList list = (dList) scriptEntry.getObject("locations");
        dList players = (dList) scriptEntry.getObject("entities");

        dB.report(scriptEntry, getName(), material.debug()
                + list.debug() + players.debug() + duration.debug());

        for (dObject plr : players.filter(dPlayer.class)) {

            if (plr == null || !((dPlayer) plr).isOnline()) continue;

            for (dObject obj : list.filter(dLocation.class)) {
                new FakeBlock((dPlayer) plr, (dLocation) obj, material, duration);
            }

        }
    }
}
