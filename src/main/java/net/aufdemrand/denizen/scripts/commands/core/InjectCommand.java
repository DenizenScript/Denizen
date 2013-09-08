package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.queues.core.InstantQueue;
import net.aufdemrand.denizen.scripts.queues.core.TimedQueue;
import net.aufdemrand.denizen.utilities.debugging.dB;

import java.util.List;

/**
 * Injects a task script in the current ScriptQueue.
 * This replaces the now-deprecated runtask command without the queue argument.
 *
 * @author Jeremy Schroeder
 *
 */
public class InjectCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (arg.matches("instant, instantly"))
                scriptEntry.addObject("instant", new Element(true));

            else if (arg.matches("local, locally"))
                scriptEntry.addObject("local", new Element(true));

            else if (!scriptEntry.hasObject("script")
                    && arg.matchesArgumentType(dScript.class))
                scriptEntry.addObject("script", arg.asType(dScript.class));

            else if (!scriptEntry.hasObject("path"))
                scriptEntry.addObject("path", arg.asElement());

        }

        if (!scriptEntry.hasObject("script") && !scriptEntry.hasObject("local"))
            throw new InvalidArgumentsException("Must define a SCRIPT to be injected.");

        if (!scriptEntry.hasObject("path") && scriptEntry.hasObject("local"))
            throw new InvalidArgumentsException("Must specify a PATH.");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        dB.report(getName(),
                (scriptEntry.hasObject("script") ? scriptEntry.getdObject("script").debug() : scriptEntry.getScript().debug())
                        + (scriptEntry.hasObject("instant") ? scriptEntry.getdObject("instant").debug() : "")
                        + (scriptEntry.hasObject("path") ? scriptEntry.getElement("path").debug() : "")
                        + (scriptEntry.hasObject("local") ? scriptEntry.getElement("local").debug() : ""));

        // Get the script
        dScript script = (dScript) scriptEntry.getObject("script");

        // Get the entries
        List<ScriptEntry> entries;
        // If it's local
        if (scriptEntry.hasObject("local"))
            entries = scriptEntry.getScript().getContainer().getEntries(
                    scriptEntry.getPlayer(),
                    scriptEntry.getNPC(),
                    scriptEntry.getElement("path").asString());

        // If it has a path
        else if (scriptEntry.hasObject("path"))
            entries = script.getContainer().getEntries(
                    scriptEntry.getPlayer(),
                    scriptEntry.getNPC(),
                    scriptEntry.getElement("path").asString());

        // Else, assume standard path
        else entries = script.getContainer().getBaseEntries(
                    scriptEntry.getPlayer(),
                    scriptEntry.getNPC());

        // If 'instantly' was specified, make each entry 'instant'.
        if (scriptEntry.hasObject("instant"))
            for (ScriptEntry entry : entries)
                    entry.setInstant(true);

        // Inject the entries into the current scriptqueue
        scriptEntry.getResidingQueue().injectEntries(entries, 0);

    }

}
