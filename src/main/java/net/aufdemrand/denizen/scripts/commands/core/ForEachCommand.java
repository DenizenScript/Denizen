package net.aufdemrand.denizen.scripts.commands.core;

import net.aufdemrand.denizen.exceptions.CommandExecutionException;
import net.aufdemrand.denizen.exceptions.InvalidArgumentsException;
import net.aufdemrand.denizen.objects.*;
import net.aufdemrand.denizen.objects.aH.Argument;
import net.aufdemrand.denizen.scripts.ScriptEntry;
import net.aufdemrand.denizen.scripts.queues.ScriptQueue;
import net.aufdemrand.denizen.scripts.ScriptRegistry;
import net.aufdemrand.denizen.scripts.commands.AbstractCommand;
import net.aufdemrand.denizen.scripts.containers.core.TaskScriptContainer;
import net.aufdemrand.denizen.utilities.debugging.dB;
import net.aufdemrand.denizen.utilities.debugging.dB.Messages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author aufdemrand
 *
 */
public class ForEachCommand extends AbstractCommand {

    // - foreach li@p@Vegeta|p@MuhammedAli|n@123 {
    //   - inventory move origin:<%value%.inventory> destination:in@location[123,70,321]
    //   }

    @Override
    public void parseArgs(ScriptEntry scriptEntry) throws InvalidArgumentsException {

        for (aH.Argument arg : aH.interpret(scriptEntry.getArguments())) {

            if (!scriptEntry.hasObject("list")
                    && arg.matchesArgumentType(dList.class))
                scriptEntry.addObject("list", arg.asType(dList.class));
            
            else
                throw new InvalidArgumentsException(Messages.ERROR_LOTS_OF_ARGUMENTS);
            
        }
        
        if (!scriptEntry.hasObject("list"))
            throw new InvalidArgumentsException(Messages.ERROR_MISSING_OTHER, "LIST");

    }

    @Override
    public void execute(ScriptEntry scriptEntry) throws CommandExecutionException {

        // Get objects
        dList list = (dList) scriptEntry.getObject("list");

        // Report to dB
        dB.report(getName(), list.debug());

        for (String value : list) {
            
            scriptEntry.getResidingQueue().addContext("value", value);
            
            
            
        }
        
    }

}
