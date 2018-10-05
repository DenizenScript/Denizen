package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.Utilities;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import org.apache.commons.io.FileUtils;

import java.io.File;

public class FileCopyCommand extends AbstractCommand {

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {


        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("origin")
                    && arg.matchesPrefix("origin", "o")) {
                scriptEntry.addObject("origin", arg.asElement());
            }
            else if (!scriptEntry.hasObject("destination")
                    && arg.matchesPrefix("destination", "d")) {
                scriptEntry.addObject("destination", arg.asElement());
            }
            else if (!scriptEntry.hasObject("overwrite")
                    && arg.matches("overwrite")) {
                scriptEntry.addObject("overwrite", new Element("true"));
            }
            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("origin")) {
            throw new InvalidArgumentsException("Must have a valid origin!");
        }

        if (!scriptEntry.hasObject("destination")) {
            throw new InvalidArgumentsException("Must have a valid destination!");
        }

        scriptEntry.defaultObject("overwrite", new Element("false"));
    }

    @Override
    public void execute(final ScriptEntry scriptEntry) throws CommandExecutionException {
        Element origin = scriptEntry.getElement("origin");
        Element destination = scriptEntry.getElement("destination");
        Element overwrite = scriptEntry.getElement("overwrite");

        if (scriptEntry.dbCallShouldDebug()) {

            dB.report(scriptEntry, getName(), origin.debug() + destination.debug() + overwrite.debug());

        }

        if (!Settings.allowFilecopy()) {
            dB.echoError(scriptEntry.getResidingQueue(), "File copy disabled by server administrator.");
            scriptEntry.addObject("success", new Element("false"));
            return;
        }

        File o = new File(DenizenAPI.getCurrentInstance().getDataFolder(), origin.asString());
        File d = new File(DenizenAPI.getCurrentInstance().getDataFolder(), destination.asString());
        boolean ow = overwrite.asBoolean();
        boolean dexists = d.exists();
        boolean disdir = d.isDirectory() || destination.asString().endsWith("/");

        if (!o.exists()) {
            dB.echoError(scriptEntry.getResidingQueue(), "File copy failed, origin does not exist!");
            scriptEntry.addObject("success", new Element("false"));
            return;
        }

        // TODO: Origin is within safe directory path?

        if (!Utilities.isSafeFile(d)) {
            dB.echoError(scriptEntry.getResidingQueue(), "Can't copy files to there!");
            scriptEntry.addObject("success", new Element("false"));
            return;
        }

        if (dexists && !disdir && !ow) {
            dB.echoDebug(scriptEntry, "File copy ignored, destination file already exists!");
            scriptEntry.addObject("success", new Element("false"));
            return;
        }
        try {
            if (dexists && !disdir) {
                d.delete();
            }
            if (disdir && !dexists) {
                d.mkdirs();
            }
            if (o.isDirectory()) {
                FileUtils.copyDirectory(o, d);
            }
            else {
                FileUtils.copyFile(o, d);
            }
            scriptEntry.addObject("success", new Element("true"));
        }
        catch (Exception e) {
            dB.echoError(scriptEntry.getResidingQueue(), e);
            scriptEntry.addObject("success", new Element("false"));
            return;
        }
    }
}
