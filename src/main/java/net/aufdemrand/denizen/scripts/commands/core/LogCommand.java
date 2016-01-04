package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.Settings;
import net.aufdemrand.denizen.utilities.debugging.DebugLog;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizencore.exceptions.CommandExecutionException;
import net.aufdemrand.denizencore.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizencore.objects.Element;
import net.aufdemrand.denizencore.objects.aH;
import net.aufdemrand.denizencore.scripts.ScriptEntry;
import net.aufdemrand.denizencore.scripts.commands.AbstractCommand;
import net.aufdemrand.denizencore.tags.TagManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;

public class LogCommand extends AbstractCommand {

    public enum Type {SEVERE, INFO, WARNING, FINE, FINER, FINEST, NONE, CLEAR}

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("type")
                    && arg.matchesPrefix("type")
                    && arg.matchesEnum(Type.values())) {
                scriptEntry.addObject("type", arg.asElement());
            }

            else if (!scriptEntry.hasObject("file")
                    && arg.matchesPrefix("file")) {
                scriptEntry.addObject("file", arg.asElement());
            }

            else if (!scriptEntry.hasObject("message")) {
                scriptEntry.addObject("message", new Element(arg.raw_value));
            }

            else {
                arg.reportUnhandled();
            }
        }

        if (!scriptEntry.hasObject("message")) {
            throw new InvalidArgumentsException("Must specify a message.");
        }

        if (!scriptEntry.hasObject("file")) {
            throw new InvalidArgumentsException("Must specify a file.");
        }

        if (!scriptEntry.hasObject("type")) {
            scriptEntry.addObject("type", new Element("INFO"));
        }
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        if (!Settings.allowLogging()) {
            dB.echoError("Logging disabled by administrator.");
            return;
        }
        Element message = scriptEntry.getElement("message");
        Element fileName = scriptEntry.getElement("file");
        Element typeElement = scriptEntry.getElement("type");

        dB.report(scriptEntry, getName(),
                message.debug() + fileName.debug() + typeElement.debug());

        Type type = Type.valueOf(typeElement.asString().toUpperCase());

        String directory = URLDecoder.decode(System.getProperty("user.dir"));
        File file = new File(directory, fileName.asString());

        String output = TagManager.cleanOutputFully(message.asString());

        file.getParentFile().mkdirs();
        if (type == Type.NONE) {
            try {
                FileWriter fw = new FileWriter(file, true);
                fw.write(output + "\n");
                fw.close();
            }
            catch (IOException e) {
                dB.echoError(scriptEntry.getResidingQueue(), "Error logging to file...");
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            return;
        }

        else if (type == Type.CLEAR) {
            try {
                FileWriter fw = new FileWriter(file);
                if (output.length() > 0) {
                    fw.write(output + "\n");
                }
                fw.close();
            }
            catch (IOException e) {
                dB.echoError(scriptEntry.getResidingQueue(), "Error logging to file...");
                dB.echoError(scriptEntry.getResidingQueue(), e);
            }
            return;
        }

        DebugLog log = new DebugLog("Denizen-ScriptLog-" + fileName, file.getAbsolutePath());

        switch (type) {
            case SEVERE:
                log.severe(output);
                break;

            case INFO:
                log.info(output);
                break;

            case WARNING:
                log.warning(output);
                break;

            case FINE:
                log.fine(output);
                break;

            case FINER:
                log.finer(output);
                break;

            case FINEST:
                log.finest(output);
        }

        log.close();
    }
}
