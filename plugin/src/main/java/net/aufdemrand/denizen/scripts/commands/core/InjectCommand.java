package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.objects.dScript;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;

import java.util.List;

/**
 * Injects a task script in the current ScriptQueue.
 * This replaces the now-deprecated runtask command without the queue argument.
 */
public class InjectCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpretArguments(scriptEntry.aHArgs)) {

            if (arg.matches("instant", "instantly")) {
                scriptEntry.addObject("instant", new Element(true));
            }
            else if (arg.matches("local", "locally")) {
                scriptEntry.addObject("local", new Element(true));
            }
            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class)
                    && !arg.matchesPrefix("p", "path")) {
                scriptEntry.addObject("script", arg.asType(dScript.class));
            }
            else if (!scriptEntry.hasObject("path")) {
                String path = arg.asElement().asString();
                if (!scriptEntry.hasObject("script")) {
                    int dotIndex = path.indexOf('.');
                    if (dotIndex > 0) {
                        dScript script = new dScript(path.substring(0, dotIndex));
                        if (script.isValid()) {
                            scriptEntry.addObject("script", script);
                            path = path.substring(dotIndex + 1);
                        }
                    }
                }
                scriptEntry.addObject("path", new Element(path));
            }
            else {
                arg.reportUnhandled();
            }

        }

        if (!scriptEntry.hasObject("script") && !scriptEntry.hasObject("local")) {
            throw new InvalidArgumentsException("Must define a SCRIPT to be injected.");
        }

        if (!scriptEntry.hasObject("path") && scriptEntry.hasObject("local")) {
            throw new InvalidArgumentsException("Must specify a PATH.");
        }

    }

    @Override
    public void execute(ScriptEntry scriptEntry) {

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(),
                    (scriptEntry.hasObject("script") ? scriptEntry.getdObject("script").debug() : scriptEntry.getScript().debug())
                            + (scriptEntry.hasObject("instant") ? scriptEntry.getdObject("instant").debug() : "")
                            + (scriptEntry.hasObject("path") ? scriptEntry.getElement("path").debug() : "")
                            + (scriptEntry.hasObject("local") ? scriptEntry.getElement("local").debug() : ""));

        }

        // Get the script
        dScript script = scriptEntry.getdObject("script");

        // Get the entries
        List<ScriptEntry> entries;
        // If it's local
        if (scriptEntry.hasObject("local")) {
            entries = scriptEntry.getScript().getContainer().getEntries(scriptEntry.entryData.clone(),
                    scriptEntry.getElement("path").asString());
        }

        // If it has a path
        else if (scriptEntry.hasObject("path")) {
            entries = script.getContainer().getEntries(scriptEntry.entryData.clone(),
                    scriptEntry.getElement("path").asString());
        }

        // Else, assume standard path
        else {
            entries = script.getContainer().getBaseEntries(scriptEntry.entryData.clone());
        }

        if (entries == null) {
            dB.echoError(scriptEntry.getResidingQueue(), "Script inject failed (invalid path or script name)!");
            return;
        }

        // If 'instantly' was specified, run the commands immediately.
        if (scriptEntry.hasObject("instant")) {
            scriptEntry.getResidingQueue().runNow(entries, "INJECT");
        }
        else {
            // Inject the entries into the current scriptqueue
            scriptEntry.getResidingQueue().injectEntries(entries, 0);
        }

    }
}
