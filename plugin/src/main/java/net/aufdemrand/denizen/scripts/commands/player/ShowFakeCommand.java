package net.aufdemrand.denizen.scripts.commands.player;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.objects.dMaterial;
import net.aufdemrand.denizen.objects.dPlayer;
import net.aufdemrand.denizen.utilities.blocks.FakeBlock;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Duration;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

public class ShowFakeCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        dList locations = new dList();
        dList entities = new dList();
        boolean added_entities = false;

        // Iterate through arguments
        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matchesPrefix("to", "players")) {
                for (String entity : dList.valueOf(arg.getValue())) {
                    if (dPlayer.matches(entity)) {
                        entities.add(entity);
                    }
                }
                added_entities = true; // TODO: handle lists properly
            }
            else if (arg.matchesArgumentList(dMaterial.class)) {
                scriptEntry.addObject("materials", arg.asType(dList.class));
            }
            else if (locations.isEmpty()
                    && arg.matchesArgumentType(dList.class)) {
                for (String item : dList.valueOf(arg.getValue())) {
                    if (dLocation.matches(item)) {
                        locations.add(item);
                    }
                }
            }
            else if (locations.isEmpty()
                    && arg.matchesArgumentType(dLocation.class)) {
                locations.add(arg.getValue());
            }
            else if (arg.matchesPrefix("d", "duration")
                    && arg.matchesArgumentType(Duration.class)) {
                scriptEntry.addObject("duration", arg.asType(Duration.class));
            }
            else if (arg.matches("cancel")) {
                scriptEntry.addObject("cancel", new Element(true));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (entities.isEmpty() && ((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()) {
            entities.add(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().identify());
        }

        if (locations.isEmpty()) {
            throw new InvalidArgumentsException("Must specify at least one valid location!");
        }

        if (!added_entities && (!((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer()
                || !((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().isOnline())) {
            throw new InvalidArgumentsException("Must have a valid, online player attached!");
        }

        if (entities.isEmpty() && added_entities) {
            throw new InvalidArgumentsException("Must specify valid targets!");
        }

        if (!scriptEntry.hasObject("materials") && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Must specify valid material(s)!");
        }

        scriptEntry.addObject("entities", entities);
        scriptEntry.addObject("locations", locations);

        scriptEntry.defaultObject("duration", new Duration(10)).defaultObject("cancel", new Element(false));
    }


    @Override
    public void execute(ScriptEntry scriptEntry) {

        Duration duration = scriptEntry.getdObject("duration");
        dList material_list = scriptEntry.getdObject("materials");
        dList list = scriptEntry.getdObject("locations");
        dList players = scriptEntry.getdObject("entities");
        Element cancel = scriptEntry.getElement("cancel");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), (material_list != null ? material_list.debug() : "")
                    + list.debug() + players.debug() + duration.debug() + cancel.debug());

        }


        boolean shouldCancel = cancel.asBoolean();

        List<dMaterial> mats = null;
        if (!shouldCancel) {
            mats = material_list.filter(dMaterial.class, scriptEntry);
        }

        int i = 0;
        for (dLocation loc : list.filter(dLocation.class, scriptEntry)) {
            if (!shouldCancel) {
                FakeBlock.showFakeBlockTo(players.filter(dPlayer.class, scriptEntry), loc, mats.get(i % mats.size()), duration);
            }
            else {
                FakeBlock.stopShowingTo(players.filter(dPlayer.class, scriptEntry), loc);
            }
            i++;
        }
    }
}
