package net.aufdemrand.denizen.scripts.commands.core;

import java.io.File;
import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.aH;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.utilities.DenizenAPI;
import net.aufdemrand.denizen.utilities.debugging.DebugLog;
import net.aufdemrand.denizen.utilities.debugging.dB;

/**
 * Lets script authors create their own debug logs to check Denizen actions.
 * Useful for scripts that sometimes need checking the actions.
 * 
 * <b>Logs are getting stored inside the <code>denizen/log</code> folder.</b>
 * 
 * Usage: <code>log "message" (type:severe|info|warning|fine|finer|finest) [file:filename]</code>
 * 
 * @author spaceemotion
 */
public class LogCommand extends AbstractCommand {
    protected static File logDirectory;
    public enum Type { SEVERE, INFO, WARNING, FINE, FINER, FINEST };
    
    public LogCommand() {
        if(logDirectory == null) return;
        
        logDirectory = new File(DenizenAPI.getCurrentInstance().getDataFolder(), "logs");
        logDirectory.mkdirs();
    }
    
    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {
        String message = null, fileName = "";
        Type type = Type.INFO;
        
        if(scriptEntry.getArguments().size() < 2) {
            throw new InvalidArgumentsException("Needs at least 2 arguments (message and file)!");
        }
        
        for(String arg : scriptEntry.getArguments()) {
            if(aH.matchesValueArg("type", arg, aH.ArgumentType.String)) {
                try {
                    type = Type.valueOf(aH.getStringFrom(arg));
                    dB.echoDebug("Set type to " + type.name() + "!");
                } catch(Exception e) {
                    dB.echoError("Invalid type: " + e.getMessage());
                }
            } else if(aH.matchesValueArg("file", arg, aH.ArgumentType.String)) {
                fileName = aH.getStringFrom(arg);
                dB.echoDebug("Appending to '" + fileName + "' log file");
            } else {
                message = arg;
            }
        }
        
        if(message == null)
            throw new InvalidArgumentsException("Must specify a message.");
        
        if(fileName.isEmpty())
            throw new InvalidArgumentsException("Must specify a file.");
        
        File file = new File(logDirectory, fileName);
        DebugLog log = new DebugLog("Denizen-ScriptLog-" + fileName, file.getAbsolutePath());
        
        scriptEntry.addObject("message", message);
        scriptEntry.addObject("name", fileName); // Just for debugging
        scriptEntry.addObject("type", type);
        scriptEntry.addObject("log", log);
    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {
        String message = (String) scriptEntry.getObject("message");
        String fileName = (String) scriptEntry.getObject("name");
        Type type = (Type) scriptEntry.getObject("type");
        DebugLog log = (DebugLog) scriptEntry.getObject("log");
        
         dB.report(getName(),
                aH.debugObj("Type", type) + aH.debugObj("Filename", fileName)
                + aH.debugObj("Message", message));
        
        switch(type) {
            case SEVERE:
                log.severe(message);
                break;
                
            case INFO:
                log.info(message);
                break;
                
            case WARNING:
                log.warning(message);
                break;
                
            case FINE:
                log.fine(message);
                break;
                
            case FINER:
                log.finer(message);
                break;
                
            case FINEST:
                log.finest(message);
        }
        
        log.close();
    }
    
}
