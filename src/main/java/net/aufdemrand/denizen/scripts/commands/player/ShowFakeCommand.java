package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
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

            if (arg.matchesPrefix("to", "players")) {
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

            else if (arg.matches("cancel"))
                scriptEntry.addObject("cancel", new Element(true));

            else
                arg.reportUnhandled();

        }

        if (entities.isEmpty() && ((BukkitScriptEntryData)scriptEntry.entryData).hasPlayer())
            entities.add(((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().identify());

        if (locations.isEmpty())
            throw new InvalidArgumentsException("Must specify at least one valid location!");

        if (!added_entities && (!((BukkitScriptEntryData)scriptEntry.entryData).hasPlayer()
                || !((BukkitScriptEntryData)scriptEntry.entryData).getPlayer().isOnline()))
            throw new InvalidArgumentsException("Must have a valid, online player attached!");

        if (entities.isEmpty() && added_entities)
            throw new InvalidArgumentsException("Must specify valid targets!");

        if (!scriptEntry.hasObject("material") && !scriptEntry.hasObject("cancel"))
            throw new InvalidArgumentsException("Must specify a valid material!");

        scriptEntry.addObject("entities", entities);
        scriptEntry.addObject("locations", locations);

        scriptEntry.defaultObject("duration", new Duration(10)).defaultObject("cancel", new Element(false));
    }


    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        Duration duration = scriptEntry.getdObject("duration");
        dMaterial material = scriptEntry.getdObject("material");
        dList list = scriptEntry.getdObject("locations");
        dList players = scriptEntry.getdObject("entities");
        Element cancel = scriptEntry.getElement("cancel");

        dB.report(scriptEntry, getName(), (material != null ? material.debug() : "")
                + list.debug() + players.debug() + duration.debug() + cancel.debug());

        boolean shouldCancel = cancel.asBoolean();

        for (dLocation loc : list.filter(dLocation.class)) {
            if (!shouldCancel)
                FakeBlock.showFakeBlockTo(players.filter(dPlayer.class), loc, material, duration);
            else
                FakeBlock.stopShowingTo(players.filter(dPlayer.class), loc);
        }
    }
}
