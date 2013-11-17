package net.aufdemrand.denizen.scripts.commands.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URLDecoder;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.Element;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.debugging.DebugLog;
import net.aufdemrand.denizen.utilities.debugging.dB;

public class LogCommand extends AbstractCommand {

    public enum Type { SEVERE, INFO, WARNING, FINE, FINER, FINEST, NONE }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {
            if (!scriptEntry.hasObject("type")
                &&arg.matchesPrefix("type")
                && arg.matchesEnum(Type.values()))
                scriptEntry.addObject("type", arg.asElement());

            else if (!scriptEntry.hasObject("file")
                    && arg.matchesPrefix("file"))
                scriptEntry.addObject("file", arg.asElement());

            else if (!scriptEntry.hasObject("message"))
                scriptEntry.addObject("message", arg.asElement());

            else
                arg.reportUnhandled();
        }

        if(!scriptEntry.hasObject("message"))
            throw new InvalidArgumentsException("Must specify a message.");

        if(!scriptEntry.hasObject("file"))
            throw new InvalidArgumentsException("Must specify a file.");

        if (!scriptEntry.hasObject("type"))
            scriptEntry.addObject("type", new Element("INFO"));
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        Element message =  scriptEntry.getElement("message");
        Element fileName = scriptEntry.getElement("file");
        Element typeElement = scriptEntry.getElement("type");

         dB.report(scriptEntry, getName(),
                 message.debug() + fileName.debug() + typeElement.debug());

        Type type = Type.valueOf(typeElement.asString().toUpperCase());

        String directory = URLDecoder.decode(System.getProperty("user.dir"));
        File file = new File(directory, fileName.asString());

        if (type == Type.NONE) {
            try {
                file.getParentFile().mkdirs();
                FileWriter fw = new FileWriter(file, true);
                fw.write(message + "\n");
                fw.close();
            }
            catch (IOException e) {
                dB.echoError("Error logging to file...");
                dB.echoError(e);
            }
            return;
        }

        DebugLog log = new DebugLog("Denizen-ScriptLog-" + fileName, file.getAbsolutePath());

        switch(type) {
            case SEVERE:
                log.severe(message.asString());
                break;

            case INFO:
                log.info(message.asString());
                break;

            case WARNING:
                log.warning(message.asString());
                break;

            case FINE:
                log.fine(message.asString());
                break;

            case FINER:
                log.finer(message.asString());
                break;

            case FINEST:
                log.finest(message.asString());
        }

        log.close();
    }

}
