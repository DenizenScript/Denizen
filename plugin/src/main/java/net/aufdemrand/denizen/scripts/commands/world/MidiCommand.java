package net.aufdemrand.denizen.scripts.commands.world;

import net.aufdemrand.denizen.BukkitScriptEntryData;
import net.aufdemrand.denizen.objects.dEntity;
import net.aufdemrand.denizen.objects.dLocation;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.midi.MidiUtil;
import net.aufdemrand.denizen.utilities.midi.NoteBlockReceiver;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dList;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.scripts.commands.Holdable;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class MidiCommand extends AbstractCommand implements Holdable {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("cancel")
                    && (arg.matches("cancel") || arg.matches("stop"))) {
                scriptEntry.addObject("cancel", "");
            }
            else if (!scriptEntry.hasObject("location") &&
                    arg.matchesArgumentType(dLocation.class)) {
                scriptEntry.addObject("location", arg.asType(dLocation.class));
            }
            else if (!scriptEntry.hasObject("entities") &&
                    arg.matchesArgumentList(dEntity.class)) {
                scriptEntry.addObject("entities", arg.asType(dList.class).filter(dEntity.class, scriptEntry));
            }
            else if (!scriptEntry.hasObject("volume") &&
                    arg.matchesPrimitive(aH.PrimitiveType.Double) &&
                    arg.matchesPrefix("volume", "vol", "v")) {
                scriptEntry.addObject("volume", arg.asElement());
            }
            else if (!scriptEntry.hasObject("tempo") &&
                    arg.matchesPrimitive(aH.PrimitiveType.Double)) {
                scriptEntry.addObject("tempo", arg.asElement());
            }
            else if (!scriptEntry.hasObject("file")) {

                String path = DenizenAPI.getCurrentInstance().getDataFolder() +
                        File.separator + "midi" +
                        File.separator + arg.getValue();
                if (!path.endsWith(".mid")) {
                    path = path + ".mid";
                }

                scriptEntry.addObject("file", new Element(path));
            }
            else {
                arg.reportUnhandled();
            }
        }

        // Produce error if there is no file and the "cancel" argument was
        // not used
        if (!scriptEntry.hasObject("file")
                && !scriptEntry.hasObject("cancel")) {
            throw new InvalidArgumentsException("Missing file (Midi name) argument!");
        }

        if (!scriptEntry.hasObject("location")) {
            scriptEntry.defaultObject("entities", (((BukkitScriptEntryData) scriptEntry.entryData).hasPlayer() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getPlayer().getDenizenEntity()) : null),
                    (((BukkitScriptEntryData) scriptEntry.entryData).hasNPC() ? Arrays.asList(((BukkitScriptEntryData) scriptEntry.entryData).getNPC().getDenizenEntity()) : null));
        }

        scriptEntry.defaultObject("tempo", new Element(1)).defaultObject("volume", new Element(10));
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute(final ScriptEntry scriptEntry) {

        boolean cancel = scriptEntry.hasObject("cancel");
        File file = !cancel ? new File(scriptEntry.getElement("file").asString()) : null;

        if (!cancel && !Utilities.canReadFile(file)) {
            dB.echoError("Server config denies reading files in that location.");
            return;
        }

        if (!cancel && !file.exists()) {
            dB.echoError(scriptEntry.getResidingQueue(), "Invalid file " + scriptEntry.getElement("file").asString());
            return;
        }

        List<dEntity> entities = (List<dEntity>) scriptEntry.getObject("entities");
        dLocation location = (dLocation) scriptEntry.getObject("location");
        float tempo = scriptEntry.getElement("tempo").asFloat();
        float volume = scriptEntry.getElement("volume").asFloat();

        // Report to dB
        if (scriptEntry.dbCallShouldDebug()) {
            dB.report(scriptEntry, getName(), (cancel ? aH.debugObj("cancel", cancel) : "") +
                    (file != null ? aH.debugObj("file", file.getPath()) : "") +
                    (entities != null ? aH.debugObj("entities", entities.toString()) : "") +
                    (location != null ? location.debug() : "") +
                    aH.debugObj("tempo", tempo) +
                    aH.debugObj("volume", volume));
        }

        // Play the midi
        if (!cancel) {
            NoteBlockReceiver rec;
            if (location != null) {
                rec = MidiUtil.playMidi(file, tempo, volume, location);
            }
            else {
                rec = MidiUtil.playMidi(file, tempo, volume, entities);
            }
            if (rec == null) {
                dB.echoError(scriptEntry.getResidingQueue(), "Something went wrong playing a midi!");
                scriptEntry.setFinished(true);
            }
            else {
                rec.onFinish = new Runnable() {
                    @Override
                    public void run() {
                        scriptEntry.setFinished(true);
                    }
                };
            }
        }
        else {
            if (location != null) {
                MidiUtil.stopMidi(location.identify());
            }
            else {
                MidiUtil.stopMidi(entities);
            }
            scriptEntry.setFinished(true);
        }
    }
}
